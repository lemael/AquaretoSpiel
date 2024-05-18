package service.network

import CSVMapper
import service.*
import entity.*
import edu.udo.cs.sopra.ntf.*
import entity.players.DumbBot
import entity.players.HotseatPlayer
import entity.players.OnlineBGWPlayer
import entity.players.SmartBot
import view.Refreshable

typealias Wagon = MutableList<Wagonable>

/**
 * Service layer class that realizes the necessary logic for sending and receiving messages
 * in multiplayer network games. Bridges between the [NetworkClient] and the other services.
 */
class NetworkService(private val rootService: RootService) : AbstractRefreshingService() {

    companion object {
        /** URL of the BGW net server hosted for SoPra participants */
        const val SERVER_ADDRESS = "sopra.cs.tu-dortmund.de:80/bgw-net/connect"

        /** Name of the game as registered with the server */
        const val GAME_ID = "Aquaretto"
    }

    /** Network client. Nullable for offline games. */
    var client: NetworkClient? = null
        private set

    var isOnlineSession = false

    /**
     * Additional attributes that holds the  TakeTruckMessage Elements
     **/
    //for sendMoveTil
    var boughtCardFrom : String? = null
    //for sendTakeWagon
    val animalsToSend: MutableList<Triple<Int, Int, Int>> = mutableListOf()
    val offspringToSend: MutableList<Triple<Int, Int, Int>> = mutableListOf()
    val workersToSend: MutableList<Triple<Int, Int, JobEnum>> = mutableListOf()
    var takenWagon = mutableListOf<Wagonable>()
    var takenWagonId = -1
    var netWagon = mutableListOf<Wagonable>()

    var wagonsSnapshot = listOf<Wagon>()


    /** Helper function that determines the next player based on index and based on wether hetookwagon or not **/

     fun determineNextPlayer() : Int {
        val game = rootService.aquarettoGame
        val oldCurr = game.currentPlayer
        var newCurrentPlayer: Int = oldCurr
        if (!game.currentPlayer().tookWagon) {
            newCurrentPlayer = (game.currentPlayer + 1) % game.players.size

            while (true) {
                if (!game.players[newCurrentPlayer].tookWagon) {
                    break
                }
                newCurrentPlayer = (newCurrentPlayer + 1) % game.players.size
            }

        }
        return newCurrentPlayer
     }



    /**
     * current state of the connection in a network game.
     */
    var connectionState: ConnectionState = ConnectionState.DISCONNECTED
        private set

    /**
     * Updates the [connectionState] to [newState] and notifies
     * all refreshable via [Refreshable.refreshConnectionState]
     */
    fun updateConnectionState(newState: ConnectionState) {
        this.connectionState = newState
        onAllRefreshables {
            refreshConnectionState(newState)
        }
    }

    /**
     * Connects to server and creates a new game session.
     *
     * @param secret Server secret.
     * @param name Player name.
     * @param sessionID identifier of the hosted session (to be used by guest on join)
     *
     * @throws IllegalStateException if already connected to another game or connection attempt fails
     */
    fun hostGame(secret: String, name: String, sessionID: String?) {
        if (!connect(secret, name)) {
            error("Connection failed")
        }
        //updateConnectionState(ConnectionState.CONNECTED)

        if (sessionID.isNullOrBlank()) {
            client?.createGame(GAME_ID, "Welcome!")
        } else {
            client?.createGame(GAME_ID, sessionID, "Welcome!")
        }
        //updateConnectionState(ConnectionState.WAITING_FOR_HOST_CONFIRMATION)
    }

    /**
     * Disconnects the [client] from the server, nulls it and updates the
     * [connectionState] to [ConnectionState.DISCONNECTED]. Can safely be called
     * even if no connection is currently active.
     */
    fun disconnect() {
        client?.apply {
            if (sessionID != null) leaveGame("Goodbye!")
            if (isOpen) disconnect()
        }
        client = null
        rootService.networkService.isOnlineSession = false
        //updateConnectionState(ConnectionState.DISCONNECTED)
    }

    /**
     * Connects to server and joins a game session as guest player.
     *
     * @param secret Server secret.
     * @param name Player name.
     * @param sessionID identifier of the joined session (as defined by host on create)
     *
     * @throws IllegalStateException if already connected to another game or connection attempt fails
     */
    fun joinGame(secret: String, name: String, sessionID: String) {
        if (!connect(secret, name)) {
            error("Connection failed")
        }
        //updateConnectionState(ConnectionState.CONNECTED)

        client?.joinGame(sessionID, "Hello!")

        //updateConnectionState(ConnectionState.WAITING_FOR_JOIN_CONFIRMATION)
    }


    /**
     * Connects to server, sets the [NetworkService.client] if successful and returns `true` on success.
     *
     * @param secret Network secret. Must not be blank (i.e. empty or only whitespaces)
     * @param name Player name. Must not be blank
     *
     * @throws IllegalArgumentException if secret or name is blank
     * @throws IllegalStateException if already connected to another game
     */
    private fun connect(secret: String, name: String): Boolean {
        //require(connectionState == ConnectionState.DISCONNECTED && client == null)
        { "already connected to another game" }

        require(secret.isNotBlank()) { "server secret must be given" }
        require(name.isNotBlank()) { "player name must be given" }

        val newClient =
            NetworkClient(
                playerName = name,
                host = SERVER_ADDRESS,
                secret = secret,
                networkService = this
            )

        return if (newClient.connect()) {
            this.client = newClient
            this.rootService.networkService.isOnlineSession = true
            true
        } else {
            false
        }
    }

    /*** COMPLETE HERE:   ****/
    fun startNewJoinedGame(message: InitGameMessage, playerName: String, playerType: Int) {
        check(playerType in 0..3) { "Invalid player type provided" }
        //check(connectionState == ConnectionState.WAITING_FOR_INIT) { "not waiting for game init message. " }

        val playerNames = message.players.toMutableList()
        val indexOfLocalPlayer = playerNames.indexOf(playerName)

        val playerTypes = MutableList(playerNames.size) { ONLINE }
        playerTypes[indexOfLocalPlayer] = playerType

        for (names in playerNames) {
            if(client?.playerName == names ){
                playerTypes[indexOfLocalPlayer] = onlinePlayerIs
            }
        }


        val animals = message.drawPile.mapNotNull { id ->
            CSVMapper.mapToWagonableById(id)
        }
        val wagAnimals: MutableList<Wagonable> = animals.map { it as Wagonable }.toMutableList()
        rootService.gameStateService.initializeGame(playerNames, playerTypes, wagAnimals, false)

        rootService.gameStateService.reverseLists()

        onAllRefreshables { refreshAfterInitializeGame() }

        // TODO()  Handle hotseat player : --> client ist hotseat client?.playerName is me
        val currentPlayer = rootService.aquarettoGame.currentPlayer()

        /*when (currentPlayer.name) {
            playerName -> updateConnectionState(ConnectionState.WAITING_FOR_MY_TURN)
            else -> updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)
        }*/
    }

    fun receiveAddTileToTruck(message: AddTileToTruckMessage) {
        val truckId = rootService.aquarettoGame.board.transportWagons.indexOf(wagonsSnapshot[message.truckId])
        rootService.playerActionService.putCardOnWagon(truckId)
        rootService.gameStateService.endTurn()
    }

    fun receiveBuyExpansion(message: BuyExpansionMessage) {
        val positionList: List<PositionPair> = message.positionList
        when (positionList.size) {
            3 -> { // Small expansion
                val baseAndOrientation = findBaseAndOrientationForSmallExpansion(positionList)
                val baseCoordinateS: Pair<Int, Int> = baseAndOrientation.first.x to baseAndOrientation.first.y
                rootService.playerActionService.addSmallExtension(baseCoordinateS, baseAndOrientation.second)
            }

            4 -> { // Large expansion
                val base = findBaseForLargeExpansion(positionList)
                val baseCoordinateB: Pair<Int, Int> = base.x to base.y
                rootService.playerActionService.addLargeExtension(baseCoordinateB)
            }

            else -> throw IllegalArgumentException("Invalid number of tiles in expansion.")
        }
        rootService.gameStateService.endTurn()
    }


    fun receiveMoveCoworker(message: MoveCoworkerMessage) {
        val start: WorkerTriple = message.start
        val destination: WorkerTriple = message.destination
        val startJob: JobEnum = start.jobEnum
        val oldCoordinates = start.x to start.y
        val newCoordinates = destination.x to destination.y
        //TODO(): check if use of relocate is correct.
        when (startJob) {
            JobEnum.CASHIER -> rootService.playerActionService.relocateWorker(CASHIER, newCoordinates)
            JobEnum.KEEPER -> rootService.playerActionService.relocateWorker(NURSE, newCoordinates)
            JobEnum.MANAGER -> rootService.playerActionService.relocateWorker(MANAGER, newCoordinates)
            else -> rootService.playerActionService.relocateWorker(oldCoordinates, newCoordinates)
        }
        rootService.gameStateService.endTurn()
    }


    fun receiveMoveTile(message: MoveTileMessage) {
        // Retrieve the player object
        val buyFromPlayer: Player = rootService.aquarettoGame.players.find { it.name == message.playerName }
            ?: throw IllegalArgumentException("Player not found: ${message.playerName}")

        // Current player performing the buy action
        val currentPlayer = rootService.aquarettoGame.currentPlayer()

        // Buying the card from the specified player's depot
        // according to buyCommand, this  transfers the top card from buyFromPlayer's
        // depot to currentPlayer's cardsToPlay list
        if(buyFromPlayer == currentPlayer){
            println("---------------------------------- depot to park ----------------------------------")
            rootService.playerActionService.depotToPark()
        }
        else{
            println("---------------------------------- buy from other player $buyFromPlayer ----------------------------------")
            rootService.playerActionService.buyCard(buyFromPlayer)
        }

        //the card bought is now the last in the currentPlayer's cardsToPlay list
        val boughtCard = currentPlayer.cardsToPlay.lastOrNull()
            ?: throw IllegalStateException("No card was bought or the bought card is not a valid Depotable type")

        // Placing the bought card into the park at the specified position from the message
        if(message.position.x == 0 && message.position.y == 0)
            rootService.playerActionService.animalToDepot(boughtCard)
        else
            rootService.playerActionService.animalToPark(boughtCard, message.position.x, message.position.y)

        // Removing the card from cardsToPlay list after it has been placed in the park,
        currentPlayer.cardsToPlay.remove(boughtCard)

        // Handling offspring placement from the message's offspringList
        // If List is empty, forEach will not execute anything
        if(boughtCard !is Baby) message.offspringList.forEach { offspring ->
            val offspringAnimal = CSVMapper.mapToWagonableById(offspring.tileId) as? Depotable
                ?: throw IllegalArgumentException("Invalid offspring tile ID: ${offspring.tileId}")
            if(offspring.x == 0 && offspring.y == 0)
                rootService.playerActionService.animalToDepot(offspringAnimal)
            else
                rootService.playerActionService.animalToPark(offspringAnimal, offspring.x, offspring.y)
        }

        // Handling workers based on their jobEnum provided in the workerList from the message
        message.workerList.forEach { worker ->
            when (worker.jobEnum) {
                JobEnum.TRAINER -> {
                    // If the worker is a TRAINER, move the worker to the specified new position
                    rootService.playerActionService.relocateWorker(null, Pair(worker.x, worker.y))
                }
                // For manager, nurse (KEEPER), or cashier no action is required as per game rules
                JobEnum.MANAGER, JobEnum.CASHIER, JobEnum.KEEPER -> { /* No action required */
                }

                else -> throw IllegalArgumentException("Unknown job type: ${worker.jobEnum}")
            }
        }
        //clearing cards to play when online player
        rootService.aquarettoGame.currentPlayer().cardsToPlay.clear()

        rootService.gameStateService.endTurn()
    }

    /**
     * play the opponent's turn by handling the [TakeTruckMessage] sent through the server.
     * placing all Animals, children and workers on the given coordinates.
     * If (0,0) place animal into depot
     * */
    fun receiveTakeTruck(message: TakeTruckMessage) {

        /*check(connectionState == ConnectionState.WAITING_FOR_OPPONENT) {
            "not expecting an opponent's turn."
        }*/

        val localTruckId = rootService.aquarettoGame.board.transportWagons.indexOf(wagonsSnapshot[message.truckId])
        val allPlayerCards = rootService.aquarettoGame.board.transportWagons[localTruckId].toList()

        // Taking the specified truck with all its tiles: animals and possibly Offspring and workers
        rootService.playerActionService.takeWagon(localTruckId)
        println("took wagon is over now")
        // Process each animal from the truck: Placing in Depot if position is (0,0)
        message.animalList.forEach { animalTriple ->
            val animalID = (allPlayerCards[animalTriple.truck] as Animal).ID
            val animalOrBaby = CSVMapper.mapToWagonableById(animalID) as? Depotable
                ?: throw IllegalArgumentException("Invalid animal or baby animal ID: ${animalTriple.truck}")
            if (animalTriple.x == 0 && animalTriple.y == 0) {
                rootService.playerActionService.animalToDepot(animalOrBaby)
            } else {
                try {
                    rootService.playerActionService.animalToPark(animalOrBaby, animalTriple.x, animalTriple.y)
                } catch (e: IllegalArgumentException) {
                    println("Placing animal/baby at ${animalTriple.x}, ${animalTriple.y} is not possible. Exception caught in AnimalToPark: ${e.message}")
                }
            }
        }

        // Handling potential offspring placement in the park
        message.offspringList.forEach { offspringTriple ->
            val offspring = CSVMapper.mapToWagonableById(offspringTriple.tileId) as? Depotable
                ?: throw IllegalArgumentException("Invalid offspring ID: ${offspringTriple.tileId}")
            if (offspringTriple.x == 0 && offspringTriple.y == 0) {
                rootService.playerActionService.animalToDepot(offspring)
            } else {
                try {
                    rootService.playerActionService.animalToPark(offspring, offspringTriple.x, offspringTriple.y)
                } catch (e: IllegalArgumentException) {
                    //Catch the exception from the AnimalToPark Require-Statements
                    println("Placing offspring at ${offspringTriple.x}, ${offspringTriple.y} is not possible. Exception caught in AnimalToPark: ${e.message}")
                }
            }
        }

        // Handle potential workers produced as a result of taking the truck
        message.workerList.forEach { workerTriple ->
            when (workerTriple.jobEnum) {
                JobEnum.TRAINER -> rootService.playerActionService.relocateWorker(
                    null,
                    Pair(workerTriple.x, workerTriple.y)
                )
                JobEnum.MANAGER -> rootService.playerActionService.relocateWorker(null, MANAGER)
                JobEnum.CASHIER -> rootService.playerActionService.relocateWorker(null, CASHIER)
                JobEnum.KEEPER -> rootService.playerActionService.relocateWorker(null, NURSE)
            }
        }
        println("placed all things")
        //clearing cards to play when online player
        rootService.aquarettoGame.currentPlayer().cardsToPlay.clear()

        /*val nextPlayer = determineNextPlayer()
        if(nextPlayer != rootService.aquarettoGame.currentPlayer ){
            updateConnectionState(ConnectionState.WAITING_FOR_MY_TURN)
        }else{
            updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)
        }*/
        println("going to end the turn now")
        rootService.gameStateService.updateEndTurn()
        println("end turn is over")
    }


    /**
     * Message when the top of the depot gets discarded
     */
    fun receiveDiscard(message: DiscardMessage) {
        /*check(connectionState == ConnectionState.WAITING_FOR_OPPONENT) {
            " Expecting my turn not opponents "
        }*/
        rootService.playerActionService.sellCard()

        //updateConnectionState(ConnectionState.WAITING_FOR_MY_TURN)

        rootService.gameStateService.endTurn()
    }


    /** Helper function that find the base and orientation of  smallExpansion positionList.
     * Returns a Pair of Base position (Pair(x,y)) and orientation (Int)
     * Base : iterate through each position to see which one, when considered the base, leaves the other
     * two with either a shared x or y coordinate.
     * Orientation: Given the base,find the correct pattern corresponding to TOP_LEFT/TOP_RIGHT...
     */
    private fun findBaseAndOrientationForSmallExpansion(positionList: List<PositionPair>): Pair<PositionPair, Int> {

        val baseX = positionList.minOf { it.x }
        val baseY = positionList.minOf { it.y }

        val positions = positionList.map { posPair -> posPair.x to posPair.y }

        val allTiles = mutableListOf(0 to 0, 1 to 0, 0 to 1, 1 to 1).map { pair -> baseX+pair.first to baseY+pair.second }

        val missing = allTiles.filter { it !in positions }.map { pair -> pair.first-baseX to pair.second-baseY }[0]
        val orientation = when(missing){
            0 to 1 -> TOP_LEFT
            1 to 1 -> TOP_RIGHT
            0 to 0 -> BOTTOM_LEFT
            1 to 0 -> BOTTOM_RIGHT
            else -> -1
        }
        return Pair(PositionPair(baseX, baseY), orientation)

        /*// sort positionList (not necessary but can help)
        val sortedPositions = positionList.sortedWith(compareBy({ it.x }, { it.y }))

        // Identify the base tile.
        val base = sortedPositions.find { pos ->
            // Project the positions excluding the current one being considered as the base
            val others = sortedPositions - pos
            // Check if the remaining tiles share an x or y coordinate, indicating alignment
            others[0].x == others[1].x || others[0].y == others[1].y
        } ?: throw IllegalArgumentException("Invalid tile arrangement")

        // Determine orientation based on the positions of the other two tiles relative to the base
        val orientation = when {
            sortedPositions.any { it.x == base.x + 1 && it.y == base.y } &&
                    sortedPositions.any { it.x == base.x && it.y == base.y + 1 } -> TOP_LEFT

            sortedPositions.any { it.x == base.x - 1 && it.y == base.y } &&
                    sortedPositions.any { it.x == base.x && it.y == base.y + 1 } -> TOP_RIGHT

            sortedPositions.any { it.x == base.x + 1 && it.y == base.y } &&
                    sortedPositions.any { it.x == base.x && it.y == base.y - 1 } -> BOTTOM_LEFT

            sortedPositions.any { it.x == base.x - 1 && it.y == base.y } &&
                    sortedPositions.any { it.x == base.x && it.y == base.y - 1 } -> BOTTOM_RIGHT

            else -> throw IllegalArgumentException("Could not determine orientation")
        }

        return Pair(base, orientation)*/
    }

    /** Helper function that find the base of a BigExpansion positionList.
     * Returns a Base position (Pair(x,y))
     * The base is the Tile with the smallest coordinates
     */
    private fun findBaseForLargeExpansion(positionList: List<PositionPair>): PositionPair {
        // Ensure the input is valid for a large expansion
        require(positionList.size == 4) { "A large expansion must consist of exactly 4 tiles." }

        // Find the tile with the smallest x coordinate
        val minX = positionList.minOf { it.x }
        // Among tiles with the smallest x, find the one with the smallest y coordinate
        val minY = positionList.filter { it.x == minX }.minOf { it.y }

        // The tile with the smallest x and y coordinates is the base of the large expansion
        return positionList.first { it.x == minX && it.y == minY }
    }


    //TODO: Neu param from View: 0 2 3 --> bot oder normal spieler
    fun startNewHostedGame(hostPlayerName: String, hostPlayerType: Int) {
        check(hostPlayerType in 0..3) { "invalid player type provided" }
        //check(connectionState == ConnectionState.WAITING_FOR_GUESTS) { "currently not able to start a new Game." }

        val guestNames = client!!.otherPlayerName

        val playerNames = mutableListOf(hostPlayerName) + guestNames
        val playerTypes = listOf(hostPlayerType) + MutableList(guestNames.size) { ONLINE }

        //Initialize a new game
        rootService.gameStateService.initializeGame(playerNames, playerTypes.toMutableList(), null, false)

        val board = rootService.aquarettoGame.board

        val deckCards = board.mainStack + board.endOfGameStack
        val deckIDs = deckCards.map { wagonable -> wagonable.ID }

        //Construct InitGame message
        val message = InitGameMessage(
            playerNames.toList(),
            deckIDs
        )

        client?.sendGameActionMessage(message)

        rootService.gameStateService.reverseLists()

        onAllRefreshables { refreshAfterInitializeGame() }
/*
        //TODO: DETERMINE next player and update states.
        val currentPlayer = rootService.aquarettoGame.currentPlayer()

        when (currentPlayer) {
            is HotseatPlayer -> updateConnectionState(ConnectionState.WAITING_FOR_MY_TURN)
            is OnlineBGWPlayer -> updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)
            else -> throw IllegalStateException("Unhandled player type")
        }
*/
        //send message over Network

    }


    fun sendAddTileToTruck(truckId: Int) {
        // Ensure that it's my Turn
        /**require(connectionState == ConnectionState.WAITING_FOR_MY_TURN) { "this is not my turn" }
        try {
        } catch (e: Exception) {
            TODO("Not yet implemented")
        }
        **/

        // Validate the truck ID
        val sendId = wagonsSnapshot.indexOf(rootService.aquarettoGame.board.transportWagons[truckId])
        // Assuming you have a network service or client instance that allows sending messages
        val message = AddTileToTruckMessage(sendId)
        client?.sendGameActionMessage(message)
    }


    private fun prepareLists(): Triple<MutableList<AnimalTriple>, MutableList<OffspringTriple>, MutableList<WorkerTriple>> {
        val animalList: MutableList<AnimalTriple> = mutableListOf()
        val childrenList: MutableList<OffspringTriple> = mutableListOf()
        val workerList: MutableList<WorkerTriple> = mutableListOf()

        animalsToSend.forEach { animal ->
            if (animal.first == 0 && animal.second == 0) { // If placed in depot
                animalList.add(AnimalTriple(0, 0, animal.third))
            } else {
                animalList.add(AnimalTriple(animal.first, animal.second, animal.third))
            }
        }
        offspringToSend.forEach { offspring ->
            if (offspring.first == 0 && offspring.second == 0) { // If placed in depot (or any special handling)
                childrenList.add(OffspringTriple(0, 0, offspring.third))
            } else {
                childrenList.add(OffspringTriple(offspring.first, offspring.second, offspring.third))
            }
        }
        
        workersToSend.forEach { worker ->
            workerList.add(WorkerTriple(worker.first, worker.second, worker.third))
        }

        return Triple(animalList, childrenList, workerList)
    }

    fun sendTakeTruck() {
        //require(connectionState == ConnectionState.WAITING_FOR_MY_TURN) { "Expecting opponents turn" }

        //Lists Conversion
        val listsToSend = prepareLists()

        val game = rootService.aquarettoGame
        require(takenWagonId in 0..4) { "Invalid truck ID: $takenWagonId" }

        val message = TakeTruckMessage(
            takenWagonId,
            listsToSend.first.toList(),
            listsToSend.second.toList(),
            listsToSend.third.toList()
        )
        /*send message */
        client?.sendGameActionMessage(message)


        /*val nextPlayer = determineNextPlayer()
        if(nextPlayer != rootService.aquarettoGame.currentPlayer ){
            updateConnectionState(ConnectionState.WAITING_FOR_MY_TURN)
        }else{
            updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)
        }*/
        netWagon.clear()
    }
    /**
     * resets all lest an variables for next Turn, called after endTurn
     */
    fun resetLists() {
        takenWagon.clear()
        boughtCardFrom = null
        animalsToSend.clear()
        offspringToSend.clear()
        workersToSend.clear()
        takenWagonId = -1
    }

    fun sendBuyExpansion(isBigExpansion: Boolean, baseX: Int, baseY: Int, orientation: Int? = null) {
        //require(connectionState == ConnectionState.WAITING_FOR_MY_TURN) { "not my turn" }

        // Prepare the coordinates for the expansion based on its size and orientation
        val coordinates = mutableListOf<PositionPair>()


        if (isBigExpansion) {
            // Add positions for a big expansion (assuming 2x2 layout)
            coordinates.apply {
                add(PositionPair(baseX + 1, baseY))
                add(PositionPair(baseX, baseY + 1))
                add(PositionPair(baseX + 1, baseY + 1))
                coordinates.add(PositionPair(baseX, baseY))
            }
        } else {
            // Calculate and add positions for a small expansion based on its orientation
            when (orientation) {
                //Add tiles to the top and to left of the base
                TOP_LEFT -> {
                    coordinates.add(PositionPair(baseX+1, baseY + 1))
                    coordinates.add(PositionPair(baseX + 1, baseY))
                    coordinates.add(PositionPair(baseX, baseY))
                }
                //Add tiles to the top and to right of the base
                TOP_RIGHT -> {
                    coordinates.add(PositionPair(baseX + 1, baseY))
                    coordinates.add(PositionPair(baseX, baseY + 1))
                    coordinates.add(PositionPair(baseX, baseY))
                }
                //Add tiles to the bottom and to right of the base
                BOTTOM_RIGHT -> {
                    coordinates.add(PositionPair(baseX, baseY + 1))
                    coordinates.add(PositionPair(baseX + 1, baseY + 1))
                    coordinates.add(PositionPair(baseX, baseY))
                }
                //Add tiles to the bottom and to left of the base
                BOTTOM_LEFT -> {
                    coordinates.add(PositionPair(baseX + 1, baseY + 1))
                    coordinates.add(PositionPair(baseX + 1, baseY))
                    coordinates.add(PositionPair(baseX, baseY+1))
                }

                else -> throw IllegalArgumentException("Invalid orientation: $orientation")
            }
        }

        // Construct the BuyExpansionMessage
        val message = BuyExpansionMessage(coordinates)

        // Send the message
        client?.sendGameActionMessage(message)
    }


    fun sendMoveCoworker(oldPosition: Pair<Int, Int>, newPosition: Pair<Int, Int>) {
        //require(connectionState == ConnectionState.WAITING_FOR_MY_TURN) { "Not my turn." }

        val oldWorkerType = when (oldPosition) {
            MANAGER -> JobEnum.MANAGER
            CASHIER -> JobEnum.CASHIER
            NURSE -> JobEnum.KEEPER
            else -> JobEnum.TRAINER
        }

        val newWorkerType = when (newPosition) {
            MANAGER -> JobEnum.MANAGER
            CASHIER -> JobEnum.CASHIER
            NURSE -> JobEnum.KEEPER
            else -> JobEnum.TRAINER
        }

        val oldWorkerTriple = WorkerTriple(oldPosition.first, oldPosition.second, oldWorkerType)
        val newWorkerTriple = WorkerTriple(newPosition.first, newPosition.second, newWorkerType)

        val message = MoveCoworkerMessage(oldWorkerTriple, newWorkerTriple)

        client?.sendGameActionMessage(message)

        //rootService.networkService.updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)
    }


    fun sendMoveTile(playerName: String, x: Int, y: Int) {
        //require(connectionState == ConnectionState.WAITING_FOR_MY_TURN) { "its not my turn" }
        val listsToSend = prepareLists()

        /*create MoveTileMessage */
        val message = MoveTileMessage(
            playerName,
            PositionPair(x, y),
            listsToSend.second.toList(),
            listsToSend.third.toList()
        )
        /*send message */
        client?.sendGameActionMessage(message)


        //resetLists()
    }


    fun sendDiscard() {
        //require(connectionState == ConnectionState.WAITING_FOR_MY_TURN) { "not my turn" }

        //create TakeTruckMessage
        val message = DiscardMessage()
        //send message
        client?.sendGameActionMessage(message)
    }


}
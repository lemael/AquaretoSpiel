package service

import CSVMapper
import edu.udo.cs.sopra.ntf.JobEnum
import entity.*
import entity.commands.*
import entity.players.OnlineBGWPlayer
import view.*

/**
 * This class deals with all game interactions that involve a move of a player.
 *
 * All methods operate similarly:
 * 1) check conditions
 * 2) create a [Command] that contains the respective action
 *      including refreshes
 * 3) apply this via the [GameStateService.executeCommand] function
 */
class PlayerActionService(val root: RootService) : AbstractRefreshingService() {


    /**
     * Takes the top card from the [Board.mainStack] and puts it on the specified wagon.
     * In the event that the [Board.mainStack] is empty, a Card is taken from [Board.endOfGameStack], as described by the game rules.
     *
     * Calls [Refreshable.refreshAfterWagonsChanged]
     *
     * @throws IllegalArgumentException when the given wagon is invalid (i.e. not existent or full)
     */
    fun putCardOnWagon(wagonIndex: Int) {
        val wagons = root.aquarettoGame.board.transportWagons
        val board = root.aquarettoGame.board

        require(wagonIndex in board.transportWagons.indices) { "the given wagon $wagonIndex does not exist" }
        require(wagons[wagonIndex].any { it is Empty }) { "the selected wagon is full" }

        val newCommand = PutCardOnWagonCommand(root.aquarettoGame.undos.size, board, wagonIndex)

        root.gameStateService.executeCommand(newCommand)

        val currentPlayer = root.aquarettoGame.currentPlayer()
        val shouldSendMessage = root.networkService.isOnlineSession && currentPlayer !is OnlineBGWPlayer

        if (shouldSendMessage) {
            root.networkService.sendAddTileToTruck(wagonIndex)
        }

    }

    /**
     * Takes the specified wagon and assigns it to the current [Player].
     * As [Player.cardsToPlay] can only contain [Parkable]s,
     * all [CoinCard]s should be directly given to the players [Player.numCoins].
     *
     * Calls [Refreshable.refreshAfterTakeWagon] , [Refreshable.refreshAfterGainedCard]
     * and [Refreshable.refreshAfterChangedBalance] if the number of coins was changed
     *
     * @throws IllegalArgumentException when the given wagon is invalid (i.e. not existent or empty)
     */
    fun takeWagon(wagonIndex: Int) {
        val wagons = root.aquarettoGame.board.transportWagons
        val currentPlayer = root.aquarettoGame.currentPlayer()

        require(!currentPlayer.tookWagon)
        require(wagonIndex in wagons.indices) { "the given wagon $wagonIndex does not exist" }
        require(wagons[wagonIndex].any { it !is Empty }) { "the selected wagon is empty" }

        root.networkService.netWagon = wagons[wagonIndex]

        val newCommand =
            TakeWagonCommand(root.aquarettoGame.undos.size, wagonIndex, root.aquarettoGame.board, currentPlayer)

        //relevant for network -> index of taken tile
        root.networkService.takenWagon = wagons[wagonIndex]
        root.networkService.takenWagonId = root.networkService.wagonsSnapshot.indexOf(wagons[wagonIndex])

        root.gameStateService.executeCommand(newCommand)
    }

    /**
     * Places the given [Depotable] on the specified position in the [Waterpark] by adjusting [Waterpark.map] and the respective [Pool].
     *
     * Calls [Refreshable.refreshAfterParkChanged] and [Refreshable.refreshAfterChangedBalance] if the number of coins was changed
     *
     * @throws IllegalArgumentException when the animal can not be placed on the specified space
     */
    fun animalToPark(animal: Depotable, x: Int, y: Int) = animalToPark(animal, x to y)

    /**
     * Places the given [Depotable] on the specified position in the [Waterpark] by adjusting [Waterpark.map] and the respective [Pool].
     *
     * Calls [Refreshable.refreshAfterParkChanged] and [Refreshable.refreshAfterChangedBalance] if the number of coins was changed
     *
     * @throws IllegalArgumentException when the animal can not be placed on the specified space
     */
    fun animalToPark(animal: Depotable, position: Pair<Int, Int>) {
        println("animal to park")
        val map = root.aquarettoGame.currentPlayer().park.map
        // conditions for the exact position
        require(position in map) { "position $position does not exist" }
        require(map[position] is Empty) { "the park has to be empty on position $position, currently ${map[position]}" }
        // check neighbours
        val neighbours = listOf(
            position.first + 1 to position.second,
            position.first - 1 to position.second,
            position.first to position.second + 1,
            position.first to position.second - 1
        )
        neighbours.forEach {
            if (it in map) {
                val content = map[it]
                if (content is Depotable){
                    require(content.type == animal.type) { "position $position borders a different animal type at $it" }
                }
            }
        }
        // check pool conditions
        var actualPool: Pool? = null
        var newPool = false
        val pools = root.aquarettoGame.currentPlayer().park.pools
        pools.forEach { pool ->
            if (pool.type == animal.type) {
                require(neighbours.any { it in pool.members }) { "there is a pool of the correct type but it does not border the position" }
                actualPool = pool
            }
        }
        if (actualPool == null) {
            require(pools.size < root.aquarettoGame.currentPlayer().park.maxPools) { "can not add a new pool as maximum has been reached" }
            actualPool = Pool(animal.type)
            newPool = true
        }
        println("got through requirements")
        //network
        val currentPlayer = root.aquarettoGame.currentPlayer()
        if (root.networkService.isOnlineSession) {
            if (animal is Animal) root.networkService.animalsToSend.add(
                Triple(
                    position.first,
                    position.second,
                    root.networkService.netWagon.indexOf(animal)
                )
            )
            if (animal is Baby) root.networkService.offspringToSend.add(
                Triple(
                    position.first,
                    position.second,
                    CSVMapper.mapToInt(animal)
                )
            )
        }
        println("animals: ${root.networkService.animalsToSend}, babies: ${root.networkService.offspringToSend}")

        val command =
            AnimalToParkCommand(root.aquarettoGame.undos.size, animal, position, currentPlayer, actualPool!!, newPool)

        root.gameStateService.executeCommand(command)
    }

    /**
     * Places the given [Depotable] on top of the [Player.depot] stack.
     *
     * Calls [Refreshable.refreshAfterDepotChanged]
     */
    fun animalToDepot(animal: Depotable) {
        // no conditions need to be checked
        val command = AnimalToDepotCommand(root.aquarettoGame.undos.size, animal, root.aquarettoGame.currentPlayer())
        root.gameStateService.executeCommand(command)
        //network
        val currentPlayer = root.aquarettoGame.currentPlayer()
        if (root.networkService.isOnlineSession) {
            if (animal is Animal) root.networkService.animalsToSend.add(
                Triple(0, 0, root.networkService.netWagon.indexOf(animal))
            )
            if (animal is Baby) root.networkService.offspringToSend.add(
                Triple(0, 0, CSVMapper.mapToInt(animal))
            )
        }
    }

    /**
     * Moves the top animal from the [Player.depot] stack to the specified position in the [Waterpark].
     *
     * Moves 1 (one) coin from the [Player] to the bank, as this is a MoneyAction.
     *
     * Calls [Refreshable.refreshAfterDepotToPark] and [Refreshable.refreshAfterChangedBalance]
     */
    fun depotToPark() {
        val player = root.aquarettoGame.currentPlayer()

        require(player.depot.isNotEmpty()) { "can not buy card from an empty depot." }
        require(player.numCoins > 0) { "player can not afford depotToPark: actual: ${player.numCoins} required 1" }

        //network
        root.networkService.boughtCardFrom = player.name


        val command = DepotToParkCommand(root.aquarettoGame.undos.size, player)
        root.gameStateService.executeCommand(command)
    }

    /**
     * Moves a worker from the specified old job to the specified new job.
     *
     * For adding a worker, set [old] to be null
     *
     * Otherwise, referencing a job is intended as follows
     * 1) cashier, nurse and manager via their constants [CASHIER], [NURSE], [MANAGER]
     * 2) trainer via the position he worked at or is intended to work now
     *
     * Moves 1 (one) coin from the [Player] to the bank, as this is a MoneyAction.
     *
     * Calls [Refreshable.refreshAfterRelocateWorker] and [Refreshable.refreshAfterChangedBalance]
     *
     * @throws IllegalArgumentException when the old position did not have someone working there
     * @throws IllegalArgumentException when the new position is already full
     */
    fun relocateWorker(old: Pair<Int, Int>?, new: Pair<Int, Int>) {
        val player = root.aquarettoGame.currentPlayer()

        require(old==null || player.numCoins > 0) { "player can not afford relocateWorker: actual: ${player.numCoins} required 1" }
        // check the old worker for existence
        when (old) {
            null -> {}
            CASHIER -> require(player.numCashiers > 0) { "can not relocate cashier, as the player does not have one" }
            NURSE -> require(player.numNurses > 0) { "can not relocate nurse, as the player does not have one" }
            MANAGER -> require(player.hasManager) { "can not relocate manager, as the player does not have one" }
            else -> {
                require(old in player.park.map) { "can not relocate worker from illegal position: $old" }
                require(player.park.map[old] is Worker) { "given location does not contain a trainer." }
            }
        }
        // check the new worker for available spot
        when (new) {
            CASHIER -> require(player.numCashiers < 2) { "can not employ cashier, as the player already has 2" }
            NURSE -> require(player.numNurses < 2) { "can not employ nurse, as the player already has 2" }
            MANAGER -> require(!player.hasManager) { "can not employ manager, as the player does already has one" }
            else -> {
                require(new in player.park.map) { "can not employ worker on an illegal position: $new" }
                require(player.park.map[new] is Empty) { "given location already contains something." }
            }
        }

        val command = RelocateWorkerCommand(root.aquarettoGame.undos.size, old, new, player)
        root.gameStateService.executeCommand(command)

        //TODO CHECK
        val shouldSendMessage = root.networkService.isOnlineSession && player !is OnlineBGWPlayer
        if (shouldSendMessage) {
            //when relocating
            if (old != null) {
                root.networkService.sendMoveCoworker(old, new)
            }
            //when new
            if (old == null) {
                when (new) {
                    MANAGER -> root.networkService.workersToSend.add(Triple(new.first, new.second, JobEnum.MANAGER))
                    NURSE -> root.networkService.workersToSend.add(Triple(new.first, new.second, JobEnum.KEEPER))
                    CASHIER -> root.networkService.workersToSend.add(Triple(new.first, new.second, JobEnum.CASHIER))
                    else -> root.networkService.workersToSend.add(Triple(new.first, new.second, JobEnum.TRAINER))
                }

            }
        }
    }

    /**
     * Buys a card from the specified [Player]. Moves their top [Player.depot] card to the own [Player.cardsToPlay] list.
     *
     * Moves 1 (one) coin from the [Player] to the bank and 1 (one) coin to the other player, as this is a MoneyAction.
     *
     * Calls [Refreshable.refreshAfterBuyCard] and [Refreshable.refreshAfterChangedBalance]
     */
    fun buyCard(buyFromPlayer: Player) {
        val currentPlayer = root.aquarettoGame.currentPlayer()

        require(currentPlayer.numCoins >= 2)
        { "The player does not have enough coins for this purchase. currently: ${currentPlayer.numCoins}" }

        require(buyFromPlayer.depot.size >= 1)
        { "The chosen player does not have a card in the depot." }

        // create the command
        val newCommand =
            BuyCardCommand(root.aquarettoGame.undos.size, currentPlayer = currentPlayer, buyFromPlayer = buyFromPlayer)

        // give the command to the GameStateService for execution
        root.gameStateService.executeCommand(newCommand)

        //network
        root.networkService.boughtCardFrom = buyFromPlayer.name
    }

    /**
     * Deletes the top card of the [Player.depot]
     *
     * Moves 2 (two) coins from the [Player] to the bank, as this is a MoneyAction.
     *
     * Calls [Refreshable.refreshAfterSellCard] and [Refreshable.refreshAfterChangedBalance]
     */
    fun sellCard() {
        val currentPlayer = root.aquarettoGame.currentPlayer()

        require(currentPlayer.numCoins >= 2)
        { "The player does not have enough coins for this transaction." }

        require(currentPlayer.depot.size >= 1)
        { "The player does not have a card in the depot." }

        // create the command
        val newCommand = SellCardCommand(root.aquarettoGame.undos.size, player = currentPlayer)

        // give the command to the GameStateService for execution
        root.gameStateService.executeCommand(newCommand)

        //network
        val shouldSendMessage = root.networkService.isOnlineSession && currentPlayer !is OnlineBGWPlayer
        if (shouldSendMessage) {
            root.networkService.sendDiscard()
        }
    }

    /**
     * adds a small extension to the [Waterpark] at the given location by expanding [Waterpark.map].
     * Stores the data in [Waterpark.smallExtensions] for potential later use.
     *
     * For [orientation] please use [TOP_LEFT], [TOP_RIGHT], [BOTTOM_LEFT] and [BOTTOM_RIGHT]
     *
     * Moves 1 (one) coin from the [Player] to the bank, as this is a MoneyAction.
     *
     * Calls [Refreshable.refreshAfterExtension] and [Refreshable.refreshAfterChangedBalance]
     *
     * @throws IllegalStateException when the maximum number of small extensions is reached.
     * @throws IllegalArgumentException when the position or orientation is not correct
     */
    fun addSmallExtension(base: Pair<Int, Int>, orientation: Int) {
        val currPlayer = root.aquarettoGame.currentPlayer()
        val parkMap = currPlayer.park.map
        check(currPlayer.park.smallExtensions.size < 2) { "maximum number of small extensions is reached" }
        check(currPlayer.numCoins > 0) { "player can not afford a small extension. cost: 1, has: ${currPlayer.numCoins}" }
        // check that all tiles that are added do not yet exist
        val offset = mutableListOf(0 to 1, 1 to 1, 0 to 0, 1 to 0)
        when (orientation) {
            TOP_LEFT -> offset.remove(0 to 1)
            TOP_RIGHT -> offset.remove(1 to 1)
            BOTTOM_LEFT -> offset.remove(0 to 0)
            BOTTOM_RIGHT -> offset.remove(1 to 0)
            else -> require(false)
            { "orientation is $orientation which does not match any of the ones defined in constants.kt" }
        }
        val tiles = offset.map { (x, y) -> (x + base.first to y + base.second) }
        tiles.forEach { require(it !in parkMap) { "extension overlaps with existing tiles" } }
        // check that the added tiles are not the special tiles (0,0), (1,0), (0,1)
        require(MANAGER !in tiles) { "extension overlaps the (0,0) manager tile" }
        require(NURSE !in tiles) { "extension overlaps the (1,0) nurse tile" }
        require(CASHIER !in tiles) { "extension overlaps the (0,1) cashier tile" }
        // check that the extension is adjacent to the existing board
        val neighbours = mutableListOf<Pair<Int, Int>>()
        tiles.forEach { neighbours.addAll(parkMap.directNeighbours(it)) }
        require(neighbours.any { it in parkMap }) { "the extension does not border any existing tile" }

        val newCommand = AddSmallExtensionCommand(
            root.aquarettoGame.undos.size,
            tiles,
            currPlayer,
            Triple(base.first, base.second, orientation)
        )

        root.gameStateService.executeCommand(newCommand)

        val currentPlayer = root.aquarettoGame.currentPlayer()
        val shouldSendMessage =
             root.networkService.isOnlineSession && currentPlayer !is OnlineBGWPlayer
        //when add smallExtension
        if (shouldSendMessage ) {
            root.networkService.sendBuyExpansion(false,base.first,base.second,orientation)
        }
    }

    /**
     * adds a large extension to the [Waterpark] at the given location by expanding [Waterpark.map].
     * Stores the data in [Waterpark.largeExtensions] for potential later use.
     *
     * Moves 2 (two) coins from the [Player] to the bank, as this is a MoneyAction.
     *
     * Calls [Refreshable.refreshAfterExtension] and [Refreshable.refreshAfterChangedBalance]
     *
     * @param base the bottom left coordinate of the extension
     *
     * @throws IllegalArgumentException when the maximum number of large extensions is reached.
     * @throws IllegalArgumentException when the position is not correct
     */
    fun addLargeExtension(base: Pair<Int, Int>) {
        val currPlayer = root.aquarettoGame.currentPlayer()
        val parkMap = currPlayer.park.map
        check(currPlayer.park.largeExtensions.size < 2) { "maximum number of large extensions is reached" }
        check(currPlayer.numCoins > 1) { "player can not afford a large extension. cost: 2, has: ${currPlayer.numCoins}" }
        // check that all tiles that are added do not yet exist
        val tiles = mutableListOf(0 to 1, 1 to 1, 0 to 0, 1 to 0).map { (x, y) -> (x + base.first to y + base.second) }
        // check that the added tiles are not the special tiles (0,0), (1,0), (0,1)
        require(MANAGER !in tiles) { "extension overlaps the (0,0) manager tile" }
        require(NURSE !in tiles) { "extension overlaps the (1,0) nurse tile" }
        require(CASHIER !in tiles) { "extension overlaps the (0,1) cashier tile" }
        // check if overlapping with park
        tiles.forEach { require(it !in parkMap) { "extension overlaps with existing tiles" } }
        // check that the extension is adjacent to the existing board
        val neighbours = mutableListOf<Pair<Int, Int>>()
        tiles.forEach { neighbours.addAll(parkMap.directNeighbours(it)) }
        require(neighbours.any { it in parkMap }) { "the extension does not border any existing tile" }

        val newCommand =
            AddLargeExtensionCommand(root.aquarettoGame.undos.size, tiles, currPlayer, base.first to base.second)

        root.gameStateService.executeCommand(newCommand)


        val currentPlayer = root.aquarettoGame.currentPlayer()
        val shouldSendMessage =
            root.networkService.isOnlineSession && currentPlayer !is OnlineBGWPlayer
        //when add smallExtension
        if (shouldSendMessage ) {
            root.networkService.sendBuyExpansion(true,base.first,base.second)
        }

    }


}

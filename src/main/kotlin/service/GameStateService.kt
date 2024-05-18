package service

import CSVLoader
import CSVMapper
import com.fasterxml.jackson.module.kotlin.readValue

import entity.*
import entity.commands.EndTurnCommand
import entity.players.*
import service.network.ConnectionState

import view.Refreshable
import java.io.File
import kotlin.random.Random
import kotlin.random.nextInt


class GameStateService(val root: RootService): AbstractRefreshingService(){

    /**
     * Initializes the entire game.
     *
     * @param names the player-names
     * @param types specifies the type of player, use [HOTSEAT], [ONLINE], [DUMB], [SMART]
     *
     * Calls [Refreshable.refreshAfterInitializeGame]
     */
    fun initializeGame(names: List<String>, types: MutableList<Int>,  deck: MutableList<Wagonable> ? = null, sendRefresh: Boolean = true) {
        require(names.size == types.size)
        println("received types: $types")
        // create board
        var mainDeck : MutableList<Wagonable> = mutableListOf()
        mainDeck = if(deck.isNullOrEmpty()) createCards(names.size).apply { shuffle() } else deck
        println("raw deck: ${mainDeck.map { it.ID }}")
        val endOfGameDeck = mutableListOf<Wagonable>()
        for(i in mainDeck.size-14 .. mainDeck.size) { endOfGameDeck.add(0, mainDeck.removeLast()) }
        val wagons = mutableListOf<MutableList<Wagonable>>()
        // initialize wagons
        if(names.size == 2) {
            wagons.add(mutableListOf(Empty()))
            wagons.add(mutableListOf(Empty(),Empty()))
            wagons.add(mutableListOf(Empty(), Empty(),Empty()))
        } else {
            repeat(names.size) { wagons.add(mutableListOf(Empty(), Empty(), Empty())) }
        }

        root.networkService.wagonsSnapshot = wagons
        val board = Board(0).apply {
            mainStack = mainDeck
            endOfGameStack = endOfGameDeck
            transportWagons = wagons
        }

        // create players
        val players = mutableListOf<Player>()
        for (i in names.indices) {
            when (types[i]) {
                HOTSEAT -> players.add(HotseatPlayer(names[i]))
                ONLINE -> players.add(OnlineBGWPlayer(names[i]))
                DUMB -> players.add(DumbBot(names[i]))
                SMART -> players.add(SmartBot(names[i]))
                else -> require(false) { "type of player is not of known format: was ${types[i]}" }
            }
        }

        // create game
        val game = Aquaretto(players, board)
        root.aquarettoGame = game

        println(game.board.mainStack.map { it.ID })
        println(game.board.endOfGameStack.map { it.ID })

        if(sendRefresh)
            onAllRefreshables { refreshAfterInitializeGame() }
    }

    fun createCards(numPlayers: Int): MutableList<Wagonable> {
        // randomly select the used animals
        val mainAnimals = mutableListOf(AnimalType.DOLPHIN, AnimalType.ORCA, AnimalType.SEALION)
        val sideAnimals = mutableListOf(
            AnimalType.HIPPO,
            AnimalType.TURTLE,
            AnimalType.CROCODILE,
            AnimalType.ICEBEAR,
            AnimalType.PENGUIN
        )
        repeat(5 - numPlayers) { sideAnimals.removeAt(Random.nextInt(0 until numPlayers)) }

        val cards = mutableListOf<Wagonable>()
        val idOffset = mapOf<AnimalType, Int>(
            AnimalType.DOLPHIN to 11,
            AnimalType.ORCA to 24,
            AnimalType.SEALION to 37,
            AnimalType.TURTLE to 50,
            AnimalType.HIPPO to 63,
            AnimalType.CROCODILE to 76,
            AnimalType.PENGUIN to 89,
            AnimalType.ICEBEAR to 102
        )
        // create animals for the main types
        mainAnimals.forEach { type ->
            for(i in 0..1) { cards.add(Animal(type = type, gender = AnimalGender.MALE, ID = idOffset[type]!!+2+i)) }
            for(i in 0..1) { cards.add(Animal(type = type, gender = AnimalGender.FEMALE, ID = idOffset[type]!!+i)) }
            for(i in 0..1) { cards.add(Animal(type = type, isTrainable = false, ID = idOffset[type]!!+9+i)) }
            for(i in 0..4) { cards.add(Animal(type = type, ID = idOffset[type]!!+4+i)) }
        }
        sideAnimals.forEach { type ->
            for(i in 0..1) { cards.add(Animal(type = type, isTrainable = false, gender = AnimalGender.MALE, ID = idOffset[type]!!+2+i)) }
            for(i in 0..1) { cards.add(Animal(type = type, isTrainable = false, gender = AnimalGender.FEMALE, ID = idOffset[type]!!+i)) }
            for(i in 0..5) { cards.add(Animal(type = type, isTrainable = false, isFish = true, ID = idOffset[type]!!+5+i)) }
            cards.add(Animal(type = type, isTrainable = false, ID = idOffset[type]!!+4))
        }
        for(i in 0..9) { cards.add(CoinCard(ID = 1+i)) }

        return cards
    }
    fun reverseLists(){
        root.aquarettoGame.board.mainStack.reverse()
        root.aquarettoGame.board.endOfGameStack.reverse()
        println(root.aquarettoGame.board.mainStack.map { it.ID })
        println(root.aquarettoGame.board.endOfGameStack.map { it.ID })
    }

    /**
     * loads the stored game pointed to by the file name
     *
     * assumes the file exists and describes a valid game
     */
    fun loadGame(fileName: String = "aquaSafe.json"):Aquaretto {
        return mapper.readValue(fileName)
    }

    /**
     * writes the current safe state to the given file name
     *
     * files have to start with "aquaSafe" to prevent accidental file overwriting
     */
    fun safeGame(fileName: String = "aquaSafe.json") {
        require(fileName.startsWith("aquaSafe")){"file names have to start with \"aquaSafe\" to avoid overwriting important files"}
        mapper.writeValue(File(fileName), root.aquarettoGame)
    }

    /**
     * executes the given [Command] and adds it to [Aquaretto.undos]
     */
    fun executeCommand(command: Command) {
        super.refreshables.forEach { command.addRefreshable(it) }
        val game = root.aquarettoGame
        game.redos.clear()
        game.undos.add(command)
        command.apply()
    }

    /**
     * reverts the most recent [Command] with its [Command.revert], removes it from the [Aquaretto.undos] list
     * and adds it to the [Aquaretto.redos] list as the most recent element
     */
    fun revertLastCommand() {
        val game = root.aquarettoGame
        require(game.undos.isNotEmpty()) { "Undos are empty" }
        val currentCommand = game.undos.last()  //handled command
        currentCommand.revert()                 //revert
        game.redos.add(currentCommand)          //add to redos
        game.undos.removeLast()                 //remove from undos
    }

    /**
     * executes the most recent [Command] with its [Command.apply], removes it from the [Aquaretto.redos] list
     * and adds it to the [Aquaretto.undos] list as the most recent element
     */
    fun applyLastReverted() {
        val game = root.aquarettoGame
        require(game.redos.isNotEmpty()) { "Redos are  empty" }
        val currentCommand = game.redos.last()  //handled command
        currentCommand.apply()                  //apply
        game.undos.add(currentCommand)          //add to undos
        game.redos.removeLast()                 //remove from redos
    }

    fun endTurn(){
        onAllRefreshables { refreshBeforeEndTurn() }
    }
    /**
     * Ends the turn of the current player.
     *
     * Calls [Refreshable.refreshAfterEndTurn]
     */
    fun updateEndTurn() {
        val players = root.aquarettoGame.players
        val currentPlayer = root.aquarettoGame.currentPlayer()
        val network = root.networkService

        //network
        val shouldSendMessage = network.isOnlineSession && currentPlayer !is OnlineBGWPlayer
        //if wagon was taken
        if (shouldSendMessage && currentPlayer.tookWagon){
            network.sendTakeTruck()
        }
        //if tile was moved
        if(shouldSendMessage && network.boughtCardFrom != null){
            if(network.animalsToSend.isEmpty())
                network.sendMoveTile(network.boughtCardFrom!!, network.offspringToSend.first().first, network.offspringToSend.first().second)
            else
                network.sendMoveTile(network.boughtCardFrom!!, network.animalsToSend.first().first, network.animalsToSend.first().second)
        }
        println("resetting lists")
        network.resetLists()

        val endGame =
            when(root.aquarettoGame.players.size){
                2 -> root.aquarettoGame.board.transportWagons.size == 1
                else -> root.aquarettoGame.board.transportWagons.size == 0
            }
        // create the command
        if(endGame && root.aquarettoGame.board.endOfGameStack.size < 15)
            root.gameStateService.endGame()
        else{
            val newCommand = EndTurnCommand(root.aquarettoGame.undos.size, root.aquarettoGame, root)
            // give the command to the GameStateService for execution
            root.gameStateService.executeCommand(newCommand)
        }
    }

    /**
     * Ends the game and evaluates every player's scores
     *
     * Calls [Refreshable.refreshAfterEndGame]
     */
    fun endGame() {
        val results = mutableListOf<Triple<Player, Int, Int>>()
        root.aquarettoGame.players.forEach {
            results.add(Triple(it, evaluatePlayer(it).first, evaluatePlayer(it).second))
        }
        results.sortWith(compareBy({-it.second}, {-it.third}))
        onAllRefreshables { refreshAfterEndGame(results) }
    }

    /**
     * calculates the score of the given player
     */
    fun evaluatePlayer(player: Player): Pair<Int, Int> {
        val map = player.park.map
        var score = 0
        // each animal and baby give one point
        score += map.filter { it.value is Depotable }.size
        // point for each coin*cashier
        score += player.numCoins * player.numCashiers
        // points for each fish*nurse
        score += map.filter {
            val animal = it.value
            (animal is Animal && animal.isFish)
        }.size * player.numNurses
        // points for each trainable neighbour of a trainer
        val trainers = map.filter { it.value is Worker }.map { it.key }
        trainers.forEach { neigh ->
            score += map.allNeighbours(neigh).filter {
                val animal = map[it]
                animal is Animal && animal.isTrainable
            }.size
        }
        // reduce 2 points for each animal type in depot (1 with manager)
        score -= player.depot.groupBy { it.type }.size * if (player.hasManager) 1 else 2
        return score to player.numCoins
    }

}

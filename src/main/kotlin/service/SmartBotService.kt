package service

import entity.*
import entity.players.SmartBot
import entity.AnimalType
import entity.Depotable
import entity.Wagonable
import view.Refreshable

/**
 * Structure of strategy
 *
 * The first 4 animals drawn are the target animal for the rest of the game
 * they get placed on (4,2) (2,4) (0,2) (2,0)
 * to get the maximum out of two trainers on (1,3) and (3,1)
 * extensions will be placed when the pool reached a size 3 or 4 for the large pool
 *
 *
 * choose move in this order when valid
 *
 * 1. test before if a given animal has no space
 * 2. place card from depot if type is one of mine
 * 3. buy card from other player if type is one of mine and needed gender for baby (if i have enough coins)
 *      if chosen animal has no space extend pool if one pool uses all of its space
 * 4. evaluate best truck and take it
 *
 * priority for worker
 * * trainers on (1,3) and (3,1)
 * * manager
 * * 2 cashiers
 * * 2 nurses
 *
 * priority for placing animals
 *  * place (2,4) expand around
 *  * (2,3)(1,4)(3,4) extension (2,5) ...
 *  *
 *  * place (4,2) expand around
 *  * (3,2)(4,1)(4,3) extension (5,2) ...
 *  *
 *  * place (2,0) expand around
 *  * (2,1)(3,0) extension (2,-1) ...
 *  *
 *  * place (0,2) expand around
 *  * (1,2)(0,3) extension (-1,2) ....
 *
 *  expected max extended board
 *                             [1 to 6        ]
 *                             [1 to 5, 2 to 5]
 *                              1 to 4, 2 to 4, 3 to 4,
 *  [-2 to 3, -1 to 3], 0 to 3, 1 to 3, 2 to 3, 3 to 3, 4 to 3, [5 to 3, 6 to 3]
 *  [-2 to 2, -1 to 2], 0 to 2, 1 to 2, 2 to 2, 3 to 2, 4 to 2, [5 to 2        ]
 *                              1 to 1, 2 to 1, 3 to 1, 4 to 1,
 *                                      2 to 0, 3 to 0
 *                                     [2 to-1, 3 to-1]
 *                                     [2 to-2. 3 to-2]
 *
 */
class SmartBotService(private val rootService: RootService) : Refreshable, AbstractRefreshingService() {
    private val aiHelperService = rootService.aiHelperService
    private val playerService = rootService.playerActionService

    private lateinit var player: Player
    private lateinit var board: Board
    private lateinit var game: Aquaretto

    private val poolLeft = listOf(0 to 2, 1 to 2, 0 to 3, -1 to 2, -1 to 3, -2 to 3, -2 to 2)
    private val poolBottom = listOf(2 to 0, 2 to 1, 3 to 0, 2 to -1, 3 to -1, 2 to -2, 3 to -2)
    private val poolTop = listOf(2 to 4, 2 to 3, 1 to 4, 3 to 4, 2 to 5, 1 to 5, 1 to 6)
    private val poolRight = listOf(4 to 2, 3 to 2, 4 to 1, 4 to 3, 5 to 2, 5 to 3, 6 to 3)
    private val workers = listOf(MANAGER, CASHIER, NURSE, 1 to 3, 3 to 3, 3 to 1,1 to 1, 2 to 2)
    private val pools = listOf(poolLeft, poolBottom, poolTop, poolRight)

    /**
     * Initializes the variables after game is initialized
     */
    override fun refreshAfterInitializeGame() {
        board = rootService.aquarettoGame.board
        game = rootService.aquarettoGame
        player = rootService.aquarettoGame.currentPlayer()

        if (player !is SmartBot) return
        chooseMoves()
        rootService.gameStateService.endTurn()
    }

    /**
     * Calls chooseMoves when the current player is a SmartBot
     */
    override fun refreshAfterEndTurn(isReverting: Boolean, isNewRound: Boolean) {
        player = rootService.aquarettoGame.currentPlayer()

        if (isReverting || player !is SmartBot) return
        chooseMoves()
        rootService.gameStateService.endTurn()
    }

    /**
     * Calls placeWorker when a worker is gained by a SmartBot
     */
    override fun refreshAfterGainedWorker(isReverting: Boolean) {
        player = rootService.aquarettoGame.currentPlayer()

        if (isReverting || player !is SmartBot) return
        playerService.relocateWorker(null, bestWorker())
    }

    /**
     * Calls placeCard until cardsToPlay is empty
     */
    override fun refreshAfterGainedCard(isReverting: Boolean) {
        player = rootService.aquarettoGame.currentPlayer()

        if (isReverting || player !is SmartBot) return
        println("gained a card")
        while (player.cardsToPlay.isNotEmpty()) {
            placeCard(player.cardsToPlay.first())
        }
    }

    /**
     * chooses a move from determent priority list
     */
    private fun chooseMoves() {
        if ((board.mainStack.size < 10 && player.numCashiers == 0) || (board.mainStack.size > 10 && player.numCashiers < 2)) {
            //Prio 1
            player.park.pools.forEach() { if (addExtension(it)) return }

            //Prio 2
            if (player.depot.size > 0 && isActiveType(player.depot.last())) {
                if (player.numCoins >= 1 && uniqueTypeOnDepot(player) && hasSpace(player.depot.last().type)) {
                    println("Bot is playing depot to park")
                    playerService.depotToPark()
                    return
                }
            }

            //Prio 3
            game.players.forEach() {
                if (it == player) return@forEach
                if (it.depot.size > 0 && isActiveType(it.depot.last())) {
                    if (player.numCoins >= 2 && !uniqueTypeOnDepot(player) && hasSpace(it.depot.last().type)) {
                        println("Bot is playing buy card")
                        playerService.buyCard(it)
                        return
                    }
                }
            }
        }
        //guaranteed that a move is made with if all are empty card is drawn and if one is not empty card is taken

        //Prio 4
        //only if no wagon is empty -> one card on any wagon in not type Empty
        if (board.transportWagons.any() { wagon -> wagon.any() { it !is Empty } }) {
            val bestWagon = bestWagon()

            if (bestWagon != null) {
                println("Bot is playing take wagon")
                playerService.takeWagon(bestWagon)
                return
            }
        }

        //Prio 5
        placeTileOnWagon()
        //playerService.putCardOnWagon(board.transportWagons.indexOfFirst() { wagon -> wagon.any() { it is Empty } }) // first wagon with an Empty() spacer
    }

    private fun hasSpace(animalType: AnimalType): Boolean {
        val pool = pools.find { pool ->
            val tile = player.park.map[pool[0]]
            return tile is Depotable && tile.type == animalType
        }

        return pool?.any { tile -> player.park.map[tile] is Empty } ?: false
    }

    /**
     * Adds its extension to one of the four pools if it has reached its max capacity
     */
    private fun addExtension(it: Pool): Boolean {
        //if (player.park.pools.size <= player.park.maxPools) return false
        if (player.numCoins < 1) return false
        if (it.members.size == 4) {
            if (it.members.contains(poolTop.first())) {
                if (!player.park.map.contains(1 to 5)) {
                    println("Bot is playing small extension")
                    playerService.addSmallExtension(1 to 5, TOP_RIGHT)
                    return true
                }
            }
            if (it.members.contains(poolRight.first())) {
                if (!player.park.map.contains(5 to 2)) {
                    println("Bot is playing small extension")
                    playerService.addSmallExtension(5 to 2, BOTTOM_RIGHT)
                    return true
                }
            }
        }

        if (player.numCoins < 2) return false
        if (it.members.size >= 3 || (it.members.size >= 2 && player.park.largeExtensions.isEmpty())) {
            if (it.members.contains(poolLeft.first())) {
                if (!player.park.map.contains(-2 to 2)) {
                    println("Bot is playing large extension")
                    playerService.addLargeExtension(-2 to 2)
                    return true
                }
            }
            if (it.members.contains(poolBottom.first())) {
                if (!player.park.map.contains(2 to -2)) {
                    println("Bot is playing large extension")
                    playerService.addLargeExtension(2 to -2)
                    return true
                }
            }
        }
        return false
    }

    /**
     * chooses the best wagon for the bot by calculating a score for each wagon
     *
     * @return index of the best wagon
     */
    private fun bestWagon(): Int? {
        var bestIndexToScore = 0 to -10

        board.transportWagons.forEach() {
            val score = evaluateWagonPotential(it)
            if (score >= bestIndexToScore.second) {
                bestIndexToScore = board.transportWagons.indexOf(it) to score
            }
        }

        val wagonsAreNotFull = board.transportWagons.any { wagon -> wagon.any { card -> card is Empty } }
        if (bestIndexToScore.second < 4 && wagonsAreNotFull) return null

        return bestIndexToScore.first
    }

    /**
     * Evaluates a score for all cards in a given wagon
     *
     * @param wagon the wagon to be evaluated
     * @return a score
     */
    private fun evaluateWagonPotential(wagon: MutableList<Wagonable>): Int {
        var acc = 0

        wagon.forEach { card ->
            acc += evaluateTile(card)
        }

        return acc
    }

    /**
     * Evaluates a score for a given card
     *
     * @param tile the card to be evaluated
     * @return a score
     */
    private fun evaluateTile(tile: Wagonable): Int {
        if (tile is Empty) return 0

        // tile is coin-card
        if (tile !is Depotable) return 1

        val activeTypes = getActiveTypes(player)
        val depotTypes = getDepotTypes()

        if (activeTypes.contains(tile.type)) {
            val poolOfTile = player.park.pools.first { it.type == tile.type }
            // type is being collected and space is available
            if (aiHelperService.countAnimals(player.park, tile.type) < 7) {
                // is male or female
                if (tile is Animal && tile.gender != AnimalGender.UNSPECIFIED) {
                    //is needed mal or female
                    if (tile.gender == AnimalGender.MALE && poolOfTile.numFemale >= 1) return 5
                    if (tile.gender == AnimalGender.FEMALE && poolOfTile.numMale >= 1) return 5
                    return 4
                } else return 3
            }

            // type is being collected and space isn't available
            return if (depotTypes.contains(tile.type)) 0 else -1
        }

        // add type to active Types
        if (activeTypes.size < player.park.maxPools){
            var score = 2

            if (!getUsedTypes().contains(tile.type)) score += 1

            if (tile is Animal && tile.isTrainable) score += 1

            return score
        }

        // type is not on depot yet
        if (!depotTypes.contains(tile.type)) return -1

        // type is already on depot
        return 0
    }

    /**
     * Checks if a type is  present in one of the four pools
     *
     * @return true if it is
     */
    private fun isActiveType(animal: Depotable): Boolean {
        return getActiveTypes(player).contains(animal.type)
    }

    /**
     * Places a card, following the priority-queues
     *
     * @param card the card which has to be placed
     */
    private fun placeCard(card: Depotable) {
        val activeTypes = getActiveTypes(player)

        // check if type is NOT collected yet
        if (!activeTypes.contains(card.type)) {
            // if empty fourth exists and pool cam be started, start collecting type
            if (activeTypes.size < player.park.maxPools) {
                pools.forEach { pool ->
                    if (player.park.map[pool[0]] is Empty) {
                        println("Bot is playing animal to park")
                        playerService.animalToPark(card, pool[0])
                        return
                    }
                }
            }

            // otherwise card has to be put in depot
            println("Bot is playing card to depot")
            playerService.animalToDepot(card)
            return
        }

        // find the correct quarter, where the type is already being collected
        pools.forEach { pool ->
            val startTile = player.park.map[pool[0]]

            if (startTile is Depotable && startTile.type == card.type) {
                // iterate through corresponding priority-queue and place card if possible
                pool.forEach {
                    if (player.park.map.contains(it) && player.park.map[it] is Empty) {
                        println("Bot is playing card to park")
                        playerService.animalToPark(card, it)
                        return
                    }
                }
            }
        }

        // if no such position is found, put card to depot
        println("Bot is playing card to depot")
        playerService.animalToDepot(card)
    }

    /**
     * Checks if the top-most depot-card is unique
     *
     * @param player is the player, whose depot is being checked
     * @return true type is unique
     */
    private fun uniqueTypeOnDepot(player: Player): Boolean {
        if (player.depot.isEmpty()) return false

        val topDepotCard = player.depot.last()

        return player.depot.count { card -> card.type == topDepotCard.type } == 1
    }

    private fun placeTileOnWagon() {
        val fromMainStack = board.mainStack.isNotEmpty()
        val drawnCard = if (fromMainStack) board.mainStack.last() else board.endOfGameStack.last()

        if (evaluateTile(drawnCard) > 0) {
            println("Bot is playing card on wagon")
            playerService.putCardOnWagon(getSortedEvaluation().last().second)
        } else {
            println("Bot is playing card on wagon")
            playerService.putCardOnWagon(getSortedEvaluation().first().second)
        }
    }

    private fun getSortedEvaluation(): List<Pair<Int, Int>> {
        val indexedWagons = board.transportWagons.mapIndexed { index, wagon -> wagon to index }
        val availableWagons = indexedWagons.filter { it.first.any { tile -> tile is Empty } }
        val evaluation = availableWagons.map { evaluateWagonPotential(it.first) to it.second }
        return evaluation.sortedBy { it.first }
    }

    /**
     * calculates the best worker to be placed out of the [workers] list
     */
    private fun bestWorker(): Pair<Int, Int> {
        var bestCordsToScore = MANAGER to -1
        workers.forEach() {
            val score = evaluateWorker(it)
            if (score > bestCordsToScore.second) {
                bestCordsToScore = it to score
            }
        }
        return bestCordsToScore.first
    }

    /**
     * calculates point gained by new worker on [pos] (without cashier we spend a lot)
     */
    private fun evaluateWorker(pos: Pair<Int, Int>): Int {
        val map = player.park.map
        //Cashiers
        if (pos == CASHIER && player.numCashiers < 2) return player.numCoins
        //Manager
        if (pos == MANAGER && !player.hasManager) return getDepotTypes().size
        //Nurse
        if (player.numNurses < 2) {
            if (pos == NURSE) return map.filter { it.value is Animal && (it.value as Animal).isFish }.size
        }
        //else trainer
        if (map.contains(pos) && map[pos] is Empty) {
            return map.allNeighbours(pos).filter {
                val animal = map[it]
                (animal is Animal && animal.isTrainable)
            }.size
        }
        return -1
    }

    /**
     * To get current animal types needed
     *
     * @return a set of all types present on the board
     */
    private fun getActiveTypes(player: Player): MutableSet<AnimalType> {
        return player.park.pools.map { pool -> pool.type }.toMutableSet()

    }

    private fun getUsedTypes(): MutableSet<AnimalType> {
        val usedTypes = mutableSetOf<AnimalType>()

        game.players.forEach { player -> usedTypes.addAll(getActiveTypes(player)) }

        return usedTypes
    }

    /**
     * @return a set of all types present in the depot
     */
    private fun getDepotTypes(): MutableSet<AnimalType> {
        val depotTypes = mutableSetOf<AnimalType>()

        player.depot.forEach { card ->
            depotTypes.add(card.type)
        }


        return depotTypes
    }
}
package service

import entity.*
import entity.players.DumbBot
import view.Refreshable
import kotlin.random.Random

class DumbBotService(private val rootService: RootService) : Refreshable, AbstractRefreshingService() {
    private val aiHelperService = rootService.aiHelperService
    private val playerService = rootService.playerActionService

    private lateinit var player: Player
    private lateinit var board: Board
    private lateinit var game: Aquaretto

    override fun refreshAfterInitializeGame() {
        board = rootService.aquarettoGame.board
        game = rootService.aquarettoGame
        player = rootService.aquarettoGame.currentPlayer()

        if (player !is DumbBot) return
        chooseMoves()
        rootService.gameStateService.endTurn()
    }

    /**
     * Calls chooseMoves when the current player is a SmartBot
     */
    override fun refreshAfterEndTurn(isReverting: Boolean, isNewRound: Boolean) {
        player = rootService.aquarettoGame.currentPlayer()

        if (isReverting || player !is DumbBot) return
        chooseMoves()
        rootService.gameStateService.endTurn()
    }

    /**
     * Calls placeWorker when a worker is gained by a SmartBot
     */
    override fun refreshAfterGainedWorker(isReverting: Boolean) {
        player = rootService.aquarettoGame.currentPlayer()

        if (isReverting || player !is DumbBot) return
        placeWorker()
    }

    override fun refreshAfterGainedCard(isReverting: Boolean) {
        player = rootService.aquarettoGame.currentPlayer()

        if (isReverting || player !is DumbBot) return
        while (player.cardsToPlay.isNotEmpty()) {
            placeCard()
        }
    }

    /**
     * chooses a move from determent priority list
     */
    private fun chooseMoves() {
        val moves = aiHelperService.legalMoves(rootService.aquarettoGame)
        when (moves.random()) {
            1 -> if (!drawCard()) chooseMoves()
            2 -> if (!takeWagon()) chooseMoves()
            3 -> if (!chooseExtension()) chooseMoves()
            4 -> if (!buySell()) chooseMoves()
        }
    }

    private fun drawCard(): Boolean {
        val legalWagons = board.transportWagons.filter { wagon -> wagon.any() { it is Empty } }
        if (legalWagons.isEmpty()) return false
        val chosen = legalWagons[Random.nextInt(0, legalWagons.size)]
        val index = board.transportWagons.indexOf(chosen)
        playerService.putCardOnWagon(index)
        return true
    }

    private fun takeWagon(): Boolean {
        val legalWagons = board.transportWagons.filter { wagon -> wagon.any() { it !is Empty } }
        if (legalWagons.isEmpty()) return false
        val chosen = legalWagons[Random.nextInt(0, legalWagons.size)]
        val index = board.transportWagons.indexOf(chosen)
        playerService.takeWagon(index)
        return true
    }

    private fun chooseExtension(): Boolean {
        val size = Random.nextInt(0, 2) // zero or one -> small or large
        if (size == 0) {
            val legal = aiHelperService.legalSmallExtension(player.park.map)
            if (player.numCoins < 1 || player.park.smallExtensions.size >= 2 || legal.isEmpty()) return false
            val chosen = legal[Random.nextInt(0, legal.size)]
            playerService.addSmallExtension(chosen.first, chosen.second)
        } else {
            val legal = aiHelperService.legalLargeExtension(player.park.map)
            if (player.numCoins < 2 || player.park.largeExtensions.size >= 2 || legal.isEmpty()) return false
            val chosen = legal[Random.nextInt(0, legal.size)]
            playerService.addLargeExtension(chosen)
        }
        return true
    }

    private fun buySell(): Boolean {
        val legal = game.players.filter { it.depot.isNotEmpty() }
        if (player.numCoins < 2 || legal.isEmpty()) return false
        val chosen = legal[Random.nextInt(0, legal.size)]
        if (chosen == player) {
            playerService.sellCard()
        } else {
            playerService.buyCard(chosen)
        }
        return true
    }

    private fun placeWorker() {
        when (Random.nextInt(1, 4)) {
            1 -> if (!placeCashier()) placeWorker()
            2 -> if (!placeKeeper()) placeWorker()
            3 -> if (!placeTrainer()) placeWorker()
            4 -> if (!placeManager()) placeWorker()
        }
    }

    private fun placeCashier(): Boolean {
        if (player.numCashiers >= 2) return false
        playerService.relocateWorker(null, CASHIER)
        return true
    }

    private fun placeKeeper(): Boolean {
        if (player.numNurses >= 2) return false
        playerService.relocateWorker(null, NURSE)
        return true
    }

    private fun placeManager(): Boolean {
        if (player.hasManager) return false
        playerService.relocateWorker(null, MANAGER)
        return true
    }

    private fun placeTrainer(): Boolean {
        val legal = player.park.map.filter { it.value is Empty }.keys
        if (legal.isEmpty()) return false
        val chosen = legal.random()
        println("=== ALL LEGAL: $legal")
        println("====== CHOSEN: $chosen")
        playerService.relocateWorker(null, chosen)
        return true
    }

    private fun placeCard() {
        val card = player.cardsToPlay[Random.nextInt(0, player.cardsToPlay.size)]
        val positions = aiHelperService.legalPlaces(player.park, card.type)
        if (positions.isEmpty()) {
            playerService.animalToDepot(card)
        } else {
            val pos = positions[Random.nextInt(0, positions.size)]
            playerService.animalToPark(card, pos)
        }
    }
}
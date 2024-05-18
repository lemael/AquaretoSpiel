package service

import entity.*
import org.junit.jupiter.api.Test
import kotlin.test.*

/**
 * Class that provides test for [PlayerActionService] by creating an empty game and calling actions
 */
class PlayerActionServiceTest {

    /**
     * Creates a game with hotseatPlayer with name "Player n"
     */
    private fun initGame(root: RootService, playerAmount: Int) {
        val players = mutableListOf<String>()
        val types = mutableListOf<Int>()
        for (i in 0 until playerAmount) {
            players.add("Player $i")
            types.add(HOTSEAT)
        }
        root.gameStateService.initializeGame(players, types)
    }

    @Test
    fun testPutCardOnWagon() {
        val root = RootService()
        initGame(root, 3)
        val service = root.playerActionService
        val board = root.aquarettoGame.board
        val testRefreshable = TestRefreshable()
        root.addRefreshables(testRefreshable)



        //Fail wagon index out of bounds
        assertFailsWith<IllegalArgumentException>("the given wagon 3 does not exist") {
            service.putCardOnWagon(3)
        }
        assertFailsWith<IllegalArgumentException>("the given wagon 3 does not exist") {
            service.putCardOnWagon(-1)
        }

        service.putCardOnWagon(0)
        service.putCardOnWagon(0)
        assertEquals(1, board.transportWagons[0].count { it is Empty })
        board.transportWagons[1].forEach { assertTrue(it is Empty) }
        board.transportWagons[2].forEach { assertTrue(it is Empty) }

        //test refreshable
        testRefreshable.reset()
        service.putCardOnWagon(0)
        assertTrue { testRefreshable.refreshAfterWagonsChangedCalled }

        //Fail wagon full
        assertFailsWith<IllegalArgumentException>("the selected wagon is full") {
            service.putCardOnWagon(0)
        }
    }

    @Test
    fun testCommandPutCardOnWagon(){
        // setup
        val root = RootService()
        initGame(root, 3)
        val service = root.playerActionService
        val board = root.aquarettoGame.board
        val testRefreshable = TestRefreshable()
        root.addRefreshables(testRefreshable)

        val prevStackSize = board.mainStack.size
        val prevStackItem = board.mainStack.last()

        service.putCardOnWagon(0)
        testRefreshable.reset()
        root.gameStateService.revertLastCommand()
        // testing
        board.transportWagons.forEach { wagon ->
            assertEquals(3, wagon.size)
            wagon.forEach { assertTrue { it is Empty } }
        }

        assertEquals(prevStackSize, root.aquarettoGame.board.mainStack.size)
        assertEquals(prevStackItem, board.mainStack.last())

        assertTrue { testRefreshable.refreshAfterWagonsChangedCalled }
    }

    @Test
    fun testTakeWagon() {
        val root = RootService()
        initGame(root, 3)
        val service = root.playerActionService
        val animal = Animal(AnimalType.DOLPHIN)
        val player = root.aquarettoGame.currentPlayer()
        val prevCoins = player.numCoins
        val board = root.aquarettoGame.board
        val testRefreshable = TestRefreshable()
        root.addRefreshables(testRefreshable)

        //Fail wagon index out of bounds
        assertFailsWith<IllegalArgumentException>("the given wagon 3 does not exist") {
            service.takeWagon(3)
        }
        assertFailsWith<IllegalArgumentException>("the given wagon 3 does not exist") {
            service.takeWagon(-1)
        }
        //Fail wagon empty
        assertFailsWith<IllegalArgumentException>("the selected wagon is empty") {
            service.takeWagon(0)
        }

        board.mainStack.clear()
        board.mainStack.add(animal)
        board.mainStack.add(CoinCard())
        service.putCardOnWagon(0)
        service.putCardOnWagon(0)
        testRefreshable.reset()
        service.takeWagon(0)

        assertEquals(animal, player.cardsToPlay[0])
        assertEquals(prevCoins + 1, player.numCoins)

        //test refreshable
        assertTrue { testRefreshable.refreshAfterGainedCardCalled }
        assertTrue { testRefreshable.refreshAfterChangedBalanceCalled }
        assertTrue { testRefreshable.refreshAfterTakeWagonCalled }

        //test revert
        testRefreshable.reset()
        root.gameStateService.revertLastCommand()
        assertTrue(player.cardsToPlay.isEmpty())
        assertEquals(prevCoins, player.numCoins)
        assertTrue(board.transportWagons[0][0] is CoinCard)
        assertEquals(animal, board.transportWagons[0][1])

        assertTrue { testRefreshable.refreshAfterGainedCardCalled }
        assertTrue { testRefreshable.refreshAfterChangedBalanceCalled }
        assertTrue { testRefreshable.refreshAfterTakeWagonCalled }
        testRefreshable.reset()
    }

    @Test
    fun testAnimalToPark() {
        val root = RootService()
        initGame(root, 3)
        val service = root.playerActionService
        val crocodile = Animal(AnimalType.CROCODILE)
        val dolphin = Animal(AnimalType.DOLPHIN)
        val dolphinM = Animal(AnimalType.DOLPHIN, gender = AnimalGender.MALE)
        val dolphinF = Animal(AnimalType.DOLPHIN, gender = AnimalGender.FEMALE)
        val player = root.aquarettoGame.currentPlayer()
        val prevCoins = player.numCoins
        val testRefreshable = TestRefreshable()
        root.addRefreshables(testRefreshable)

        //new pool
        service.animalToPark(dolphin, 1, 3)
        assertEquals(1, player.park.pools.size)
        assertEquals(dolphin, player.park.map[1, 3])
        assertTrue { testRefreshable.refreshAfterParkChangedCalled }
        testRefreshable.reset()
        //add one to pool and add female
        service.animalToPark(dolphinF, 1, 2)
        assertEquals(2, player.park.pools[0].members.size)
        assertEquals(0, player.park.pools[0].numMale)
        assertEquals(1, player.park.pools[0].numFemale)
        //add third -> new coin
        service.animalToPark(dolphin, 0, 2)
        assertEquals(prevCoins + 1, player.numCoins)
        assertTrue { testRefreshable.refreshAfterParkChangedCalled }
        assertTrue { testRefreshable.refreshAfterChangedBalanceCalled }
        testRefreshable.reset()
        //add male to female
        service.animalToPark(dolphinM, 0, 3)
        assertEquals(0, player.park.pools[0].numMale)
        assertEquals(0, player.park.pools[0].numFemale)
        assertEquals(1, player.cardsToPlay.size)
        assertTrue { testRefreshable.refreshAfterParkChangedCalled }
        assertTrue { testRefreshable.refreshAfterGainedCardCalled }
        testRefreshable.reset()
        //add fifth test for worker by refreshable
        service.animalToPark(dolphin, 2, 3)
        assertTrue { testRefreshable.refreshAfterParkChangedCalled }
        assertTrue { testRefreshable.refreshAfterGainedWorkerCalled}
        testRefreshable.reset()

        //add second pool
        service.animalToPark(crocodile, 2, 0)
        assertEquals(2, player.park.pools.size)
        assertEquals(crocodile, player.park.map.get(2, 0))

        //test revert
        root.gameStateService.revertLastCommand()

    }

    @Test
    fun testFailAnimalToPark() {
        val root = RootService()
        initGame(root, 3)
        val service = root.playerActionService
        val crocodile = Animal(AnimalType.CROCODILE)
        val turtle = Animal(AnimalType.TURTLE)
        val hippo = Animal(AnimalType.HIPPO)
        val dolphin = Animal(AnimalType.DOLPHIN)

        //Postion our of bounds
        assertFailsWith<IllegalArgumentException>(
            "position ${Pair(-1, -1)} does not exist"
        ) {
            service.animalToPark(dolphin, -1, -1)
        }

        //Position not empty
        service.animalToPark(dolphin, 2, 2)
        assertFailsWith<IllegalArgumentException>(
            "the park has to be empty on position ${Pair(2, 2)}"
        ) {
            service.animalToPark(dolphin, 2, 2)
        }
        //invalid neighbor all directions
        assertFailsWith<IllegalArgumentException>(
            "position ${Pair(2, 3)} borders a different animal type at ${Pair(2, 2)}"
        ) {
            service.animalToPark(crocodile, 2, 3)
        }
        assertFailsWith<IllegalArgumentException>(
            "position ${Pair(3, 2)} borders a different animal type at ${Pair(2, 2)}"
        ) {
            service.animalToPark(crocodile, 3, 2)
        }
        assertFailsWith<IllegalArgumentException>(
            "position ${Pair(2, 1)} borders a different animal type at ${Pair(2, 2)}"
        ) {
            service.animalToPark(crocodile, 2, 1)
        }
        assertFailsWith<IllegalArgumentException>(
            "position ${Pair(1, 2)} borders a different animal type at ${Pair(2, 2)}"
        ) {
            service.animalToPark(crocodile, 1, 2)
        }

        //already existing pool
        assertFailsWith<IllegalArgumentException>(
            "there is a pool of the correct type but it does not border the position"
        ) {
            service.animalToPark(dolphin, 0, 2)
        }

        //max pool amount
        service.animalToPark(turtle, 1, 1)
        service.animalToPark(hippo, 0, 2)
        assertFailsWith<IllegalArgumentException>(
            "can not add a new pool as maximum has been reached"
        ) {
            service.animalToPark(crocodile, 1, 3)
        }
    }

    @Test
    fun testAnimalToDepot() {
        val root = RootService()
        initGame(root, 3)
        val service = root.playerActionService
        val dolphin = Animal(AnimalType.DOLPHIN)
        val testRefreshable = TestRefreshable()
        root.addRefreshables(testRefreshable)

        service.animalToDepot(dolphin)
        assertEquals(dolphin, root.aquarettoGame.currentPlayer().depot.last())

        assertTrue(testRefreshable.refreshAfterDepotChangedCalled)
        testRefreshable.reset()
    }

    @Test
    fun testDepotToPark() {
        val root = RootService()
        initGame(root, 3)
        val service = root.playerActionService
        val dolphin = Animal(AnimalType.DOLPHIN)
        val testRefreshable = TestRefreshable()
        val player = root.aquarettoGame.currentPlayer()
        root.addRefreshables(testRefreshable)


        for (i in 0..2) {
            player.depot.add(dolphin)
        }
        service.depotToPark()
        assertEquals(2, player.depot.size)
        assertEquals(1, player.cardsToPlay.size)
        assertEquals(dolphin, player.cardsToPlay[0])

        assertTrue(testRefreshable.refreshAfterChangedBalanceCalled)
        assertTrue(testRefreshable.refreshAfterDepotChangedCalled)
        assertTrue(testRefreshable.refreshAfterGainedCardCalled)
        testRefreshable.reset()
    }

    @Test
    fun testFailDepotToPark() {
        val root = RootService()
        initGame(root, 3)
        val dolphin = Animal(AnimalType.DOLPHIN)
        val service = root.playerActionService
        val testRefreshable = TestRefreshable()
        root.addRefreshables(testRefreshable)

        assertFailsWith<IllegalArgumentException>("can not buy card from an empty depot.") {
            service.depotToPark()
        }
        root.aquarettoGame.currentPlayer().numCoins = 0
        root.aquarettoGame.currentPlayer().depot.add(dolphin)
        assertFailsWith<IllegalArgumentException>("player can not afford depotToPark: actual: 0 required 1") {
            service.depotToPark()
        }
    }

    @Test
    fun testRelocateWorker() {
        val root = RootService()
        initGame(root, 3)
        val service = root.playerActionService
        val player = root.aquarettoGame.currentPlayer()
        val testRefreshable = TestRefreshable()
        root.addRefreshables(testRefreshable)
        val old = 1 to 1
        val new = 2 to 2


        player.park.map[old] = Worker()
        //old CASHIER ->
        root.aquarettoGame.currentPlayer().numCoins = 1
        player.numCashiers = 1
        service.relocateWorker(CASHIER, new)
        assertTrue(player.park.map[new] is Worker)
        assertEquals(0, player.numCashiers)
        assertEquals(0, player.numCoins)
        player.park.map[new] = Empty()

        //old NURSE ->
        root.aquarettoGame.currentPlayer().numCoins = 1
        player.numNurses = 1
        service.relocateWorker(NURSE, new)
        assertTrue(player.park.map[new] is Worker)
        assertEquals(0, player.numNurses)
        assertEquals(0, player.numCoins)
        player.park.map[new] = Empty()

        //old MANAGER ->
        root.aquarettoGame.currentPlayer().numCoins = 1
        player.hasManager = true
        service.relocateWorker(MANAGER, new)
        assertTrue(player.park.map[new] is Worker)
        assertFalse(player.hasManager)
        assertEquals(0, player.numCoins)
        player.park.map[new] = Empty()

        // old worker ->
        root.aquarettoGame.currentPlayer().numCoins = 1
        service.relocateWorker(old, new)
        assertTrue(player.park.map[new] is Worker)
        assertTrue(player.park.map[old] is Empty)
        assertEquals(0, player.numCoins)
        player.park.map[new] = Empty()


        player.park.map[old] = Worker()

        //new CASHIER ->
        root.aquarettoGame.currentPlayer().numCoins = 1
        service.relocateWorker(old, CASHIER)
        assertTrue(player.park.map[old] is Empty)
        assertEquals(1, player.numCashiers)
        assertEquals(0, player.numCoins)
        player.park.map[old] = Worker()

        //new NURSE ->
        root.aquarettoGame.currentPlayer().numCoins = 1
        player.numNurses = 1
        service.relocateWorker(old, NURSE)
        assertTrue(player.park.map[old] is Empty)
        assertEquals(2, player.numNurses)
        assertEquals(0, player.numCoins)
        player.park.map[old] = Worker()

        //new MANAGER ->
        root.aquarettoGame.currentPlayer().numCoins = 1
        player.hasManager = false
        service.relocateWorker(old, MANAGER)
        assertTrue(player.park.map[old] is Empty)
        assertTrue(player.hasManager)
        assertEquals(0, player.numCoins)
        player.park.map[old] = Worker()

        // new worker -> included with old worker

        //testing refreshable
        testRefreshable.reset()
        root.aquarettoGame.currentPlayer().numCoins = 1
        service.relocateWorker(old, new)
        assertTrue { testRefreshable.refreshAfterChangedBalanceCalled }
        assertTrue { testRefreshable.refreshAfterRelocateWorkerCalled }
        testRefreshable.reset()
    }

    @Test
    fun testFailRelocateWorker() {
        val root = RootService()
        initGame(root, 3)
        val service = root.playerActionService
        val player = root.aquarettoGame.currentPlayer()
        val old = 1 to 1
        var new = 1 to 1

        //Test Balance
        root.aquarettoGame.currentPlayer().numCoins = 0
        assertFailsWith<IllegalArgumentException>("player cannot afford relocateWorker: actual: 0, required 1") {
            service.relocateWorker(old, new)
        }

        root.aquarettoGame.currentPlayer().numCoins = 100

        //when (old)
        // CASHIER ->
        assertFailsWith<IllegalArgumentException>("cannot relocate cashier, as the player does not have one") {
            service.relocateWorker(CASHIER, new)
        }
        //NURSE ->
        assertFailsWith<IllegalArgumentException>("cannot relocate nurse, as the player does not have one") {
            service.relocateWorker(NURSE, new)
        }
        //MANAGER ->
        assertFailsWith<IllegalArgumentException>("cannot relocate manager, as the player does not have one") {
            service.relocateWorker(MANAGER, new)
        }
        //else ->
        assertFailsWith<IllegalArgumentException>("cannot relocate worker from an illegal position: ${-1 to -1}") {
            service.relocateWorker(-1 to -1, new)
        }
        assertFailsWith<IllegalArgumentException>("given location does not contain a trainer.") {
            service.relocateWorker(1 to 1, new)
        }


        //when (new)
        player.park.map[old] = Worker()
        //CASHIER ->
        player.numCashiers = 2
        assertFailsWith<IllegalArgumentException>("cannot employ cashier, as the player already has 2") {
            service.relocateWorker(old, CASHIER)
        }
        //NURSE ->
        player.numNurses = 2
        assertFailsWith<IllegalArgumentException>("cannot employ nurse, as the player already has 2") {
            service.relocateWorker(old, NURSE)
        }
        //MANAGER ->
        player.hasManager = true
        assertFailsWith<IllegalArgumentException>("cannot employ manager, as the player already has one") {
            service.relocateWorker(old, MANAGER)
        }
        //else ->
        new = -1 to -1
        assertFailsWith<IllegalArgumentException>("cannot employ worker on an illegal position: $new") {
            service.relocateWorker(old, new)
        }
        new = 2 to 2
        player.park.map[new] = Worker()
        assertFailsWith<IllegalArgumentException>("given location already contains something.") {
            service.relocateWorker(old, new)
        }
    }

    @Test
    fun testCommandRelocateWorker() {
        val root = RootService()
        initGame(root, 3)
        val service = root.playerActionService
        val player = root.aquarettoGame.currentPlayer()
        val testRefreshable = TestRefreshable()
        root.addRefreshables(testRefreshable)
        val old = 1 to 1
        val new = 2 to 2

        root.aquarettoGame.currentPlayer().numCoins = 1
        player.park.map[old] = Worker()
        //old CASHIER ->
        player.numCashiers = 1
        service.relocateWorker(CASHIER, new)
        root.gameStateService.revertLastCommand()           //revert
        assertTrue(player.park.map[new] is Empty)
        assertEquals(1, player.numCashiers)
        assertEquals(1, player.numCoins)

        //old NURSE ->
        player.numNurses = 1
        service.relocateWorker(NURSE, new)
        root.gameStateService.revertLastCommand()           //revert
        assertTrue(player.park.map[new] is Empty)
        assertEquals(1, player.numNurses)
        assertEquals(1, player.numCoins)

        //old MANAGER ->
        player.hasManager = true
        service.relocateWorker(MANAGER, new)
        root.gameStateService.revertLastCommand()           //revert
        assertTrue(player.park.map[new] is Empty)
        assertTrue(player.hasManager)
        assertEquals(1, player.numCoins)

        // old worker ->
        service.relocateWorker(old, new)
        root.gameStateService.revertLastCommand()           //revert
        assertTrue(player.park.map[new] is Empty)
        assertTrue(player.park.map[old] is Worker)
        assertEquals(1, player.numCoins)


        //new CASHIER ->
        player.numCashiers = 0
        service.relocateWorker(old, CASHIER)
        root.gameStateService.revertLastCommand()           //revert
        assertTrue(player.park.map[new] is Empty)
        assertEquals(0, player.numCashiers)
        assertEquals(1, player.numCoins)

        //new NURSE ->
        player.numNurses = 0
        service.relocateWorker(old, NURSE)
        root.gameStateService.revertLastCommand()           //revert
        assertTrue(player.park.map[new] is Empty)
        assertEquals(0, player.numNurses)
        assertEquals(1, player.numCoins)

        //new MANAGER ->
        player.hasManager = false
        service.relocateWorker(old, MANAGER)
        root.gameStateService.revertLastCommand()           //revert
        assertTrue(player.park.map[new] is Empty)
        assertFalse(player.hasManager)
        assertEquals(1, player.numCoins)
        player.park.map[old] = Worker()

        // new worker -> included with old worker
    }

    @Test
    fun testBuyCard() {
        val root = RootService()
        initGame(root, 3)
        val service = root.playerActionService
        val dolphin = Animal(AnimalType.DOLPHIN)
        val player = root.aquarettoGame.currentPlayer()
        val otherPlayer = root.aquarettoGame.players[1]
        val testRefreshable = TestRefreshable()
        root.addRefreshables(testRefreshable)

        player.numCoins = 2
        otherPlayer.depot.add(dolphin)
        service.buyCard(otherPlayer)
        assertEquals(dolphin, player.cardsToPlay.last())
        assert(otherPlayer.depot.isEmpty())
        assertEquals(0, player.numCoins)

        assertTrue { testRefreshable.refreshAfterChangedBalanceCalled }
        assertTrue { testRefreshable.refreshAfterDepotChangedCalled }
        testRefreshable.reset()
    }

    @Test
    fun testFailBuyCard() {
        val root = RootService()
        initGame(root, 3)
        val service = root.playerActionService
        val player = root.aquarettoGame.currentPlayer()
        val otherPlayer = root.aquarettoGame.players[1]

        assertFailsWith<IllegalArgumentException>(
            "The player does not have enough coins for this purchase."
        ) {
            service.buyCard(otherPlayer)
        }
        player.numCoins = 2
        assertFailsWith<IllegalArgumentException>(
            "The chosen player does not have a card in the depot."
        ) {
            service.buyCard(otherPlayer)
        }

    }

    @Test
    fun testCommandBuyCard() {
        val root = RootService()
        initGame(root, 3)
        val service = root.playerActionService
        val dolphin = Animal(AnimalType.DOLPHIN)
        val player = root.aquarettoGame.currentPlayer()
        val otherPlayer = root.aquarettoGame.players[1]

        player.numCoins = 2
        otherPlayer.depot.add(dolphin)
        service.buyCard(otherPlayer)
        root.gameStateService.revertLastCommand()       //revert
        assert(player.depot.isEmpty())
        assertEquals(dolphin, otherPlayer.depot.last())
        assertEquals(2, player.numCoins)

    }

    @Test
    fun testSellCard() {
        val root = RootService()
        initGame(root, 3)
        val service = root.playerActionService
        val dolphin = Animal(AnimalType.DOLPHIN)
        val player = root.aquarettoGame.currentPlayer()
        val testRefreshable = TestRefreshable()
        root.addRefreshables(testRefreshable)

        player.numCoins = 2
        player.depot.add(dolphin)
        service.sellCard()
        assert(player.depot.isEmpty())
        assertEquals(0, player.numCoins)

        assertTrue { testRefreshable.refreshAfterChangedBalanceCalled }
        assertTrue { testRefreshable.refreshAfterDepotChangedCalled }
        testRefreshable.reset()
    }

    @Test
    fun testFailSellCard() {
        val root = RootService()
        initGame(root, 3)
        val service = root.playerActionService
        val player = root.aquarettoGame.currentPlayer()

        assertFailsWith<IllegalArgumentException>(
            "The player does not have enough coins for this transaction."
        ) {
            service.sellCard()
        }
        player.numCoins = 2
        assertFailsWith<IllegalArgumentException>(
            "The player does not have a card in the depot."
        ) {
            service.sellCard()
        }

    }

    @Test
    fun testCommandSellCard() {
        val root = RootService()
        initGame(root, 3)
        val service = root.playerActionService
        val dolphin = Animal(AnimalType.DOLPHIN)
        val player = root.aquarettoGame.currentPlayer()

        player.numCoins = 2
        player.depot.add(dolphin)
        service.sellCard()
        root.gameStateService.revertLastCommand()       //revert
        assertEquals(dolphin, player.depot.last())
        assertEquals(2, player.numCoins)

    }

    // region addSmallExtension
    @Test
    fun testAddSmallExtension() {
        val root = RootService()
        initGame(root, 2)
        val player = root.aquarettoGame.currentPlayer()
        val map = root.aquarettoGame.currentPlayer().park.map
        // give player a sufficient number of coins
        player.numCoins = 10
        // with base = (3,-1), orientation = TOP_LEFT the added tiles are (3,0), (4,-1), (4,0)
        root.playerActionService.addSmallExtension(3 to -1, TOP_LEFT)
        val tiles = listOf(3 to -1, 4 to -1, 4 to 0)

        // check correct addition to the map
        tiles.forEach {
            assertTrue { it in map }
            assertTrue { map[it] is Empty }
        }
        // check correct coin reduction
        assertEquals(9, player.numCoins)
        // check storage in park.smallExtensions
        assertTrue { player.park.smallExtensions.contains(Triple(3, -1, TOP_LEFT)) }
    }

    @Test
    fun testFailAddSmallExtension() {
        val root = RootService()
        initGame(root, 2)
        val player = root.aquarettoGame.currentPlayer()
        // test an insufficient number of coins
        player.numCoins = 0
        assertFailsWith<IllegalStateException>("player can not afford a small extension. cost: 1, has: 0")
        { root.playerActionService.addSmallExtension(4 to -1, TOP_LEFT) }
        player.numCoins = 10
        //incorrect rotation
        assertFailsWith<IllegalArgumentException>("orientation is 2 to 2 which does not match any of the ones defined in constants.kt")
        { root.playerActionService.addSmallExtension(3 to -1, 4) }
        // test overlapping with park
        assertFailsWith<IllegalArgumentException>("extension overlaps with existing tiles")
        { root.playerActionService.addSmallExtension(2 to 2, TOP_RIGHT) }
        // test overlapping with special tiles
        assertFailsWith<IllegalArgumentException>("extension overlaps the (0,1) cashier tile")
        { root.playerActionService.addSmallExtension(-1 to 0, BOTTOM_RIGHT) }
        assertFailsWith<IllegalArgumentException>("extension overlaps the (0,0) manager tile")
        { root.playerActionService.addSmallExtension(-1 to -1, BOTTOM_RIGHT) }
        assertFailsWith<IllegalArgumentException>("extension overlaps the (1,0) nurse tile")
        { root.playerActionService.addSmallExtension(0 to -1, TOP_LEFT) }
        // test not adjacent to board
        assertFailsWith<IllegalArgumentException>("the extension does not border any existing tile")
        { root.playerActionService.addSmallExtension(-5 to -5, TOP_LEFT) }
        //test max extension reached
        root.playerActionService.addSmallExtension(3 to -1, TOP_LEFT)
        root.playerActionService.addSmallExtension(-2 to 3, BOTTOM_LEFT)
        assertFailsWith<IllegalStateException>("maximum number of small extensions is reached")
        { root.playerActionService.addSmallExtension(3 to -1, BOTTOM_LEFT) } //pos doesn't matter, max checked first

    }

    @Test
    fun testCommandAddSmallExtension() {
        val root = RootService()
        initGame(root, 2)
        val player = root.aquarettoGame.currentPlayer()
        val map = root.aquarettoGame.currentPlayer().park.map
        // give player a sufficient number of coins
        player.numCoins = 10
        // execute and revert the command
        root.playerActionService.addSmallExtension(3 to -1, TOP_LEFT)
        root.gameStateService.revertLastCommand()

        // check for unchanged coins
        assertEquals(10, player.numCoins)
        // check for deletion of map entries
        val tiles = listOf(3 to -1, 4 to -1, 4 to 0)
        tiles.forEach {
            assertTrue { it !in map }
        }
        // check deletion from park.smallExtensions
        assertFalse { player.park.smallExtensions.contains(Triple(3, -1, TOP_LEFT)) }
    }
    // endregion


    // region addLargeExtension
    @Test
    fun testAddLargeExtension() {
        val root = RootService()
        initGame(root, 2)
        val player = root.aquarettoGame.currentPlayer()
        val map = root.aquarettoGame.currentPlayer().park.map
        // give player a sufficient number of coins
        player.numCoins = 10
        // with base = (4,-1), the added tiles are (4, -1), (4,0), (5,-1), (5,0)
        root.playerActionService.addLargeExtension(4 to -1)
        val tiles = listOf(4 to -1, 4 to 0, 5 to -1, 5 to 0)

        // check correct addition to the map
        tiles.forEach {
            assertTrue { it in map }
            assertTrue { map[it] is Empty }
        }
        // check correct coin reduction
        assertEquals(8, player.numCoins)
        // check storage in park.largeExtensions
        assertTrue { player.park.largeExtensions.contains(4 to -1) }
    }

    @Test
    fun testFailAddLargeExtension() {
        val root = RootService()
        initGame(root, 2)
        val player = root.aquarettoGame.currentPlayer()
        // test an insufficient number of coins
        player.numCoins = 0
        assertFailsWith<IllegalStateException>("player can not afford a large extension. cost: 2, has: 0")
        { root.playerActionService.addLargeExtension(4 to -1) }
        player.numCoins = 10
        // test overlapping with special tiles
        assertFailsWith<IllegalArgumentException>("extension overlaps the (0,1) cashier tile")
        { root.playerActionService.addLargeExtension(-1 to 1) }
        assertFailsWith<IllegalArgumentException>("extension overlaps the (0,0) manager tile")
        { root.playerActionService.addLargeExtension(-1 to -1) }
        assertFailsWith<IllegalArgumentException>("extension overlaps the (1,0) nurse tile")
        { root.playerActionService.addLargeExtension(1 to -1) }
        // test overlapping with park
        assertFailsWith<IllegalArgumentException>("extension overlaps with existing tiles")
        { root.playerActionService.addLargeExtension(2 to 2) }
        // test not adjacent to board
        assertFailsWith<IllegalArgumentException>("the extension does not border any existing tile")
        { root.playerActionService.addLargeExtension(-5 to -5) }
        //test max extension reached
        root.playerActionService.addLargeExtension(3 to -2)
        root.playerActionService.addLargeExtension(-2 to 3)
        assertFailsWith<IllegalStateException>("maximum number of large extensions is reached")
        { root.playerActionService.addLargeExtension(3 to -1) } //pos doesn't matter, max checked first

    }

    @Test
    fun testCommandAddLargeExtension() {
        val root = RootService()
        initGame(root, 2)
        val player = root.aquarettoGame.currentPlayer()
        val map = root.aquarettoGame.currentPlayer().park.map
        // give player a sufficient number of coins
        player.numCoins = 10
        // execute and revert the command
        root.playerActionService.addLargeExtension(4 to -1)
        root.gameStateService.revertLastCommand()

        // check for unchanged coins
        assertEquals(10, player.numCoins)
        // check for deletion of map entries
        val tiles = listOf(4 to -1, 4 to 0, 5 to -1, 5 to 0)
        tiles.forEach {
            assertTrue { it !in map }
        }
        // check deletion from park.smallExtensions
        assertFalse { player.park.largeExtensions.contains(4 to -1) }
    }
    // endregion

}

package service
import entity.*
import entity.commands.PutCardOnWagonCommand
import entity.players.DumbBot
import entity.players.SmartBot
import org.junit.jupiter.api.Test
import kotlin.test.*

class GameStateServiceTest {

    /**
     * Creates a game with hotseatPlayer with name "Player n"
     */
    private fun initGame(root: RootService, playerAmount : Int) {
        val players = mutableListOf<String>()
        val types = mutableListOf<Int>()
        for (i in 0 until playerAmount) {
            players.add("Player $i")
            types.add(HOTSEAT)
        }
        root.gameStateService.initializeGame(players, types)
    }
    /**
     * [testEvaluatePlayer] tests possible combinations of workers and animals in the park and check if the result
     * is calculated correctly. TRAINERS are untested for now*/
    @Test
    fun testEvaluatePlayer()
    {
        val root = RootService()
        initGame(root, 3)
        val playService = root.playerActionService
        val gameService = root.gameStateService
        val crocodile = Animal(AnimalType.CROCODILE, false, false, AnimalGender.UNSPECIFIED)
        val dolphin = Animal(AnimalType.DOLPHIN, false, true, AnimalGender.UNSPECIFIED)
        val dolphinUntrainable = Animal(AnimalType.DOLPHIN, false, false, AnimalGender.MALE)
        val orcaFish = Animal(AnimalType.ORCA, true, false)
        val currentPlayer = root.aquarettoGame.currentPlayer()
        val coins = currentPlayer.numCoins
        playService.animalToPark(dolphin, 0, 2)
        playService.animalToPark(dolphin, 0, 3)
        playService.animalToPark(dolphin, 1, 3)
        playService.animalToPark(dolphin, 2, 3)
        playService.animalToDepot(crocodile)
        var result = gameService.evaluatePlayer(currentPlayer).first + 0
        assertEquals(2,result) //4 dolphin - 2 depot.(+4 mit Trainer)
        playService.animalToPark(dolphinUntrainable,2,2)
        result = gameService.evaluatePlayer(currentPlayer).first + 0
        assertEquals(3,result) //5 dolphin -2 depot (+4 mit Trainer!)
        currentPlayer.hasManager = true
        result = gameService.evaluatePlayer(currentPlayer).first + 0
        assertEquals(4,result) // 5 dolphin - 2*0,5 depot
        currentPlayer.hasManager = false
        playService.animalToPark(orcaFish,2,0)
        playService.animalToPark(orcaFish,3,0)
        currentPlayer.numNurses = 1
        result = gameService.evaluatePlayer(currentPlayer).first + 0
        assertEquals(7,result) //5 dolphin + 2 Orca + 2 nurse -2 depot
        currentPlayer.numNurses = 0
        currentPlayer.numCashiers = 1
        currentPlayer.numCoins = 1
        result = gameService.evaluatePlayer(currentPlayer).first + 0
        assertEquals(6,result) // 5 dolphin + 2 orca -2 depot +1 Münze
        currentPlayer.numCashiers = 0
        playService.animalToDepot(dolphin)
        result = gameService.evaluatePlayer(currentPlayer).first + 0
        assertEquals(3,result) //5 dolphin +2 orca  -4 depot
        currentPlayer.park.map[1,2] = Worker()
        result = gameService.evaluatePlayer(currentPlayer).first + 0
        assertEquals(7,result) // 5 dolphin +2 orca +4 trainer -4 depot
        //failed seit dem letzten Update
    }
    //failed die Funktion oder der Test?
    @Test
    fun testEndTurn()
    {
        val root = RootService()
        initGame(root, 3)
        val gameService = root.gameStateService
        val testRefreshable = TestRefreshable()
        root.addRefreshables(testRefreshable)
        assertFalse(testRefreshable.refreshAfterEndTurnCalled)
        val prevPlayer = root.aquarettoGame.currentPlayer
        gameService.endTurn()
        assertTrue(root.aquarettoGame.currentPlayer != prevPlayer)
        assertTrue(testRefreshable.refreshAfterEndTurnCalled)
        root.aquarettoGame.currentPlayer().tookWagon = true
        gameService.endTurn()
        root.aquarettoGame.currentPlayer().tookWagon = true
        gameService.endTurn()
        root.aquarettoGame.currentPlayer().tookWagon = true
        gameService.endTurn()
        assertTrue(!root.aquarettoGame.currentPlayer().tookWagon)

    }
    @Test
    fun testEndGame()
    {
        val root = RootService()
        initGame(root, 3)
        val playService = root.playerActionService
        val gameService = root.gameStateService
        val dolphin = Animal(AnimalType.DOLPHIN, false, true, AnimalGender.UNSPECIFIED)
        val dolphinUntrainable = Animal(AnimalType.DOLPHIN, false, false, AnimalGender.MALE)
        val testRefreshable = TestRefreshable()
        root.addRefreshables(testRefreshable)
        playService.animalToPark(dolphin, 0, 2)
        playService.animalToPark(dolphin, 0, 3)
        playService.animalToPark(dolphin, 1, 3)
        playService.animalToPark(dolphin, 2, 3)
        gameService.endTurn()
        playService.animalToPark(dolphin, 0, 2)
        playService.animalToPark(dolphin, 0, 3)
        playService.animalToPark(dolphin, 1, 3)
        playService.animalToPark(dolphin, 2, 3)
        playService.animalToPark(dolphinUntrainable,2,2)
        gameService.endTurn()
        playService.animalToPark(dolphin, 0, 2)
        playService.animalToPark(dolphin, 0, 3)
        playService.animalToPark(dolphin, 1, 3)
        gameService.endGame()
        val results = testRefreshable.results
        assertEquals(5, results[0].second)
        assertEquals(4, results[1].second)
        assertEquals(3,results[2].second)

    }

    @Test
    fun testCommand(){
        val root = RootService()
        initGame(root,3)
        val service = root.gameStateService
        val game = root.aquarettoGame

        //test fail with lists empty
        assertFailsWith<IllegalArgumentException>("Undos are empty") { service.revertLastCommand() }
        assertFailsWith<IllegalArgumentException>("Redos are empty") { service.applyLastReverted() }


        //Execute command
        val tempCommand1 = PutCardOnWagonCommand(game.undos.size,game.board,0)
        service.executeCommand(tempCommand1)        //undo:(1) redo:()
        assertEquals(tempCommand1,game.undos[0])
        assertEquals(0,game.undos[0].id)
        assert(game.redos.isEmpty())
        //Execute command again
        val tempCommand2 = PutCardOnWagonCommand(game.undos.size,game.board,1)
        service.executeCommand(tempCommand2)        //undo:(1,2) redo:()
        assertEquals(tempCommand2,game.undos[1])
        assertEquals(1,game.undos[1].id)
        assert(game.redos.isEmpty())
        //revert command
        service.revertLastCommand()                //undo:(1) redo:(2)
        assertEquals(1,game.undos.size)
        assertEquals(tempCommand2,game.redos[0])
        //revert command again
        service.revertLastCommand()                //undo:() redo:(2,1)
        assert(game.undos.isEmpty())
        assertEquals(tempCommand1,game.redos[1])
        //redo command
        service.applyLastReverted()                //undo:(1) redo:(2)
        assertEquals(1,game.undos.size)
        assertEquals(1,game.redos.size)
        //Execute when redos is not empty
        val tempCommand3 = PutCardOnWagonCommand(game.undos.size,game.board,2)
        service.executeCommand(tempCommand3)       //undo:(1,3) redo:()
        assertEquals(tempCommand3,game.undos[1])
        assertEquals(1,game.undos[1].id)
        assert(game.redos.isEmpty())
    }
    @Test

    fun testCreateCards()
    {
        val root = RootService()
        initGame(root, 3)
        val gameService = root.gameStateService
        val testRefreshable = TestRefreshable()
        root.addRefreshables(testRefreshable)
        val cards = gameService.createCards(3)
        val coinCard = cards.filter{ it is CoinCard }
        val animals = cards.filter {it is Animal}
        val females = animals.filter{val card = it
            card is Animal && card.gender == AnimalGender.FEMALE}
        val males = animals.filter { val card = it
            card is Animal && card.gender == AnimalGender.MALE}
        val untrainables = animals.filter {val card = it
            card is Animal && !card.isTrainable }
         val dolphins = animals.filter{val card = it
             card is Animal && card.type == AnimalType.DOLPHIN}
        assertEquals(10,coinCard.size)
        assertEquals(76,cards.size)
        assertEquals(12, males.size)
        assertEquals(12, females.size)
        assertEquals(39, untrainables.size)
        assertEquals(11,dolphins.size)


    }
    @Test
    fun testInitializeGame()
    {
        val root = RootService()
        val gameService = root.gameStateService
        val testRefreshable = TestRefreshable()
        root.addRefreshables(testRefreshable)
        val players = mutableListOf<String>()
        val types = mutableListOf<Int>()
        players.add("Player 0")
        types.add(2)
        players.add("Player 1")
        types.add(3)
        assertFalse(testRefreshable.refreshAfterInitializeGameCalled)
       gameService.initializeGame(players,types)
        assertTrue(testRefreshable.refreshAfterInitializeGameCalled)
        assertEquals(3, root.aquarettoGame.board.transportWagons.size)
        assertEquals(1,root.aquarettoGame.board.transportWagons[0].size)
        assertEquals(2,root.aquarettoGame.board.transportWagons[1].size)
        assertEquals(3,root.aquarettoGame.board.transportWagons[2].size)
        assertEquals(15,root.aquarettoGame.board.endOfGameStack.size)
        assertTrue( root.aquarettoGame.players[0] is SmartBot)
        assertTrue(root.aquarettoGame.players[1] is DumbBot)
    }

}
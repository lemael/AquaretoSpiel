package view

import entity.Player
import service.RootService
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.core.MenuScene

class AquarettoApplication : BoardGameApplication("Aquaretto"), Refreshable {

    private val rootService = RootService()

    private val newGameScene: MenuScene = NewGameScene(rootService).apply {
        localButton.onMouseClicked = {
            this@AquarettoApplication.showMenuScene(localGameScene)
        }
        onlineButton.onMouseClicked = {
            this@AquarettoApplication.showMenuScene(onlineGameScene)
        }
        /*
        helpButton.onMouseClicked = {
            this@AquarettoApplication.showMenuScene(howToPlayScene)
        }

         */
    }

    private val localGameScene = LocalGameScene(rootService).apply {
        newGameButton.onMouseClicked = {
            this@AquarettoApplication.showMenuScene(gameConfigScene)
        }

        backButton.onMouseClicked = {
            this@AquarettoApplication.showMenuScene(newGameScene)
        }
    }

    private val onlineGameScene = OnlineGameScene(rootService).apply {
        hostGameButton.onMouseClicked = {
            this@AquarettoApplication.showMenuScene(hostGameScene)
        }
        joinGameButton.onMouseClicked = {
            this@AquarettoApplication.showMenuScene(joinGameScene)
        }
        backButton.onMouseClicked = {
            this@AquarettoApplication.showMenuScene(newGameScene)
        }
    }

    private val gameConfigScene = GameConfigScene(rootService).apply {
        backButton.onMouseClicked = {
            this@AquarettoApplication.showMenuScene(newGameScene)
        }
    }

    /*
    private val howToPlayScene = HowToPlayScene(rootService).apply {
        backButton.onMouseClicked = {
            this@AquarettoApplication.showMenuScene(newGameScene)
        }
    }

     */

    private val hostGameScene = HostGameScene(rootService).apply {
        backButton.onMouseClicked = { this@AquarettoApplication.showMenuScene(newGameScene) }
        createGameButton.onMouseClicked = {
            this@AquarettoApplication.showMenuScene(lobbyScene)
            rootService.networkService.hostGame("aqua24a", hostNameField.text, null)
        }
    }
    private val joinGameScene = JoinGameScene(rootService).apply {
        backButton.onMouseClicked = {
            this@AquarettoApplication.showMenuScene(newGameScene)
        }
        joinGameButton.onMouseClicked = {
            this@AquarettoApplication.showMenuScene(lobbyScene)
            rootService.networkService.joinGame("aqua24a", playerNameField.text, sessionIDField.text)
        }
    }

    private val leaderBoardScene = LeaderboardScene(rootService).apply{
        newGameButton.onMouseClicked = {
            this@AquarettoApplication.showMenuScene(newGameScene)
        }
    }

    private val optionsScene = OptionsScene(rootService).apply {
        continueButton.onMouseClicked = {
            hideMenuScene()
        }
    }
    private val aquarettoScene = AquarettoScene(rootService).apply {
        settingsButton.onMouseClicked = {
            this@AquarettoApplication.showMenuScene(optionsScene)
        }
    }

    private val lobbyScene: LobbyScene = LobbyScene(rootService).apply {
        backButton.onMouseClicked = { this@AquarettoApplication.showMenuScene(hostGameScene) }
    }

    override fun refreshAfterInitializeGame() {
        hideMenuScene()
    }

    override fun refreshAfterEndGame(results: MutableList<Triple<Player, Int, Int>>) {
        showMenuScene(leaderBoardScene)
    }

    init {
        rootService.addRefreshables(aquarettoScene, this@AquarettoApplication, leaderBoardScene, lobbyScene)
        showGameScene(aquarettoScene)
        showMenuScene(newGameScene)
        rootService.addBots()
    }

}


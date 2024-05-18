package view

import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import java.awt.Color

class JoinGameScene(private val rootService: RootService) : MenuScene(1920, 1080), Refreshable {

    private val joinGame = Label(
        posX = 690, posY = 80, width = 500, height = 173,
        text = "JOIN A GAME", font = Font(size = 70),
        alignment = Alignment.CENTER
    )





    val playerName = Label(628, 261, width = 400, text = "YOUR NAME", font = Font(size = 40), alignment = Alignment.CENTER_LEFT)
    val playerNameField = TextField(628, 328, 664, 85, font = Font(size = 40))

    val sessionID = Label(628, 480, width = 800, text = "SESSION ID", font = Font(size = 30), alignment = Alignment.CENTER_LEFT)
    val sessionIDField = TextField(628, 547, 664, 85, font = Font(size = 40))

    val backButton = Button(
        posX = 95, posY = 895, width = 218, height = 120,
        text = "BACK", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113, 113, 113)
    )

    val joinGameButton = Button(
        posX = 1450, posY = 895, width = 400, height = 120,
        text = "JOIN GAME", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113, 113, 113)
    )

    init {
        background = ColorVisual(152, 219, 217)
        opacity = 1.0
        addComponents(
            joinGame, backButton, joinGameButton,
            playerName, playerNameField, sessionID, sessionIDField
        )
    }
}
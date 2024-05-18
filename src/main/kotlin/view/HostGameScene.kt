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

class HostGameScene(private val rootService: RootService) : MenuScene(1920, 1080) {

    private val hostGame = Label(
        posX = 690, posY = 80, width = 500, height = 173,
        text = "HOST A GAME", font = Font(size = 70),
        alignment = Alignment.CENTER
    )

    val hostName =
        Label(830, 261, width = 400, text = "YOUR NAME", font = Font(size = 40), alignment = Alignment.CENTER_LEFT)
    val hostNameField = TextField(628, 328, 664, 85, font = Font(size = 40))

    val playerNum = Label(800, 480, width = 300, text = "NUM. PLAYERS", font = Font(size = 40))
    val playerNumField = TextField(800, 547, 313, 85, font = Font(size = 40))


    val backButton = Button(
        posX = 95, posY = 895, width = 218, height = 120,
        text = "BACK", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113, 113, 113)
    )

    val createGameButton = Button(
        posX = 1450, posY = 895, width = 400, height = 120,
        text = "CREATE GAME", font = Font(size = 40, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113, 113, 113)
    )

    init {
        background = ColorVisual(152, 219, 217)
        opacity = 1.0
        addComponents(
            hostGame, backButton, createGameButton,
            hostName, hostNameField, playerNum, playerNumField
        )
    }
}
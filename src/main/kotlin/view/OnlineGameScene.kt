package view

import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import java.awt.Color

class OnlineGameScene(private val rootService: RootService) : MenuScene(1920, 1080) {

    private val onlineGame = Label(
        posX = 170, posY = 156, width = 700, height = 150,
        text = "ONLINE GAME", font = Font(size = 90),
        alignment = Alignment.CENTER_LEFT
    )

    val hostGameButton = Button(
        posX = 180, posY = 335, width = 370, height = 120,
        text = "HOST GAME", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    )


    val joinGameButton = Button(
        posX = 180, posY = 500, width = 345, height = 120,
        text = "JOIN GAME", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    )
    val backButton = Button(
        posX = 180, posY = 665, width = 218, height = 120,
        text = "BACK", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    )

    init{
        background = ColorVisual(152, 219, 217)
        opacity = 1.0
        addComponents(
            onlineGame, hostGameButton, joinGameButton, backButton
        )
    }
}
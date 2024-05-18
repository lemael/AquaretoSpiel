package view

import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import java.awt.Color

class LocalGameScene(private val rootService: RootService): MenuScene(1920, 1080) {
    private val localGame = Label(
        posX = 170, posY = 156, width = 700, height = 150,
        text = "LOCAL GAME", font = Font(size = 90),
        alignment = Alignment.CENTER_LEFT
    )

     val newGameButton = Button(
        posX = 180, posY = 335, width = 355, height = 120,
        text = "NEW GAME", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    )

    private val resumeGameButton = Button(
        posX = 180, posY = 500, width = 460, height = 120,
        text = "RESUME GAME", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    ).apply {
        onMouseClicked={
            rootService.aquarettoGame =rootService.gameStateService.loadGame()
        }
    }

     val backButton = Button(
        posX = 180, posY = 665, width = 228, height = 120,
        text = "BACK", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    )

    init{
        background = ColorVisual(152, 219, 217)
        opacity = 1.0
        addComponents(
            localGame, newGameButton, resumeGameButton, backButton
        )
    }
}
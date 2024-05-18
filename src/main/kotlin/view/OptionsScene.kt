package view

import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import java.awt.Color

class OptionsScene(private val rootService: RootService): MenuScene(1920, 1080){
    private val optionsHead = Label(
        posX = 170, posY = 156, width = 700, height = 150,
        text = "OPTION", font = Font(size = 110),
        alignment = Alignment.CENTER_LEFT
    )

    val continueButton = Button(
        posX = 180, posY = 335, width = 400, height = 120,
        text = "CONTINUE", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    )

    val saveButton = Button(
        posX = 180, posY = 500, width = 264, height = 120,
        text = "SAVE", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    ).apply {
        onMouseClicked = {
            rootService.gameStateService.safeGame()
        }
    }


    private val quitButton = Button(
        posX = 180, posY = 665, width = 203, height = 120,
        text = "QUIT", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    ).apply {
        onMouseClicked = { System.exit(0) }
    }



    init{
        background = ColorVisual(152, 219, 217)
        opacity = 1.0
        addComponents(
            continueButton,
            saveButton,
            quitButton,
            optionsHead

        )
    }
}
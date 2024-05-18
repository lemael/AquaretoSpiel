package view

import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import java.awt.Color

class NewGameScene(private val rootService: RootService): MenuScene(1920, 1080) {


    private val aquaretto = Label(
        posX = 170, posY = 156, width = 700, height = 150,
        text = "AQUARETTO", font = Font(size = 110),
        alignment = Alignment.CENTER_LEFT
    )

   val localButton = Button(
        posX = 180, posY = 335, width = 247, height = 120,
        text = "LOCAL", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
   )

    val onlineButton = Button(
        posX = 180, posY = 500, width = 264, height = 120,
        text = "ONLINE", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    )


    private val quitButton = Button(
        posX = 180, posY = 665, width = 203, height = 120,
        text = "QUIT", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    ).apply {
        onMouseClicked = { System.exit(0) }
    }

    val helpButton = Button(
        posX = 1766, posY = 933, width = 100, height = 100, visual = ImageVisual("images/MenuSceneImages/question.png")
    )

    init{
        background = ColorVisual(152, 219, 217)
        opacity = 1.0
        addComponents(
            localButton, onlineButton, quitButton, aquaretto //, helpButton
        )
    }
}
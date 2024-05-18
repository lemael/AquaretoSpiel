package view

import entity.DUMB
import entity.HOTSEAT
import entity.SMART
import entity.onlinePlayerIs
import service.RootService
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import java.awt.Color
import kotlin.math.max
import kotlin.random.Random


class GameConfigScene(private val rootService: RootService) : MenuScene(1920, 1080) {

    val gridTextInput =
        GridPane<TextField>(posX = 630, posY = 270, columns = 1, rows = 5, spacing = 20, layoutFromCenter = false)

    val textField1 = TextField(width = 630, height = 85, font = Font(size = 40))
    val textField2 = TextField(width = 630, height = 85, font = Font(size = 40))
    val textField3 = TextField(width = 630, height = 85, font = Font(size = 40))
    val textField4 = TextField(width = 630, height = 85, font = Font(size = 40))
    val textField5 = TextField(width = 630, height = 85, font = Font(size = 40))
    val listPlayer = mutableListOf<String>()
    var listTypes = mutableListOf<Int>()
    val gridPlayerButton =
        GridPane<Button>(posX = 460, posY = 270, columns = 1, rows = 5, spacing = 20, layoutFromCenter = false)

    val p1Button = Button(
        width = 85,
        height = 85,
        visual = ColorVisual(113, 113, 113),
        text = "1",
        font = Font(size = 40, color = Color.white)
    )

    val p2Button = Button(
        width = 85,
        height = 85,
        visual = ColorVisual(113, 113, 113),
        text = "2",
        font = Font(size = 40, color = Color.white)
    )

    val p3Button = Button(
        width = 85,
        height = 85,
        visual = ColorVisual(113, 113, 113),
        text = "3",
        font = Font(size = 40, color = Color.white)
    )

    val p4Button = Button(
        width = 85,
        height = 85,
        visual = ColorVisual(113, 113, 113),
        text = "4",
        font = Font(size = 40, color = Color.white)
    )

    val p5Button = Button(
        width = 85,
        height = 85,
        visual = ColorVisual(113, 113, 113),
        text = "5",
        font = Font(size = 40, color = Color.white)
    )

    val gridRobotButton =
        GridPane<Button>(posX = 1343, posY = 280, columns = 1, rows = 5, spacing = 55, layoutFromCenter = false)

    val p1Robot = Button(width = 50, height = 50, visual = ImageVisual("images/MenuSceneImages/robot_icon.png"))
    val p2Robot = Button(width = 50, height = 50, visual = ImageVisual("images/MenuSceneImages/robot_icon.png"))
    val p3Robot = Button(width = 50, height = 50, visual = ImageVisual("images/MenuSceneImages/robot_icon.png"))
    val p4Robot = Button(width = 50, height = 50, visual = ImageVisual("images/MenuSceneImages/robot_icon.png"))
    val p5Robot = Button(width = 50, height = 50, visual = ImageVisual("images/MenuSceneImages/robot_icon.png"))

    val gridRedRobotButton =
        GridPane<Button>(posX = 1543, posY = 280, columns = 1, rows = 5, spacing = 55, layoutFromCenter = false)


    val p1RedRobot = Button(
        width = 50,
        height = 50,
        visual = ImageVisual("images/MenuSceneImages/robot_red.png")
    ).apply { isVisible = false }

    val p2RedRobot = Button(
        width = 50,
        height = 50,
        visual = ImageVisual("images/MenuSceneImages/robot_red.png")
    ).apply { isVisible = false }

    val p3RedRobot = Button(
        width = 50,
        height = 50,
        visual = ImageVisual("images/MenuSceneImages/robot_red.png")
    ).apply { isVisible = false }

    val p4RedRobot = Button(
        width = 50,
        height = 50,
        visual = ImageVisual("images/MenuSceneImages/robot_red.png")
    ).apply { isVisible = false }

    val p5RedRobot = Button(
        width = 50,
        height = 50,
        visual = ImageVisual("images/MenuSceneImages/robot_red.png")
    ).apply { isVisible = false }

    val gridGreenRobotButton =
        GridPane<Button>(posX = 1443, posY = 280, columns = 1, rows = 5, spacing = 55, layoutFromCenter = false)

    val p1GreenRobot = Button(
        width = 50,
        height = 50,
        visual = ImageVisual("images/MenuSceneImages/robot_green.png")
    ).apply { isVisible = false }

    val p2GreenRobot = Button(
        width = 50,
        height = 50,
        visual = ImageVisual("images/MenuSceneImages/robot_green.png")
    ).apply { isVisible = false }

    val p3GreenRobot = Button(
        width = 50,
        height = 50,
        visual = ImageVisual("images/MenuSceneImages/robot_green.png")
    ).apply { isVisible = false }

    val p4GreenRobot = Button(
        width = 50,
        height = 50,
        visual = ImageVisual("images/MenuSceneImages/robot_green.png")
    ).apply { isVisible = false }

    val p5GreenRobot = Button(
        width = 50,
        height = 50,
        visual = ImageVisual("images/MenuSceneImages/robot_green.png")
    ).apply { isVisible = false }

    private val configureGame = Label(
        posX = 630,
        posY = 100,
        width = 1000,
        height = 173,
        text = "CONFIGURE GAME",
        font = Font(size = 70),
        alignment = Alignment.TOP_LEFT
    )

    private val randomizeButton = Button(
        posX = 460,
        posY = 110,
        width = 70,
        height = 70,
        alignment = Alignment.TOP_CENTER,
        visual = ImageVisual("images/MenuSceneImages/randomize.png")
    )

    val backButton = Button(
        posX = 95,
        posY = 895,
        width = 218,
        height = 120,
        text = "BACK",
        font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER,
        visual = ColorVisual(113, 113, 113)
    )

    val playerButtons = listOf(p1Button, p2Button, p3Button, p4Button, p5Button)
    val textFields = listOf(textField1, textField2, textField3, textField4, textField5)
    val robotIcons = listOf(p1Robot, p2Robot, p3Robot, p4Robot, p5Robot)

    val startButton = Button(
        posX = 1450,
        posY = 895,
        width = 400,
        height = 120,
        text = "START GAME",
        font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER,
        visual = ColorVisual(113, 113, 113)
    )

    init {
        background = ColorVisual(152, 219, 217)
        opacity = 1.0

        val allRedRobots = listOf(p1RedRobot, p2RedRobot, p3RedRobot, p4RedRobot, p5RedRobot)
        val allGreenRobots = listOf(p1GreenRobot, p2GreenRobot, p3GreenRobot, p4GreenRobot, p5GreenRobot)

        gridTextInput[0, 0] = textField1
        gridTextInput[0, 1] = textField2
        gridTextInput[0, 2] = textField3
        gridTextInput[0, 3] = textField4
        gridTextInput[0, 4] = textField5

        gridPlayerButton[0, 0] = p1Button
        gridPlayerButton[0, 1] = p2Button
        gridPlayerButton[0, 2] = p3Button
        gridPlayerButton[0, 3] = p4Button
        gridPlayerButton[0, 4] = p5Button

        gridRobotButton[0, 0] = p1Robot
        gridRobotButton[0, 1] = p2Robot
        gridRobotButton[0, 2] = p3Robot
        gridRobotButton[0, 3] = p4Robot
        gridRobotButton[0, 4] = p5Robot

        gridRedRobotButton[0, 0] = p1RedRobot
        gridRedRobotButton[0, 1] = p2RedRobot
        gridRedRobotButton[0, 2] = p3RedRobot
        gridRedRobotButton[0, 3] = p4RedRobot
        gridRedRobotButton[0, 4] = p5RedRobot

        gridGreenRobotButton[0, 0] = p1GreenRobot
        gridGreenRobotButton[0, 1] = p2GreenRobot
        gridGreenRobotButton[0, 2] = p3GreenRobot
        gridGreenRobotButton[0, 3] = p4GreenRobot
        gridGreenRobotButton[0, 4] = p5GreenRobot

        // Player buttons logic to ensure at least two players
        playerButtons.forEachIndexed { index, button ->
            button.onMouseClicked = {
                val count = max(2, index + 1) // At least 2 players
                textFields.forEachIndexed { i, textField ->
                    textField.isVisible = i < count
                    robotIcons[i].isVisible = i < count
                }
                allRedRobots.forEach { it.isVisible = false }
                allGreenRobots.forEach { it.isVisible = false }
            }
        }

        // General robot button logic
        robotIcons.forEachIndexed { index, robot ->
            robot.onMouseClicked = {
                robot.isVisible = false
                allRedRobots[index].isVisible = true
                allGreenRobots[index].isVisible = true
            }
        }

        // Red robot button logic
        allRedRobots.forEachIndexed { index, redRobot ->
            redRobot.onMouseClicked = {
                redRobot.isVisible = true
                allGreenRobots[index].isVisible = false
                onlinePlayerIs = SMART
            }
        }

        // Green robot button logic
        allGreenRobots.forEachIndexed { index, greenRobot ->
            greenRobot.onMouseClicked = {
                greenRobot.isVisible = true
                allRedRobots[index].isVisible = false
                onlinePlayerIs = DUMB
            }
        }

        randomizeButton.onMouseClicked = {
            // Collect non-empty names from the text fields
            val names = textFields.mapNotNull { it.text.takeIf { name -> name.isNotBlank() } }.toMutableList()

            // Shuffle the names using Fisher-Yates shuffle algorithm for better in-place shuffling
            for (i in names.indices) {
                val j = Random.nextInt(i, names.size) // Get a random index from the current to the end
                // Swap names at indices i and j
                val temp = names[i]
                names[i] = names[j]
                names[j] = temp
            }

            // Clear all text fields
            textFields.forEach { it.text = "" }

            // Redistribute the shuffled names back into the text fields
            for (i in names.indices) {
                if (i < textFields.size) {
                    textFields[i].text = names[i]
                }
            }
        }

        startButton.onMouseClicked = {
            listPlayer.clear()
            listTypes.clear()
            textFields.forEachIndexed { index, textField ->
                if (textField.isVisible && textField.text.isNotBlank()) {
                    listPlayer.add(textField.text)

                    // Determine player type based on the visibility of robot icons
                    val playerType = when {
                        allRedRobots.getOrNull(index)?.isVisible == true && allGreenRobots.getOrNull(index)?.isVisible != true -> SMART
                        allGreenRobots.getOrNull(index)?.isVisible == true && allRedRobots.getOrNull(index)?.isVisible != true -> DUMB
                        else -> HOTSEAT // Default to HOTSEAT if both or neither robot icon is selected
                    }
                    listTypes.add(playerType)
                }
            }
            rootService.gameStateService.initializeGame(listPlayer, listTypes,null)
            println("Final listTypes before game start: $listTypes") // Debug print
            println("Final listPlayers before game start: $listPlayer") // Debug print
        }

        addComponents(
            configureGame, backButton, startButton, randomizeButton,
            gridTextInput, gridPlayerButton, gridRobotButton,
            gridRedRobotButton, gridGreenRobotButton
        )
    }
}
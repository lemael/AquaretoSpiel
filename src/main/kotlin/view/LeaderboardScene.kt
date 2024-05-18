package view

import entity.Player
import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.ListView
import tools.aqua.bgw.components.uicomponents.TableView
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import java.awt.Color

/**
 * This Menu-Scene is displayed after each game. It displays the score
 * and the allows players to start a new game or close the application.
 */
class LeaderboardScene(private val rootService: RootService): MenuScene(1920, 1080), Refreshable{

    val newGameButton = Button(
        posX = 1510, posY = 897, width = 355, height = 120,
        text = "NEW GAME", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    ).apply {
        onMouseClicked = {
            TODO()
        }
    }

    private val quitButton = Button(
        posX = 94, posY = 897, width = 210, height = 119,
        text = "QUIT", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    ).apply {
        onMouseClicked = { System.exit(0) }
    }
    private lateinit var listWinner: ListView<Pair<String, Int>>

    override fun refreshAfterEndGame(results: MutableList<Triple<Player, Int, Int>>) {
        println(results)
        val formatted = mutableListOf<Pair<String, Int>>()
        results.forEach {
            formatted.add(it.first.name to it.second)
        }
        listWinner = ListView(
            posX = 920, posY = 800, width = 300, height = 200,
            items = formatted,
            formatFunction = {"${it.first} with a score of ${it.second}"}
        )
        addComponents(listWinner)
    }

    init{
        background = ColorVisual(182, 234, 223)
        addComponents(
             quitButton, newGameButton
        )
    }
}
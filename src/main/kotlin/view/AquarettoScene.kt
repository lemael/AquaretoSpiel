package view

import entity.*
import entity.players.HotseatPlayer
import entity.players.OnlineBGWPlayer
import service.RootService
import service.CardImageLoader
import tools.aqua.bgw.animation.DelayAnimation
import tools.aqua.bgw.components.container.Area
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.layoutviews.CameraPane
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.event.MouseButtonType
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import java.awt.Color
import java.awt.image.BufferedImage

class AquarettoScene(private val rootService: RootService): BoardGameScene(1920, 1080), Refreshable{

    private var anzahlSpieler = 0
    private var currentPlayerName = ""
    private var currentPlayer = 0

    private var mainStackCount = Label(1622, 640, 60, 60, text = "0", font = Font(size = 20))
    private var endStackCount = Label(1760, 640, 60, 60, text = "0", font = Font(size = 20))

    val imageLoader = CardImageLoader()

    // region draw stack
    private val mainStack: Area<TokenView> = Area(
        posX = 1620, posY = 590, width = 63, height = 63,
        visual = ImageVisual("images/gameSceneImages/tiere/rueckseite.png")
    )

    private val endStack = Label (
        posX = 1755, posY = 590, width = 63, height = 63,
        visual = ImageVisual("images/gameSceneImages/tiere/rueckseite.png")
    )

    private var mainStackToken = TokenView(
        posX = 1620, posY = 590, width = 63, height = 63,
        visual = ImageVisual("images/gameSceneImages/tiere/rueckseite.png")
    ).apply{
        isDraggable = true
        onDragGestureStarted = {
            visual = ImageVisual(imageLoader.frontImageWagonable(rootService.aquarettoGame.board.mainStack.last()))
        }
    }

    private var endStackToken = TokenView(
        posX = 1755, posY = 590, width = 63, height = 63,
        visual = ImageVisual("images/gameSceneImages/tiere/rueckseite.png")
    ).apply {
        isDraggable = false
        onDragGestureStarted = {
            visual = ImageVisual(imageLoader.frontImageWagonable(rootService.aquarettoGame.board.endOfGameStack.last()))
        }
    }

    private fun resetMainStackToken(){
        removeComponents(mainStackToken)
        mainStackToken = TokenView(
            posX = 1620, posY = 590, width = 63, height = 63,
            visual = ImageVisual("images/gameSceneImages/tiere/rueckseite.png")
        ).apply{
            isDraggable = true
            onDragGestureStarted = {
                visual = ImageVisual(imageLoader.frontImageWagonable(rootService.aquarettoGame.board.mainStack.last()))
            }
        }
        addComponents(mainStackToken)
    }
    private fun resetEndStackToken(){
        removeComponents(endStackToken)
        endStackToken = TokenView(
            posX = 1755, posY = 590, width = 63, height = 63,
            visual = ImageVisual("images/gameSceneImages/tiere/rueckseite.png")
        ).apply {
            isDraggable = isEndOfGame
            onDragGestureStarted = {
                visual = ImageVisual(imageLoader.frontImageWagonable(rootService.aquarettoGame.board.endOfGameStack.last()))
            }
        }
        addComponents(endStackToken)
    }
    private var isEndOfGame = false
    // endregion

    // region water park
    private val targetLayout = GridPane<Area<TokenView>>(
        columns = 21, rows = 21, visual = ColorVisual(0,0,0,0),
        layoutFromCenter = true
    )

    private val window = Label(35,35, width = 730, height = 1010 ,visual = ColorVisual(151,191,164))
    private val cameraPane = CameraPane(35,35, width = 730, height = 1010, target = targetLayout)
    private lateinit var lastClickedTile: Pair<Int, Int>

    private fun initializeCamera(){
        cameraPane.zoom = 1.0
        cameraPane.interactive = true
        cameraPane.pan(500, 500, smooth = true)
        cameraPane.panMouseButton = MouseButtonType.LEFT_BUTTON

        initializePark()
    }

    private fun initializePark(){
        targetLayout.setRowHeights(63)
        targetLayout.setColumnWidths(63)

        for(i in 0 until 20){
            for(j in 0 until 20){
                targetLayout[i,j] = Area(visual = ColorVisual(0,0,0,0), width = 63, height = 63)
            }
        }

        targetLayout[9,8] = Area(visual = ImageVisual("images/gameSceneImages/tile.png"), width = 63, height = 63)
        targetLayout[10,8] = Area(visual = ImageVisual("images/gameSceneImages/tile.png"), width = 63, height = 63)
        targetLayout[11,8] = Area(visual = ImageVisual("images/gameSceneImages/tile.png"), width = 63, height = 63)

        targetLayout[8,9] = Area(visual = ImageVisual("images/gameSceneImages/tile.png"), width = 63, height = 63)
        targetLayout[9,9] = Area(visual = ImageVisual("images/gameSceneImages/tile.png"), width = 63, height = 63)
        targetLayout[10,9] = Area(visual = ImageVisual("images/gameSceneImages/tile.png"), width = 63, height = 63)
        targetLayout[11,9] = Area(visual = ImageVisual("images/gameSceneImages/tile.png"), width = 63, height = 63)
        targetLayout[12,9] = Area(visual = ImageVisual("images/gameSceneImages/tile.png"), width = 63, height = 63)

        targetLayout[8,10] = Area(visual = ImageVisual("images/gameSceneImages/tile.png"), width = 63, height = 63)
        targetLayout[9,10] = Area(visual = ImageVisual("images/gameSceneImages/tile.png"), width = 63, height = 63)
        targetLayout[10,10] = Area(visual = ImageVisual("images/gameSceneImages/tile.png"), width = 63, height = 63)
        targetLayout[11,10] = Area(visual = ImageVisual("images/gameSceneImages/tile.png"), width = 63, height = 63)
        targetLayout[12,10] = Area(visual = ImageVisual("images/gameSceneImages/tile.png"), width = 63, height = 63)

        targetLayout[8,11] = Area(visual = ImageVisual("images/gameSceneImages/tile_dirt.png"), width = 63, height = 63)
        targetLayout[9,11] = Area(visual = ImageVisual("images/gameSceneImages/tile.png"), width = 63, height = 63)
        targetLayout[10,11] = Area(visual = ImageVisual("images/gameSceneImages/tile.png"), width = 63, height = 63)
        targetLayout[11,11] = Area(visual = ImageVisual("images/gameSceneImages/tile.png"), width = 63, height = 63)
        targetLayout[12,11] = Area(visual = ImageVisual("images/gameSceneImages/tile.png"), width = 63, height = 63)

        targetLayout[8,12] = Area(visual = ImageVisual("images/gameSceneImages/tile_dirt.png"), width = 63, height = 63)
        targetLayout[9,12] = Area(visual = ImageVisual("images/gameSceneImages/tile_dirt.png"), width = 63, height = 63)
        targetLayout[10,12] = Area(visual = ImageVisual("images/gameSceneImages/tile.png"), width = 63, height = 63)
        targetLayout[11,12] = Area(visual = ImageVisual("images/gameSceneImages/tile.png"), width = 63, height = 63)



        for(element in targetLayout)
        {
            element.component?.dropAcceptor = {true}
            element.component?.onDragDropped = { dragEvent ->
                when (dragEvent.draggedComponent) {
                    grosseTafel -> {
                        dragEvent.draggedComponent.apply { reposition(800, 770)}
                        hideGrosseTafel()
                        // call to service layer
                        val topLeftService = guiCoordsToServiceCoords(element.columnIndex to element.rowIndex)
                        rootService.playerActionService.addLargeExtension(topLeftService.first to topLeftService.second-1)
                    }
                    kleineTafelTL -> {
                        dragEvent.draggedComponent.apply { reposition(1100, 770)}
                        hideKleineTafeln()
                        // call to service layer
                        val topLeftService = guiCoordsToServiceCoords(element.columnIndex to element.rowIndex)
                        rootService.playerActionService.addSmallExtension(topLeftService.first to topLeftService.second-1, TOP_LEFT)
                    }
                    kleineTafelTR -> {
                        dragEvent.draggedComponent.apply { reposition(1250, 770)}
                        hideKleineTafeln()
                        // call to service layer
                        val topLeftService = guiCoordsToServiceCoords(element.columnIndex to element.rowIndex)
                        rootService.playerActionService.addSmallExtension(topLeftService.first to topLeftService.second-1, TOP_RIGHT)
                    }
                    kleineTafelBL -> {
                        dragEvent.draggedComponent.apply { reposition(800, 770)}
                        hideKleineTafeln()
                        // call to service layer
                        val topLeftService = guiCoordsToServiceCoords(element.columnIndex to element.rowIndex)
                        rootService.playerActionService.addSmallExtension(topLeftService.first to topLeftService.second-1, BOTTOM_LEFT)
                    }
                    kleineTafelBR -> {
                        dragEvent.draggedComponent.apply { reposition(950, 770)}
                        hideKleineTafeln()
                        // call to service layer
                        val topLeftService = guiCoordsToServiceCoords(element.columnIndex to element.rowIndex)
                        rootService.playerActionService.addSmallExtension(topLeftService.first to topLeftService.second-1, BOTTOM_RIGHT)
                    }
                    newWorkerToken -> {
                        newWorkerType = guiCoordsToServiceCoords(element.columnIndex, element.rowIndex)
                        dragEvent.draggedComponent.apply { reposition(350, 785)}
                        rootService.playerActionService.relocateWorker(oldWorkerType, newWorkerType!!)
                    }
                    mainStackToken -> {
                        resetMainStackToken()
                    }
                    endStackToken -> {
                        resetEndStackToken()
                    }
                    is AnimalTokenView -> {
                        val view = dragEvent.draggedComponent as AnimalTokenView
                        val animal = view.type
                        val coords = guiCoordsToServiceCoords(element.columnIndex, element.rowIndex)
                        rootService.playerActionService.animalToPark(animal, coords)
                        cardsToPlay.remove(view)
                        removeComponents(view)
                        if(cardsToPlay.size == 0) rootService.gameStateService.endTurn()
                    }


                    else -> { println(dragEvent.draggedComponent) }
                }
            }
            element.component?.onMouseClicked = {
                lastClickedTile = element.columnIndex to element.rowIndex
                currentSelectedLabel.text = "currently: ${lastClickedTile.first}, ${lastClickedTile.second}"
            }
        }
    }



    // endregion

    // region coord maps
    /**
     * converts the targetLayout coordinates to service layer coordinates
     * 8,8 -> 0,4
     * 12,8 -> 4,4
     * 12,12 -> 4,0
     */
    private fun guiCoordsToServiceCoords(guiX: Int, guiY: Int): Pair<Int, Int>{
        return guiX-8 to 12-guiY
    }
    private fun guiCoordsToServiceCoords(guiCoord: Pair<Int, Int>): Pair<Int, Int>{
        return guiCoord.first-8 to 12-guiCoord.second
    }
    private fun serviceCoordsToGuiCoords(serX: Int, serY: Int): Pair<Int, Int>{
        return serX+8 to 12-serY
    }
    private fun serviceCoordsToGuiCoords(serCoord: Pair<Int, Int>): Pair<Int, Int>{
        return serCoord.first+8 to 12-serCoord.second
    }
    // endregion

    // region Menu options
    private val depotButton = Button(
        posX = 800, posY = 770, width = 240, height = 120,
        text = "DEPOT", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    ).apply {
        onMouseClicked = {
            hideTurnOptions()
            showDepotOptions()}
    }

    private val depotBuy = Button(
        posX = 800, posY = 770, width = 674, height = 120,
        text = "DEPOT KARTE KAUFEN", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    ).apply {
        onMouseClicked = {
            hideDepotOptions()
            showPlayerSelector()

        }
    }

    private val playerSelector = listOf(
        Button(
            posX = 800, posY = 770, width = 120, height = 120,
            text = "2", font = Font(size = 20, color = Color.white),
            alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
        ).apply {
            onMouseClicked = {
                // Get the current player index dynamically, if needed
                val currentPlayer = rootService.aquarettoGame.currentPlayer
                // Calculate the index of the next player.
                val targetPlayerIndex = (currentPlayer + 1) % rootService.aquarettoGame.players.size

                // Retrieve the next player.
                val buyFromPlayer = rootService.aquarettoGame.players[targetPlayerIndex]

                // Call the buyCard method with the next player.
                rootService.playerActionService.buyCard(buyFromPlayer)
            }
        },
        Button(
            posX = 960, posY = 770, width = 120, height = 120,
            text = "3", font = Font(size = 20, color = Color.white),
            alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
        ).apply {
            onMouseClicked = {
                val currentPlayer = rootService.aquarettoGame.currentPlayer

                val targetPlayerIndex = (currentPlayer + 2) % rootService.aquarettoGame.players.size

                val buyFromPlayer = rootService.aquarettoGame.players[targetPlayerIndex]

                rootService.playerActionService.buyCard(buyFromPlayer)
            }
        },
        Button(
            posX = 1120, posY = 770, width = 120, height = 120,
            text = "4", font = Font(size = 20, color = Color.white),
            alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
        ).apply {
            onMouseClicked = {
                val currentPlayer = rootService.aquarettoGame.currentPlayer

                val targetPlayerIndex = (currentPlayer + 3) % rootService.aquarettoGame.players.size

                val buyFromPlayer = rootService.aquarettoGame.players[targetPlayerIndex]

                rootService.playerActionService.buyCard(buyFromPlayer)
            }
        },
        Button(
            posX = 1280, posY = 770, width = 120, height = 120,
            text = "5", font = Font(size = 20, color = Color.white),
            alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
        ).apply {
            onMouseClicked = {
                val currentPlayer = rootService.aquarettoGame.currentPlayer

                val targetPlayerIndex = (currentPlayer + 4) % rootService.aquarettoGame.players.size

                val buyFromPlayer = rootService.aquarettoGame.players[targetPlayerIndex]

                rootService.playerActionService.buyCard(buyFromPlayer)
            }
        }
    )

    private val depotSell = Button(
        posX = 800, posY = 925, width = 690, height = 120,
        text = "DEPOT KARTE ABGEBEN", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    ).apply {
        onMouseClicked = {
            rootService.playerActionService.sellCard()
            rootService.gameStateService.endTurn()
        }
    }

    private val versetzenButton = Button(
        posX = 1080, posY = 770, width = 370, 120,
        text = "VERSETZEN", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    ).apply {
        onMouseClicked = {
            hideTurnOptions()
            showVersetzenOptions()
        }
    }

    private val versetzenMitarbeiter = Button(
        posX = 800, posY = 770, width = 760, height = 120,
        text = "MITARBEITER VERSETZEN", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    ).apply {
        onMouseClicked = {
            endTurnAfterRelocation = true
            hideWorkerElements()
            startOldWorkerSelections()
        }
    }

    private val versetzenDepotTier = Button(
        posX = 800, posY = 925, width = 770, height = 120,
        text = "DEPOT KARTE VERSETZEN", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    ).apply{
        onMouseClicked = {
            rootService.playerActionService.depotToPark()
        }
    }

    private val ausbauenButton = Button(
        posX = 800, posY = 925, width = 360, 120,
        text = "AUSBAUEN", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    ).apply {
        onMouseClicked = {
            hideTurnOptions()
            showAusbauenOptions()
        }
    }

    private val ausbauenGross = Button(
        posX = 800, posY = 770, width = 300, height = 120,
        text = "GROß", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    ).apply {
        onMouseClicked = {
            hideAusbauenOptions()
            showGrosseTafel()
        }
    }

    private val grosseTafel: TokenView = TokenView (
        posX = 800, posY = 770, width = 100, height = 100,
        visual = ImageVisual("images/gameSceneImages/ausbautafel_gross.png")
    ).apply {
        isDraggable = true
        onDragGestureStarted = {visual = ImageVisual("images/gameSceneImages/ausbautafel_gross.png")}
    }

    private val ausbauenKlein = Button(
        posX = 800, posY = 925, width = 300, height = 120,
        text = "KLEIN", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    ).apply {
        onMouseClicked = {
            hideAusbauenOptions()
            showKleineTafel()
        }
    }

    private val kleineTafelBL:  TokenView = TokenView (
        posX = 800, posY = 770, width = 100, height = 100,
        visual = ImageVisual("images/gameSceneImages/ausbautafel_klein_BL.png")
    ).apply {
        isDraggable = true
        onDragGestureStarted = {visual = ImageVisual("images/gameSceneImages/ausbautafel_klein_BL.png")}
    }

    private val kleineTafelBR:  TokenView = TokenView (
        posX = 950, posY = 770, width = 100, height = 100,
        visual = ImageVisual("images/gameSceneImages/ausbautafel_klein_BR.png")
    ).apply {
        isDraggable = true
        onDragGestureStarted = {visual = ImageVisual("images/gameSceneImages/ausbautafel_klein_BR.png")}
    }

    private val kleineTafelTL:  TokenView = TokenView (
        posX = 1100, posY = 770, width = 100, height = 100,
        visual = ImageVisual("images/gameSceneImages/ausbautafel_klein_TL.png")
    ).apply {
        isDraggable = true
        onDragGestureStarted = {visual = ImageVisual("images/gameSceneImages/ausbautafel_klein_TL.png")}
    }

    private val kleineTafelTR:  TokenView = TokenView (
        posX = 1250, posY = 770, width = 100, height = 100,
        visual = ImageVisual("images/gameSceneImages/ausbautafel_klein_TR.png")
    ).apply {
        isDraggable = true
        onDragGestureStarted = {visual = ImageVisual("images/gameSceneImages/ausbautafel_klein_TR.png")}
    }

    private val label = Label(
        posX = 1200, posY = 925, width = 250, height =120,
        visual = ColorVisual(113,113,113)
    )

    private val undoButton = Button(
        posX = 1230, posY = 950, width = 70, 70,
        visual = ImageVisual("images/gameSceneImages/left_arrow.png"),
        alignment = Alignment.CENTER
    ).apply {
        onMouseClicked = {
            rootService.gameStateService.revertLastCommand()
        }
    }

    private val redoButton = Button(
        posX = 1350, posY = 950, width = 70, 70,
        visual = ImageVisual("images/gameSceneImages/right_arrow.png"),
        alignment = Alignment.CENTER
    ).apply {
        onMouseClicked = {
            rootService.gameStateService.applyLastReverted()
        }
    }

    private val returnButton = Button(
        posX = 1640, posY = 770, width = 100, height = 100,
        visual = ImageVisual("images/gameSceneImages/return.png"),
        alignment = Alignment.CENTER
    ).apply {
        onMouseClicked = {
            showTurnOptions()
            hideDepotOptions()
            hideVersetzenOptions()
            hideAusbauenOptions()
            hidePlayerSelector()
            hideGrosseTafel()
            hideKleineTafeln()
        }
    }

    val helpButton = Button(
        posX = 1640, posY = 933, width = 100, height = 100, visual = ImageVisual("images/MenuSceneImages/question.png")
    )

    val settingsButton = Button(
        posX = 1766, posY = 925, width = 115, height = 115, visual = ImageVisual("images/gameSceneImages/cog.png")
    )

    val oopsButton = Button(
        posX = 650, posY = 908, width = 90, height = 100, visual = ImageVisual("images/gameSceneImages/oops.png")
    ).apply {
        onMouseClicked = {
            refreshAfterEndTurn(false, false)
        }
    }
    // endregion

    private val line1 = Label(
        posX = 800, posY = 730, width = 1078, height = 4, visual = ColorVisual(113,113,113)
    )

    private val line2 = Label(
        posX = 800, posY = 320, width = 1078, height = 4, visual = ColorVisual(113,113,113)
    )

    private val cardLabel = Label(
        posX = 1590, posY = 560, width = 260, height = 127, visual = ColorVisual(234,248,247)
    )

    /**
     * a list of labels containing information about the player
     * 1) number of coins
     * 2) large extensions remaining
     * 3) small extensions remaining
     * 4) number of cashiers
     * 5) number of nurses
     * 6) manager (yes/no)
     */
    private val playerStats = listOf(
        Label(155, 65, 100, height = 64, text = "X 2", font = Font(size = 50, color = Color.WHITE)),
        Label(155, 152, 100, height = 64, text = "X 2", font = Font(size = 50, color = Color.WHITE)),
        Label(155, 244, 100, 64, text = "X 2", font = Font(size = 50, color = Color.WHITE)),
        Label(63, 336, 200, 64, text = "cashiers: X 0", font = Font(size = 30, color = Color.WHITE)),
        Label(55, 428, 200, 64, text = "nurses: X 0", font = Font(size = 30, color = Color.WHITE)),
        Label(60, 520, 200, 64, text = "manager: no", font = Font(size = 30, color = Color.WHITE))
    )

    private val playerStatsImgs = listOf(
        Label(68,65,64,64, visual = ImageVisual("images/gameSceneImages/coin.png")),
        Label(68, 152, 64, 64, visual = ImageVisual("images/gameSceneImages/ausbautafel_gross.png")),
        Label(68, 244, 64, 64, visual = ImageVisual("images/gameSceneImages/ausbautafel_klein.png"))
    )

    private val depot: AnimalArea = AnimalArea(
        65, 908, 115, 115,
        visual = ImageVisual("images/gameSceneImages/depot.png"),
        null
    )

    private var currentPlayerNameLabel = Label(
        545, 35, 300, 64, font = Font(size = 50, color = Color.WHITE),
    )

    // region wagons

    /**
     * Wagon 1
     */
    private val wagon1 = Button(
        posX = 836, posY = 385, width = 262, height = 110, visual = ImageVisual("images/gameSceneImages/transportwagen.png")
    ).apply {
        onMouseClicked = {
            rootService.playerActionService.takeWagon(guiWagonNumberToServiceWagonIndex(1))
            if(rootService.aquarettoGame.currentPlayer().cardsToPlay.size == 0){
                rootService.gameStateService.endTurn()
            }
            hideTurnOptions()

        }
    }
    private var wagon1Deactive = false

    private val wagon1Area1: Area<TokenView> = Area(
        height = 63, width = 63, posX = 904, posY = 387, visual = ColorVisual(0, 0, 0, 0)
    )

    private val wagon1Area2: Area<TokenView> = Area(
        height = 63, width = 63, posX = 967, posY = 387, visual = ColorVisual(0, 0, 0, 0)
    )

    private val wagon1Area3: Area<TokenView> = Area(
        height = 63, width = 63, posX = 1029, posY = 387, visual = ColorVisual(0, 0, 0, 0)
    )

    /**
     * Wagon 2
     */
    private val wagon2 = Button(
        posX = 836, posY = 563, width = 262, height = 110, visual = ImageVisual("images/gameSceneImages/transportwagen.png")
    ).apply {
        onMouseClicked = {
            rootService.playerActionService.takeWagon(guiWagonNumberToServiceWagonIndex(2))
            if(rootService.aquarettoGame.currentPlayer().cardsToPlay.size == 0){
                rootService.gameStateService.endTurn()
            }
            hideTurnOptions()
        }
    }
    private var wagon2Deactive = false

    private val wagon2Area1: Area<TokenView> = Area(
        height = 63, width = 63, posX = 904, posY = 567, visual = ColorVisual(0, 0, 0, 0)
    )

    private val wagon2Area2: Area<TokenView> = Area(
        height = 63, width = 63, posX = 967, posY = 567, visual = ColorVisual(0, 0, 0, 0)
    )

    private val wagon2Area3: Area<TokenView> = Area(
        height = 63, width = 63, posX = 1029, posY = 567, visual = ColorVisual(0, 0, 0, 0)
    )

    /**
     * Wagon 3
     */

    private val wagon3 = Button(
        posX = 1220, posY = 385, width = 262, height = 110, visual = ImageVisual("images/gameSceneImages/transportwagen.png")
    ).apply {
        onMouseClicked = {
            rootService.playerActionService.takeWagon(guiWagonNumberToServiceWagonIndex(3))
            if(rootService.aquarettoGame.currentPlayer().cardsToPlay.size == 0){
                rootService.gameStateService.endTurn()
            }
            hideTurnOptions()
        }
    }
    private var wagon3Deactive = false

    private val wagon3Area1: Area<TokenView> = Area(
        height = 63, width = 63, posX = 1288, posY = 387, visual = ColorVisual(0, 0, 0, 0)
    )

    private val wagon3Area2: Area<TokenView> = Area(
        height = 63, width = 63, posX = 1351, posY = 387, visual = ColorVisual(0, 0, 0, 0)
    )

    private val wagon3Area3: Area<TokenView> = Area(
        height = 63, width = 63, posX = 1414, posY = 387, visual = ColorVisual(0, 0, 0, 0)
    )

    /**
     * Wagon 4
     */

    private val wagon4 = Button(
        posX = 1220, posY = 563, width = 262, height = 110, visual = ImageVisual("images/gameSceneImages/transportwagen.png")
    ).apply {
        onMouseClicked = {
            rootService.playerActionService.takeWagon(guiWagonNumberToServiceWagonIndex(4))
            if(rootService.aquarettoGame.currentPlayer().cardsToPlay.size == 0){
                rootService.gameStateService.endTurn()
            }
            hideTurnOptions()
        }
    }
    private var wagon4Deactive = false

    private val wagon4Area1: Area<TokenView> = Area(
        height = 63, width = 63, posX = 1288, posY = 567, visual = ColorVisual(0, 0, 0, 0)
    )

    private val wagon4Area2: Area<TokenView> = Area(
        height = 63, width = 63, posX = 1351, posY = 567, visual = ColorVisual(0, 0, 0, 0)
    )

    private val wagon4Area3: Area<TokenView> = Area(
        height = 63, width = 63, posX = 1414, posY = 567, visual = ColorVisual(0, 0, 0, 0)
    )

    /**
     * Wagon 5
     */
    private val wagon5 = Button(
        posX = 1589, posY = 385, width = 262, height = 110, visual = ImageVisual("images/gameSceneImages/transportwagen.png")
    ).apply {
        onMouseClicked = {
            rootService.playerActionService.takeWagon(guiWagonNumberToServiceWagonIndex(5))
            if(rootService.aquarettoGame.currentPlayer().cardsToPlay.size == 0){
                rootService.gameStateService.endTurn()
            }
            hideTurnOptions()
        }
    }
    private var wagon5Deactive = false

    private val wagon5Area1: Area<TokenView> = Area(
        height = 63, width = 63, posX = 1657, posY = 387, visual = ColorVisual(0, 0, 0, 0)
    )

    private val wagon5Area2: Area<TokenView> = Area(
        height = 63, width = 63, posX = 1720, posY = 387, visual = ColorVisual(0, 0, 0, 0)
    )

    private val wagon5Area3: Area<TokenView> = Area(
        height = 63, width = 63, posX = 1782, posY = 387, visual = ColorVisual(0, 0, 0, 0)
    )

    private fun serviceWagonIndexToGUIWagonNumber(serIndex: Int): Int{
        val allActive = mutableListOf<Int>()
        if(!wagon1Deactive) allActive.add(1)
        if(!wagon2Deactive) allActive.add(2)
        if(!wagon3Deactive) allActive.add(3)
        if(!wagon4Deactive) allActive.add(4)
        if(!wagon5Deactive) allActive.add(5)
        return allActive[serIndex]
    }
    private fun guiWagonNumberToServiceWagonIndex(guiNumber: Int): Int{
        val number = guiNumber
        var index = 0
        if(number==1){
            require(!wagon1Deactive){"the service layer does not store inactive wagons"}
            return index
        }
        if(!wagon1Deactive) index++
        if(number==2){
            require(!wagon2Deactive){"the service layer does not store inactive wagons"}
            return index
        }
        if(!wagon2Deactive) index++
        if(number==3){
            require(!wagon3Deactive){"the service layer does not store inactive wagons"}
            return index
        }
        if(!wagon3Deactive) index++
        if(number==4){
            require(!wagon4Deactive){"the service layer does not store inactive wagons"}
            return index
        }
        if(!wagon4Deactive) index++
        if(number==5){
            require(!wagon5Deactive){"the service layer does not store inactive wagons"}
            return index
        }
        else return -1
    }

    // endregion


    // region parks

    private val park2 = Label(
        posX = 1040 - 200,
        posY = 44,
        width = 202,
        height = 208,
        visual = ImageVisual("images/gameSceneImages/wasserpark_icon.png"),
        text = "Player 2",
        font = Font(size = 32)
    )

    private val depot2: TokenView = TokenView(
        posX = 810, posY = 216, width = 63, height = 63,
        visual = ColorVisual(Color.WHITE)
    )


    private val park3 = Label(
        posX = 1320 - 200,
        posY = 44,
        width = 202,
        height = 208,
        visual = ImageVisual("images/gameSceneImages/wasserpark_icon.png"),
        text = "Player 3",
        font = Font(size = 32)
    )

    private val depot3: TokenView = TokenView(
        posX = 1090, posY = 216, width = 63, height = 63,
        visual = ColorVisual(Color.WHITE)
    )


    private val park4 = Label(
        posX = 1600 - 200,
        posY = 44,
        width = 202,
        height = 208,
        visual = ImageVisual("images/gameSceneImages/wasserpark_icon.png"),
        text = "Player 4",
        font = Font(size = 32)
    )

    private val depot4: TokenView = TokenView(
        posX = 1370, posY = 216, width = 63, height = 63,
        visual = ColorVisual(Color.WHITE)
    )


    private val park5 = Label(
        posX = 1870 - 200,
        posY = 44,
        width = 202,
        height = 208,
        visual = ImageVisual("images/gameSceneImages/wasserpark_icon.png"),
        text = "Player 5",
        font = Font(size = 32)
    )

    private val depot5: TokenView = TokenView(
        posX = 1640, posY = 216, width = 63, height = 63,
        visual = ColorVisual(Color.WHITE)
    )

    // endregion


    // region hide/show
    private fun showTurnOptions(){
        addComponents(depotButton, versetzenButton, ausbauenButton, label, undoButton, redoButton)
    }

    private fun hideTurnOptions(){
        removeComponents(depotButton, versetzenButton, ausbauenButton, undoButton, redoButton, label)
    }

    private fun showVersetzenOptions(){
        addComponents(versetzenMitarbeiter, versetzenDepotTier, returnButton)
    }

    private fun hideVersetzenOptions(){
        removeComponents(versetzenMitarbeiter, versetzenDepotTier, returnButton)
    }

    private fun showDepotOptions(){
        addComponents(depotBuy, depotSell, returnButton)
    }

    private fun hideDepotOptions(){
        removeComponents(depotBuy, depotSell, returnButton)
    }

    private fun showPlayerSelector(){
        if(anzahlSpieler > 1) { addComponents(playerSelector[0], returnButton) }
        if(anzahlSpieler > 2) { addComponents(playerSelector[1]) }
        if(anzahlSpieler > 3) { addComponents(playerSelector[2]) }
        if(anzahlSpieler > 4) { addComponents(playerSelector[3]) }
    }

    private fun hidePlayerSelector(){
        if(anzahlSpieler > 1) { removeComponents(playerSelector[0], returnButton) }
        if(anzahlSpieler > 2) { removeComponents(playerSelector[1]) }
        if(anzahlSpieler > 3) { removeComponents(playerSelector[2]) }
        if(anzahlSpieler > 4) { removeComponents(playerSelector[3]) }
    }

    private fun showAusbauenOptions(){
        addComponents(ausbauenGross, ausbauenKlein, returnButton)
    }

    private fun hideAusbauenOptions(){
        removeComponents(ausbauenGross, ausbauenKlein, returnButton)
    }

    private fun showGrosseTafel(){
        addComponents(grosseTafel, returnButton)
    }

    private fun hideGrosseTafel(){
        removeComponents(grosseTafel, returnButton)
    }

    private fun showKleineTafel(){
        addComponents(kleineTafelBL, kleineTafelBR, kleineTafelTL, kleineTafelTR, returnButton)
    }

    private fun hideKleineTafeln(){
        removeComponents(kleineTafelBL, kleineTafelBR, kleineTafelTL, kleineTafelTR, returnButton)
    }
    // endregion

    private fun setVisuals(){
        if(anzahlSpieler > 1) {
            addComponents(
                wagon1, wagon1Area1, wagon1Area2, wagon1Area3,
                wagon2, wagon2Area1, wagon2Area2, wagon2Area3,
                wagon3, wagon3Area1, wagon3Area2, wagon3Area3,
                park2, depot2
            )
        }
        if (anzahlSpieler > 2) {
            addComponents(
                park3, depot3
            )
        }
        if (anzahlSpieler > 3) {
            addComponents(
                wagon4, wagon4Area1, wagon4Area2, wagon4Area3,
                park4, depot4
            )
        }
        if (anzahlSpieler > 4) {
            addComponents(
                wagon5, wagon5Area1, wagon5Area2, wagon5Area3,
                park5, depot5
            )
        }
    }


    private fun setDropAcceptors(){
        /**
         *  Wagon 1
         */
        wagon1Area1.dropAcceptor = { true }
        wagon1Area1.onDragDropped = {
            println("dropped")
            when(it.draggedComponent){
                mainStackToken -> resetMainStackToken()
                endStackToken -> resetEndStackToken()
                else -> { println("something unexpected was dropped") }
            }
            val index = guiWagonNumberToServiceWagonIndex(1)
            rootService.playerActionService.putCardOnWagon(index)
            rootService.gameStateService.endTurn()
        }
        wagon1Area2.dropAcceptor = { true }
        wagon1Area2.onDragDropped = {
            println("dropped")
            when(it.draggedComponent){
                mainStackToken -> resetMainStackToken()
                endStackToken -> resetEndStackToken()
                else -> { println("something unexpected was dropped") }
            }
            val index = guiWagonNumberToServiceWagonIndex(1)
            rootService.playerActionService.putCardOnWagon(index)
            rootService.gameStateService.endTurn()
        }
        wagon1Area3.dropAcceptor = { true }
        wagon1Area3.onDragDropped = {
            println("dropped")
            when(it.draggedComponent){
                mainStackToken -> resetMainStackToken()
                endStackToken -> resetEndStackToken()
                else -> { println("something unexpected was dropped") }
            }
            val index = guiWagonNumberToServiceWagonIndex(1)
            rootService.playerActionService.putCardOnWagon(index)
            rootService.gameStateService.endTurn()
        }

        /**
         * Wagon 2
         */
        wagon2Area1.dropAcceptor = { true }
        wagon2Area1.onDragDropped = {
            when(it.draggedComponent){
                mainStackToken -> resetMainStackToken()
                endStackToken -> resetEndStackToken()
                else -> { println("something unexpected was dropped") }
            }
            val index = guiWagonNumberToServiceWagonIndex(2)
            rootService.playerActionService.putCardOnWagon(index)
            rootService.gameStateService.endTurn()
        }
        wagon2Area2.dropAcceptor = { true }
        wagon2Area2.onDragDropped = {
            when(it.draggedComponent){
                mainStackToken -> resetMainStackToken()
                endStackToken -> resetEndStackToken()
                else -> { println("something unexpected was dropped") }
            }
            val index = guiWagonNumberToServiceWagonIndex(2)
            rootService.playerActionService.putCardOnWagon(index)
            rootService.gameStateService.endTurn()
        }
        wagon2Area3.dropAcceptor = { true }
        wagon2Area3.onDragDropped = {
            when(it.draggedComponent){
                mainStackToken -> resetMainStackToken()
                endStackToken -> resetEndStackToken()
                else -> { println("something unexpected was dropped") }
            }
            val index = guiWagonNumberToServiceWagonIndex(2)
            rootService.playerActionService.putCardOnWagon(index)
            rootService.gameStateService.endTurn()
        }

        /**
         * Wagon 3
         */
        wagon3Area1.dropAcceptor = { true }
        wagon3Area1.onDragDropped = {
            when(it.draggedComponent){
                mainStackToken -> resetMainStackToken()
                endStackToken -> resetEndStackToken()
                else -> { println("something unexpected was dropped") }
            }
            val index = guiWagonNumberToServiceWagonIndex(3)
            rootService.playerActionService.putCardOnWagon(index)
            rootService.gameStateService.endTurn()
        }
        wagon3Area2.dropAcceptor = { true }
        wagon3Area2.onDragDropped = {
            when(it.draggedComponent){
                mainStackToken -> resetMainStackToken()
                endStackToken -> resetEndStackToken()
                else -> { println("something unexpected was dropped") }
            }
            val index = guiWagonNumberToServiceWagonIndex(3)
            rootService.playerActionService.putCardOnWagon(index)
            rootService.gameStateService.endTurn()
        }
        wagon3Area3.dropAcceptor = { true }
        wagon3Area3.onDragDropped = {
            when(it.draggedComponent){
                mainStackToken -> resetMainStackToken()
                endStackToken -> resetEndStackToken()
                else -> { println("something unexpected was dropped") }
            }
            val index = guiWagonNumberToServiceWagonIndex(3)
            rootService.playerActionService.putCardOnWagon(index)
            rootService.gameStateService.endTurn()
        }

        /**
         * Wagon 4
         */
        wagon4Area1.dropAcceptor = { true }
        wagon4Area1.onDragDropped = {
            when(it.draggedComponent){
                mainStackToken -> resetMainStackToken()
                endStackToken -> resetEndStackToken()
                else -> { println("something unexpected was dropped") }
            }
            val index = guiWagonNumberToServiceWagonIndex(4)
            rootService.playerActionService.putCardOnWagon(index)
            rootService.gameStateService.endTurn()
        }
        wagon4Area2.dropAcceptor = { true }
        wagon4Area2.onDragDropped = {
            when(it.draggedComponent){
                mainStackToken -> resetMainStackToken()
                endStackToken -> resetEndStackToken()
                else -> { println("something unexpected was dropped") }
            }
            val index = guiWagonNumberToServiceWagonIndex(4)
            rootService.playerActionService.putCardOnWagon(index)
            rootService.gameStateService.endTurn()
        }
        wagon4Area3.dropAcceptor = { true }
        wagon4Area3.onDragDropped = {
            when(it.draggedComponent){
                mainStackToken -> resetMainStackToken()
                endStackToken -> resetEndStackToken()
                else -> { println("something unexpected was dropped") }
            }
            val index = guiWagonNumberToServiceWagonIndex(4)
            rootService.playerActionService.putCardOnWagon(index)
            rootService.gameStateService.endTurn()
        }

        /**
         * Wagon 5
         */
        wagon5Area1.dropAcceptor = { true }
        wagon5Area1.onDragDropped = {
            when(it.draggedComponent){
                mainStackToken -> resetMainStackToken()
                endStackToken -> resetEndStackToken()
                else -> { println("something unexpected was dropped") }
            }
            val index = guiWagonNumberToServiceWagonIndex(5)
            rootService.playerActionService.putCardOnWagon(index)
            rootService.gameStateService.endTurn()
        }
        wagon5Area2.dropAcceptor = { true }
        wagon5Area2.onDragDropped = {
            when(it.draggedComponent){
                mainStackToken -> resetMainStackToken()
                endStackToken -> resetEndStackToken()
                else -> { println("something unexpected was dropped") }
            }
            val index = guiWagonNumberToServiceWagonIndex(5)
            rootService.playerActionService.putCardOnWagon(index)
            rootService.gameStateService.endTurn()
        }
        wagon5Area3.dropAcceptor = { true }
        wagon5Area3.onDragDropped = {
            when(it.draggedComponent){
                mainStackToken -> resetMainStackToken()
                endStackToken -> resetEndStackToken()
                else -> { println("something unexpected was dropped") }
            }
            val index = guiWagonNumberToServiceWagonIndex(5)
            rootService.playerActionService.putCardOnWagon(index)
            rootService.gameStateService.endTurn()
        }

        /**
         * Draw stack
         */
        mainStack.dropAcceptor= { dragEvent -> true }
        mainStack.onDragDropped = { dragEvent ->
            mainStack.add((dragEvent.draggedComponent as TokenView).apply { reposition(0, 0) })
        }

        depot.dropAcceptor = { true }
        depot.onDragDropped = { dragEvent ->
            val view = dragEvent.draggedComponent as AnimalTokenView
            val animal = view.type
            rootService.playerActionService.animalToDepot(animal)
            cardsToPlay.remove(view)
            removeComponents(view)

            if(cardsToPlay.size == 0) rootService.gameStateService.endTurn()
        }


    }

    override fun refreshAfterInitializeGame() {
        anzahlSpieler = rootService.aquarettoGame.players.size
        currentPlayer = rootService.aquarettoGame.currentPlayer
        currentPlayerName = rootService.aquarettoGame.players[currentPlayer].name
        setVisuals()
        currentPlayerNameLabel.text = currentPlayerName
        playerStats[0].text = "X " + rootService.aquarettoGame.players[currentPlayer].numCoins.toString()
        playerStats[1].text = "X " + (2 - rootService.aquarettoGame.players[currentPlayer].park.smallExtensions.size).toString()
        playerStats[2].text = "X " + (2 - rootService.aquarettoGame.players[currentPlayer].park.largeExtensions.size).toString()



        //for player 2
        if (rootService.aquarettoGame.players[1].depot.isNotEmpty()) {
            val topDepotItem = rootService.aquarettoGame.players[1].depot.last()

            val image: BufferedImage = if (topDepotItem is Animal) {
                imageLoader.frontImageForTiere(topDepotItem)
            } else {
                imageLoader.frontImageBackImage()
            }

            depot2.apply { visual = ImageVisual(image) }
        }

        park2.text = rootService.aquarettoGame.players[1].name
        playerSelector[0].text = rootService.aquarettoGame.players[1].name

        mainStackCount.text = rootService.aquarettoGame.board.mainStack.size.toString()
        endStackCount.text = rootService.aquarettoGame.board.endOfGameStack.size.toString()

        //for player 3
        if (anzahlSpieler == 3) {
            if (rootService.aquarettoGame.players[2].depot.isNotEmpty()) {
                val topDepotItem = rootService.aquarettoGame.players[2].depot.last()

                val image: BufferedImage = if (topDepotItem is Animal) {
                    imageLoader.frontImageForTiere(topDepotItem)
                } else {
                    imageLoader.frontImageBackImage()
                }

                depot3.apply { visual = ImageVisual(image) }
            }

            park2.text = rootService.aquarettoGame.players[1].name
            park3.text = rootService.aquarettoGame.players[2].name
            playerSelector[0].text = rootService.aquarettoGame.players[1].name
            playerSelector[1].text = rootService.aquarettoGame.players[2].name
        }

        //for player 4
        if (anzahlSpieler == 4) {
            if (rootService.aquarettoGame.players[3].depot.isNotEmpty()) {
                val topDepotItem = rootService.aquarettoGame.players[3].depot.last()

                val image: BufferedImage = if (topDepotItem is Animal) {
                    imageLoader.frontImageForTiere(topDepotItem)
                } else {
                    imageLoader.frontImageBackImage()
                }

                depot4.apply { visual = ImageVisual(image) }
            }

            park2.text = rootService.aquarettoGame.players[1].name
            park3.text = rootService.aquarettoGame.players[2].name
            park4.text = rootService.aquarettoGame.players[3].name
            playerSelector[0].text = rootService.aquarettoGame.players[1].name
            playerSelector[1].text = rootService.aquarettoGame.players[2].name
            playerSelector[2].text = rootService.aquarettoGame.players[3].name
        }

        //for player 5
        if (anzahlSpieler == 5) {
            if (rootService.aquarettoGame.players[4].depot.isNotEmpty()) {
                val topDepotItem = rootService.aquarettoGame.players[4].depot.last()

                val image: BufferedImage = if (topDepotItem is Animal) {
                    imageLoader.frontImageForTiere(topDepotItem)
                } else {
                    imageLoader.frontImageBackImage()
                }

                depot5.apply { visual = ImageVisual(image) }
            }
            park2.text = rootService.aquarettoGame.players[1].name
            park3.text = rootService.aquarettoGame.players[2].name
            park4.text = rootService.aquarettoGame.players[3].name
            park5.text = rootService.aquarettoGame.players[4].name
            playerSelector[0].text = rootService.aquarettoGame.players[1].name
            playerSelector[1].text = rootService.aquarettoGame.players[2].name
            playerSelector[2].text = rootService.aquarettoGame.players[3].name
            playerSelector[3].text = rootService.aquarettoGame.players[4].name
        }

    }

    override fun refreshAfterDepotChanged(cardToDepot: Boolean, isReverting: Boolean, updated: Player) {
        if (updated.depot.isEmpty()) {
            depot.apply { visual = ImageVisual("images/gameSceneImages/depot.png") }
        } else {
            val topDepotItem = updated.depot.last()
            val image: BufferedImage = imageLoader.frontImageForTiere(topDepotItem)
            depot.apply {
                visual = ImageVisual(image)
                type = topDepotItem
            }
        }
    }

    override fun refreshAfterGainedWorker(isReverting: Boolean) {
        // reset type s.t. no old data is used
        // select the type of worker
        // in case of trainer, select location
        endTurnAfterRelocation = false
        if(rootService.aquarettoGame.currentPlayer() is HotseatPlayer) startNewWorkerSelection(oldNotSet = true)
    }
    // region worker buttons
    private val selectOld = false
    private val selectNew = true
    /**
     * true -> new worker will be set
     *
     * false -> old worker will be set
     */
    private var selectWorkerMode: Boolean = selectNew
    private var endTurnAfterRelocation: Boolean = false
    private var oldWorkerType: Pair<Int, Int>? = null
    private var newWorkerType: Pair<Int, Int>? = null

    private val oldWorkerLabel = Label(
        posX = 35, posY = 600, width = 730, height = 60,
        font = Font(size = 35), text = "please choose an old worker"
    )
    private val newWorkerLabel = Label(
        posX = 35, posY = 600, width = 730, height = 60,
        font = Font(size = 35), text = "please choose a new worker"
    )

    private val cashierButton = Button(
        posX = 50, posY = 685, width = 345, height = 120,
        text = "CASHIER", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    ).apply {
        onMouseClicked = {
            hideWorkerElements()
            if(selectWorkerMode) {
                newWorkerType = CASHIER
                rootService.playerActionService.relocateWorker(oldWorkerType, newWorkerType!!)
            }
            else{
                oldWorkerType = CASHIER
                startNewWorkerSelection()
            }
        }
    }
    private val nurseButton = Button(
        posX = 405, posY = 685, width = 345, height = 120,
        text = "NURSE", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    ).apply {
        onMouseClicked = {
            hideWorkerElements()
            if(selectWorkerMode){
                newWorkerType = NURSE
                rootService.playerActionService.relocateWorker(oldWorkerType, newWorkerType!!)
            }
            else{
                oldWorkerType = NURSE
                startNewWorkerSelection()
            }
        }
    }
    private val managerButton = Button(
        posX = 50, posY = 865, width = 345, height = 120,
        text = "MANAGER", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    ).apply {
        onMouseClicked = {
            hideWorkerElements()
            if(selectWorkerMode){
                newWorkerType = MANAGER
                rootService.playerActionService.relocateWorker(oldWorkerType, newWorkerType!!)
            }
            else{
                oldWorkerType = MANAGER
                startNewWorkerSelection()
            }
        }
    }
    private val trainerButton = Button(
        posX = 405, posY = 865, width = 345, height = 120,
        text = "TRAINER", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    ).apply {
        onMouseClicked = {
            hideWorkerElements()
            if(selectWorkerMode){
                newWorkerToken.isVisible = true
                // without removing and adding this component it never shows up again
                removeComponents(newWorkerToken)
                addComponents(newWorkerToken)
            }
            else{
                oldTrainerLabel.isVisible = true
                currentSelectedLabel.isVisible = true
                currentSelectedLabel.text = "currently: none"
                confirmTrainerChoiceButton.isVisible = true
            }
        }
    }
    private val newWorkerToken:  TokenView = TokenView (
        posX = 350, posY = 785, width = 100, height = 100,
        visual = ImageVisual("images/gameSceneImages/mitarbeiter.png")
    ).apply {
        isDraggable = true
        onDragGestureStarted = {visual = ImageVisual("images/gameSceneImages/mitarbeiter.png")}
    }
    private val oldTrainerLabel = Label(
        posX = 35, posY = 600, width = 730, height = 60,
        font = Font(size = 35), text = "please click on the trainer you want to relocate"
    )
    private val currentSelectedLabel = Label(
        posX = 35, posY = 650, width = 360, height = 60,
        font = Font(size = 35), text = "currently: none"
    )
    // confirms the selected tile as the location from which the new worker will be taken
    private val confirmTrainerChoiceButton = Button(
        posX = 405, posY = 865, width = 345, height = 120,
        text = "OK", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    ).apply {
        onMouseClicked = {
            hideWorkerElements()
            oldWorkerType = guiCoordsToServiceCoords(lastClickedTile.first, lastClickedTile.second)
            startNewWorkerSelection()
        }
    }



    private fun hideWorkerElements(){
        cashierButton.isVisible = false
        nurseButton.isVisible = false
        managerButton.isVisible = false
        trainerButton.isVisible = false
        newWorkerToken.isVisible = false
        oldWorkerLabel.isVisible = false
        newWorkerLabel.isVisible = false
        oldTrainerLabel.isVisible = false
        currentSelectedLabel.isVisible = false
        confirmTrainerChoiceButton.isVisible = false
    }
    private fun showWorkerChoiceButtons(asNew: Boolean){
        val player = rootService.aquarettoGame.currentPlayer()
        if((!asNew && player.numCashiers>0) || (asNew && player.numCashiers<2))
            cashierButton.isVisible = true
        if((!asNew && player.hasManager) || (asNew && !player.hasManager))
            managerButton.isVisible = true
        if((!asNew && player.numNurses>0) || (asNew && player.numNurses<2))
            nurseButton.isVisible = true

        trainerButton.isVisible = true
    }
    private fun startOldWorkerSelections(){
        oldWorkerType = null
        newWorkerType = null
        selectWorkerMode = selectOld
        showWorkerChoiceButtons(asNew = false)
        oldWorkerLabel.isVisible = true
    }
    private fun startNewWorkerSelection(oldNotSet: Boolean = false){
        if(oldNotSet){
            oldWorkerType = null
            newWorkerType = null
        }
        selectWorkerMode = selectNew
        showWorkerChoiceButtons(asNew = true)
        newWorkerLabel.isVisible = true
    }

    /*
    private val refButton = Button(
        posX = 200, posY = 1000, width = 240, height = 120,
        text = "ref", font = Font(size = 50, color = Color.white),
        alignment = Alignment.CENTER, visual = ColorVisual(113,113,113)
    ).apply {
        onMouseClicked = {
            rootService.aquarettoGame.currentPlayer().numCashiers = 1
            rootService.aquarettoGame.currentPlayer().numNurses = 1
            rootService.aquarettoGame.currentPlayer().numCoins = 10
            //isVisible = false
            hideTurnOptions()
            showTurnOptions()
            //rootService.gameStateService.endGame()
            //refreshAfterGainedWorker(false)
        }
    }

     */
    // endregion
    override fun refreshAfterRelocateWorker(isReverting: Boolean) {
        // check on nurses, cashiers and manager
        playerStats[3].text = "X ${rootService.aquarettoGame.currentPlayer().numCashiers}"
        playerStats[4].text = "X ${rootService.aquarettoGame.currentPlayer().numNurses}"
        playerStats[5].text = "X ${if(rootService.aquarettoGame.currentPlayer().hasManager){"yes"}else{"no"}}"
        // check trainers
        rootService.aquarettoGame.currentPlayer().park.map.filter { it.value is Worker }.forEach{
            val coords = serviceCoordsToGuiCoords(it.key.first, it.key.second)
            targetLayout[coords.first, coords.second] = Area(
                visual = ImageVisual("images/gameSceneImages/tile_mitarbeiter.png"), width = 63, height = 63
            )
        }
        if(endTurnAfterRelocation) rootService.gameStateService.endTurn()
    }

    override fun refreshAfterExtension(isReverting: Boolean) {
        // update counter
        playerStats[1].text = "X ${(2 - rootService.aquarettoGame.currentPlayer().park.largeExtensions.size)}"
        playerStats[2].text = "X ${(2 - rootService.aquarettoGame.currentPlayer().park.smallExtensions.size)}"
        // update field
        if(isReverting){
            targetLayout.filter { it.component?.visual is ImageVisual }.forEach {
                val serviceCoords = guiCoordsToServiceCoords(it.columnIndex, it.rowIndex)
                // revert those tiles to nothing that are no longer in the map
                if(serviceCoords !in rootService.aquarettoGame.currentPlayer().park.map)
                    it.component!!.visual = ColorVisual(0,0,0,0)
            }
        }
        else{
            rootService.aquarettoGame.currentPlayer().park.map.filter { it.value is Empty }.forEach {
                val guiCoords = serviceCoordsToGuiCoords(it.key)
                if(targetLayout[guiCoords.first, guiCoords.second]!!.visual is ColorVisual)
                    targetLayout[guiCoords.first, guiCoords.second]!!.visual = ImageVisual("images/gameSceneImages/tile.png")
            }
            hideTurnOptions()
            showTurnOptions()
            if(rootService.aquarettoGame.currentPlayer() is HotseatPlayer) rootService.gameStateService.endTurn()
        }
    }

    override fun refreshAfterEndTurn(isReverting: Boolean, isNewRound: Boolean) {
        hideAusbauenOptions()
        hideDepotOptions()
        hideVersetzenOptions()
        hideTurnOptions()
        showTurnOptions()
        hidePlayerSelector()
        currentPlayer = rootService.aquarettoGame.currentPlayer
        currentPlayerName = rootService.aquarettoGame.players[currentPlayer].name
        currentPlayerNameLabel.text = currentPlayerName

        // Update the name labels and depots for all the players
        updatePlayerNameLabelsAndDepots()
        // update the currently shown board
        refreshAfterParkChanged(false)
        // update the player stats
        playerStats[0].text = "X ${rootService.aquarettoGame.currentPlayer().numCoins}"
        playerStats[1].text = "X ${(2 - rootService.aquarettoGame.currentPlayer().park.largeExtensions.size)}"
        playerStats[2].text = "X ${(2 - rootService.aquarettoGame.currentPlayer().park.smallExtensions.size)}"
        playerStats[3].text = "cashiers: X ${rootService.aquarettoGame.currentPlayer().numCashiers}"
        playerStats[4].text = "nurses: X ${rootService.aquarettoGame.currentPlayer().numNurses}"
        playerStats[5].text = "manager: ${if(rootService.aquarettoGame.currentPlayer().hasManager){"yes"}else{"no"}}"

        if(isNewRound) resetWagons()

        if(rootService.aquarettoGame.board.mainStack.size == 1)
            mainStack.isVisible = false
        if(rootService.aquarettoGame.board.mainStack.size == 0){
            isEndOfGame = true
            endStackToken.isDraggable = true
            mainStackToken.isVisible = false
        }

        mainStackCount.text = rootService.aquarettoGame.board.mainStack.size.toString()
        endStackCount.text = rootService.aquarettoGame.board.endOfGameStack.size.toString()

        refreshAfterGainedCard(false)
        refreshAfterDepotChanged(false, false, rootService.aquarettoGame.currentPlayer())
    }
    private fun resetWagons(){
        wagon1Deactive = false
        wagon2Deactive = false
        wagon3Deactive = false
        wagon4Deactive = false
        wagon5Deactive = false
        wagon1.visual = ImageVisual("images/gameSceneImages/transportwagen.png")
        wagon2.visual = ImageVisual("images/gameSceneImages/transportwagen.png")
        wagon3.visual = ImageVisual("images/gameSceneImages/transportwagen.png")
        wagon4.visual = ImageVisual("images/gameSceneImages/transportwagen.png")
        wagon5.visual = ImageVisual("images/gameSceneImages/transportwagen.png")
        refreshAfterWagonsChanged(false)
    }

    override fun refreshAfterParkChanged(isReverting: Boolean) {
        // delete all
        targetLayout.forEach {
            it.component?.visual = ColorVisual(0,0,0,0)
        }
        // set all based on new player
        rootService.aquarettoGame.currentPlayer().park.map.filter { true }.forEach {
            val guiCoord = serviceCoordsToGuiCoords(it.key)
            targetLayout[guiCoord.first, guiCoord.second]!!.visual = when(it.value){
                is Empty -> ImageVisual("images/gameSceneImages/tile.png")
                is Worker -> ImageVisual("images/gameSceneImages/tile_mitarbeiter.png")
                is Baby -> ImageVisual(imageLoader.frontImageForTiere(it.value as Baby))
                is Animal -> ImageVisual(imageLoader.frontImageForTiere(it.value as Animal))
            }
        }
        // set three base tiles
        targetLayout[8,11] = Area(visual = ImageVisual("images/gameSceneImages/tile_dirt.png"), width = 63, height = 63)
        targetLayout[8,12] = Area(visual = ImageVisual("images/gameSceneImages/tile_dirt.png"), width = 63, height = 63)
        targetLayout[9,12] = Area(visual = ImageVisual("images/gameSceneImages/tile_dirt.png"), width = 63, height = 63)
    }

    override fun refreshAfterWagonsChanged(isReverting: Boolean) {
        val wagons = rootService.aquarettoGame.board.transportWagons
        for(i in wagons.indices){
            val number = serviceWagonIndexToGUIWagonNumber(i)
            val element1 = if(wagons[i].size>0) wagons[i][0] else null
            val element2 = if(wagons[i].size>1) wagons[i][1] else null
            val element3 = if(wagons[i].size>2) wagons[i][2] else null
            when(number){
                1 -> {
                    wagon1Area1.visual = imageLoader.imageForNullableWagonable(element1)
                    wagon1Area2.visual = imageLoader.imageForNullableWagonable(element2)
                    wagon1Area3.visual = imageLoader.imageForNullableWagonable(element3)
                }
                2 -> {
                    wagon2Area1.visual = imageLoader.imageForNullableWagonable(element1)
                    wagon2Area2.visual = imageLoader.imageForNullableWagonable(element2)
                    wagon2Area3.visual = imageLoader.imageForNullableWagonable(element3)
                }
                3 -> {
                    wagon3Area1.visual = imageLoader.imageForNullableWagonable(element1)
                    wagon3Area2.visual = imageLoader.imageForNullableWagonable(element2)
                    wagon3Area3.visual = imageLoader.imageForNullableWagonable(element3)
                }
                4 -> {
                    wagon4Area1.visual = imageLoader.imageForNullableWagonable(element1)
                    wagon4Area2.visual = imageLoader.imageForNullableWagonable(element2)
                    wagon4Area3.visual = imageLoader.imageForNullableWagonable(element3)
                }
                5 -> {
                    wagon5Area1.visual = imageLoader.imageForNullableWagonable(element1)
                    wagon5Area2.visual = imageLoader.imageForNullableWagonable(element2)
                    wagon5Area3.visual = imageLoader.imageForNullableWagonable(element3)
                }
                else -> {}
            }
        }
    }

    private fun updatePlayerNameLabelsAndDepots() {
        val players = rootService.aquarettoGame.players
        val currentPlayer = rootService.aquarettoGame.currentPlayer

        val playerParkLabels = when (players.size) {
            2 -> listOf(park2)
            3 -> listOf(park2, park3)
            4 -> listOf(park2, park3, park4)
            5 -> listOf(park2, park3, park4, park5)
            else -> listOf()
        }

        val playerDepots = when (players.size) {
            2 -> listOf(depot2)
            3 -> listOf(depot2, depot3)
            4 -> listOf(depot2, depot3, depot4)
            5 -> listOf(depot2, depot3, depot4, depot5)
            else -> listOf()
        }

        val playerSelectorLabels = when (players.size) {
            2 -> listOf(playerSelector[0])
            3 -> listOf(playerSelector[0], playerSelector[1])
            4 -> listOf(playerSelector[0], playerSelector[1], playerSelector[2])
            5 -> listOf(playerSelector[0], playerSelector[1], playerSelector[2], playerSelector[3])
            else -> listOf()
        }

        for (i in playerParkLabels.indices) {
            val playerIndex = (currentPlayer + i + 1) % players.size

            playerParkLabels[i].text = players[playerIndex].name
            playerSelectorLabels[i].text = players[playerIndex].name


            val topDepotItem = players[playerIndex].depot.lastOrNull()
            val depotVisual = when (topDepotItem) {
                is Animal -> ImageVisual(imageLoader.frontImageForTiere(topDepotItem))
                null -> ColorVisual(Color.WHITE)
                else -> ImageVisual(imageLoader.frontImageBackImage())
            }
            playerDepots[i].visual = depotVisual
        }
    }

    override fun refreshAfterGainedCard(isReverting: Boolean) {
        cardsToPlay.forEach { removeComponents(it) }
        cardsToPlay.clear()
        rootService.aquarettoGame.currentPlayer().cardsToPlay.forEach {
            val token = AnimalTokenView(
                posX = 1600 + cardsToPlay.size*80, posY = 797, width = 63, height = 63,
                visual = ImageVisual(imageLoader.frontImageForTiere(it)),
                type = it
            ).apply { isDraggable = true }
            cardsToPlay.add(token)
            addComponents(token)
        }
    }
    private val cardsToPlay = mutableListOf<TokenView>()


    override fun refreshAfterChangedBalance(isReverting: Boolean, change: Int, player: Player) {
        if(player == rootService.aquarettoGame.currentPlayer())
            playerStats[0].text = "X ${rootService.aquarettoGame.currentPlayer().numCoins}"
    }


    override fun refreshBeforeEndTurn() {
        playAnimation(DelayAnimation(500).apply {
            lock()
            onFinished = {
                rootService.gameStateService.updateEndTurn()
                unlock()
            }
        })
    }

    override fun refreshAfterTakeWagon(isReverting: Boolean, takenWagonIndex: Int) {
        val wagonNumber = serviceWagonIndexToGUIWagonNumber(takenWagonIndex)
        if(!isReverting){
            when(wagonNumber){
                1 -> {
                    wagon1.visual = ImageVisual("images/gameSceneImages/transportwagen_taken.png")
                    wagon1Deactive = true
                }
                2 -> {
                    wagon2.visual = ImageVisual("images/gameSceneImages/transportwagen_taken.png")
                    wagon2Deactive = true
                }
                3 -> {
                    wagon3.visual = ImageVisual("images/gameSceneImages/transportwagen_taken.png")
                    wagon3Deactive = true
                }
                4 -> {
                    wagon4.visual = ImageVisual("images/gameSceneImages/transportwagen_taken.png")
                    wagon4Deactive = true
                }
                5 -> {
                    wagon5.visual = ImageVisual("images/gameSceneImages/transportwagen_taken.png")
                    wagon5Deactive = true
                }
            }
        }
    }


    init {
        background = ColorVisual(152, 219, 217)
        opacity = 1.0

        setVisuals()
        addComponents(
            label, depotButton, versetzenButton, ausbauenButton, undoButton, redoButton,
            //helpButton,
            settingsButton, line1, line2, cardLabel,
            mainStack, endStack, mainStackToken, endStackToken,
            window, cameraPane,
            depot,
            currentPlayerNameLabel,
            cashierButton, nurseButton, managerButton, trainerButton, newWorkerToken, // refButton,
            oldWorkerLabel, newWorkerLabel, oldTrainerLabel, currentSelectedLabel, confirmTrainerChoiceButton,
            oopsButton, mainStackCount, endStackCount
        )
        playerStats.forEach { addComponents(it) }
        playerStatsImgs.forEach { addComponents(it) }

        setDropAcceptors()
        initializeCamera()
        hideWorkerElements()
    }
}
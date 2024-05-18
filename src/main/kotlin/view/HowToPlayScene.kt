package view

import service.RootService
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.ColorVisual

class HowToPlayScene(private val rootService: RootService): MenuScene(1920, 1080) {

    init{
        background = ColorVisual(152, 219, 217)
        opacity = 1.0
    }
}
package view

import entity.Depotable
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.visual.Visual

class AnimalTokenView(
    posX: Int, posY: Int, width: Int, height: Int,
    visual: Visual,
    var type: Depotable
): TokenView(posX, posY, width, height, visual)
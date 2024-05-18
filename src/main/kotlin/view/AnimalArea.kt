package view

import entity.Depotable
import tools.aqua.bgw.components.container.Area
import tools.aqua.bgw.visual.Visual

class AnimalArea(
    posX: Int, posY: Int, width: Int, height: Int,
    visual: Visual,
    var type: Depotable?
): Area<AnimalTokenView>(posX, posY, width, height, visual)
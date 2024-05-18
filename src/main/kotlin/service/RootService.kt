package service

import entity.Aquaretto
import service.network.NetworkService
import view.Refreshable

class RootService {
    lateinit var aquarettoGame: Aquaretto

    val playerActionService = PlayerActionService(this)
    val gameStateService = GameStateService(this)
    val aiHelperService = AIHelperService()
    val networkService = NetworkService(rootService = this)

    /* Uncomment, when GUI is sufficiently tested */
    val smartBot = SmartBotService(this)
    val dumbBot = DumbBotService(this)

    fun addBots(){
        addRefreshables(smartBot, dumbBot)
    }

    /**
     * Adds the provided [newRefreshable] to all services connected
     * to this root service
     */
    fun addRefreshable(newRefreshable: Refreshable) {
        playerActionService.addRefreshable(newRefreshable)
        gameStateService.addRefreshable(newRefreshable)
        aiHelperService.addRefreshable(newRefreshable)
        smartBot.addRefreshable(newRefreshable)
        networkService.addRefreshable(newRefreshable)
    }

    /**
     * Adds each of the provided [newRefreshable] to all services connected
     * to this root service
     */
    fun addRefreshables(vararg newRefreshable: Refreshable) {
        newRefreshable.forEach { addRefreshable(it) }
    }
}

package view

import entity.Player
import service.*
import service.network.ConnectionState

/**
 * This interface provides a mechanism for the service layer classes to communicate
 * (usually to the view classes) that certain changes have been made to the entity
 * layer, so that the user interface can be updated accordingly.
 * Default (empty) implementations are provided for all methods, so that implementing
 * UI classes only need to react to events relevant to them.
 * @see AbstractRefreshingService
 */

interface Refreshable {




    /**
     * should be called upon initial game setup, initialized everything
     */
    fun refreshAfterInitializeGame(){  }

    /**
     * should be called after [PlayerActionService.putCardOnWagon]
     *
     * changes in main stack and wagons
     */
    fun refreshAfterWagonsChanged(isReverting: Boolean){  }

    /**
     * should be called after [PlayerActionService.animalToPark]
     *
     * update in waterpark and cardsToPlay, might call balance, baby, worker refreshes
     */
    fun refreshAfterParkChanged(isReverting: Boolean){  }

    /**
     * should be called after [PlayerActionService.animalToDepot]
     *
     * update in depot and cardsToPlay
     *
     * @param cardToDepot is true iff a card is added to the depot, when removed it is false
     */
    fun refreshAfterDepotChanged(cardToDepot: Boolean, isReverting: Boolean, updated: Player){  }

    /**
     * should be called after [PlayerActionService.buyCard] and
     * [PlayerActionService.sellCard]
     *
     * update in numCoins
     */
    fun refreshAfterChangedBalance(isReverting: Boolean, change: Int, player: Player){  }

    /**
     * should be called after [PlayerActionService.animalToDepot]
     *
     * no data changed, needs to place new worker
     */
    fun refreshAfterGainedWorker(isReverting: Boolean, ){  }

    /**
     * should be called after [PlayerActionService.animalToPark]
     *
     * change in cardsToPlay (needs to be played immediately)
     */
    fun refreshAfterGainedCard(isReverting: Boolean){  }

    /**
     * should be called after [PlayerActionService.depotToPark]
     *
     * update in depot and waterpark
     *
     * could be nice to replace this with separate calls to depot and park refreshes
     */
    @Deprecated("replaced by separate calls to ParkChanged and DepotChanged")
    fun refreshAfterDepotToPark(isReverting: Boolean){  }

    /**
     * should be called after [PlayerActionService.relocateWorker]
     *
     * updates in wherever workers are drawn
     */
    fun refreshAfterRelocateWorker(isReverting: Boolean){  }

    /**
     * should be called after [PlayerActionService.buyCard]
     *
     * update in other players depot, this players cardsToPlay
     */
    @Deprecated("replaced by DepotChanged and GainedCard")
    fun refreshAfterBuyCard(isReverting: Boolean){  }

    /**
     * should be called after [PlayerActionService.sellCard]
     *
     * update in depot
     */
    @Deprecated("replaced by DepotChanged")
    fun refreshAfterSellCard(isReverting: Boolean){  }

    /**
     * should be called after [PlayerActionService.addSmallExtension] and
     * [PlayerActionService.addLargeExtension]
     *
     * update in waterpark
     */
    fun refreshAfterExtension(isReverting: Boolean){  }

    /**
     * should be called after [GameStateService.endTurn]
     */
    fun refreshAfterEndTurn(isReverting: Boolean, isNewRound: Boolean){  }

    /**
     * should be called after [GameStateService.endGame]
     */
    fun refreshAfterEndGame(results: MutableList<Triple<Player, Int, Int>>){  }

    /**
     * should be called after [PlayerActionService.takeWagon]
     */
    fun refreshAfterTakeWagon(isReverting: Boolean, takenWagonIndex: Int){  }


    fun refreshConnectionState(connectionState: ConnectionState){}

    fun refreshBeforeEndTurn(){  }

    fun refreshAfterPlayerJoined(playerList: List<String>){}

    fun refreshAfterSessionCreated(sessionId: String , playerName: String){}

    fun refreshAfterSelfJoined(hostNam: String, guests: List<String>){}
}
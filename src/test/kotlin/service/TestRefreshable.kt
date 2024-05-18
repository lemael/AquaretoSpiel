package service

import entity.Player
import entity.Wagonable
import view.Refreshable

/**
 * Test refreshable to check if refreshables are called correctly
 */
class TestRefreshable : Refreshable {
    var refreshAfterInitializeGameCalled: Boolean = false
        private set
    var refreshAfterWagonsChangedCalled: Boolean = false
        private set
    var refreshAfterParkChangedCalled: Boolean = false
        private set
    var refreshAfterDepotChangedCalled: Boolean = false
        private set
    var refreshAfterChangedBalanceCalled: Boolean = false
        private set
    var refreshAfterGainedWorkerCalled: Boolean = false
        private set
    var refreshAfterGainedCardCalled: Boolean = false
        private set
    var refreshAfterRelocateWorkerCalled: Boolean = false
        private set
    var refreshAfterExtensionCalled: Boolean = false
        private set
    var refreshAfterEndTurnCalled: Boolean = false
        private set
    var refreshAfterEndGameCalled: Boolean = false
        private set
    var refreshAfterTakeWagonCalled: Boolean = false
        private set


    override fun refreshAfterInitializeGame() {
        refreshAfterInitializeGameCalled = true
    }

    override fun refreshAfterWagonsChanged(isReverting: Boolean) {
        refreshAfterWagonsChangedCalled = true
    }

    override fun refreshAfterParkChanged(isReverting: Boolean) {
        refreshAfterParkChangedCalled = true
    }

    override fun refreshAfterDepotChanged(cardToDepot: Boolean, isReverting: Boolean, updated: Player) {
        refreshAfterDepotChangedCalled = true
    }

    override fun refreshAfterChangedBalance(isReverting: Boolean, change: Int, player: Player) {
        refreshAfterChangedBalanceCalled = true
    }

    override fun refreshAfterGainedWorker(isReverting: Boolean) {
        refreshAfterGainedWorkerCalled = true
    }

    override fun refreshAfterGainedCard(isReverting: Boolean) {
        refreshAfterGainedCardCalled = true
    }

    override fun refreshAfterRelocateWorker(isReverting: Boolean) {
        refreshAfterRelocateWorkerCalled = true
    }

    override fun refreshAfterExtension(isReverting: Boolean) {
        refreshAfterExtensionCalled = true
    }

    override fun refreshAfterEndTurn(isReverting: Boolean, isNewRound: Boolean) {
        refreshAfterEndTurnCalled = true
    }

    override fun refreshAfterEndGame(results: MutableList<Triple<Player, Int, Int>>) {
        refreshAfterEndGameCalled = true
        this.results = results
    }

    lateinit var results: MutableList<Triple<Player, Int, Int>>

    override fun refreshAfterTakeWagon(isReverting: Boolean, takenWagonIndex: Int) {
        refreshAfterTakeWagonCalled = true
    }

    fun reset() {
        refreshAfterInitializeGameCalled = false
        refreshAfterWagonsChangedCalled = false
        refreshAfterParkChangedCalled = false
        refreshAfterDepotChangedCalled = false
        refreshAfterChangedBalanceCalled = false
        refreshAfterGainedWorkerCalled = false
        refreshAfterGainedCardCalled = false
        refreshAfterRelocateWorkerCalled = false
        refreshAfterExtensionCalled = false
        refreshAfterEndTurnCalled = false
        refreshAfterEndGameCalled = false
        refreshAfterTakeWagonCalled = false
    }
}
package entity.commands

import com.fasterxml.jackson.annotation.*
import entity.*
import service.PlayerActionService

/**
 * moves a worker from one position to a new one
 *
 * for usage see [PlayerActionService.relocateWorker]
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property = "id"
)
class RelocateWorkerCommand @JsonCreator constructor(
    id: Int,
    private val old: Pair<Int, Int>?,
    private val new: Pair<Int, Int>,
    private val player: Player
): Command(id){

    override fun apply() {
        if(old!=null) player.numCoins--
        onAllRefreshables { refreshAfterChangedBalance(false, -1, player) }
        when(old){
            null -> {}
            CASHIER -> player.numCashiers--
            NURSE -> player.numNurses--
            MANAGER -> player.hasManager = false
            else -> player.park.map[old] = Empty()
        }
        when(new){
            CASHIER -> player.numCashiers++
            NURSE -> player.numNurses++
            MANAGER -> player.hasManager = true
            else -> player.park.map[new] = Worker()
        }
        onAllRefreshables { refreshAfterRelocateWorker(false) }
    }

    override fun revert() {
        if(old!=null) player.numCoins++
        onAllRefreshables { refreshAfterChangedBalance(true, 1, player) }
        when(old){
            null -> {}
            CASHIER -> player.numCashiers++
            NURSE -> player.numNurses++
            MANAGER -> player.hasManager = true
            else -> player.park.map[old] = Worker()
        }
        when(new){
            CASHIER -> player.numCashiers--
            NURSE -> player.numNurses--
            MANAGER -> player.hasManager = false
            else -> player.park.map[new] = Empty()
        }
        onAllRefreshables { refreshAfterRelocateWorker(true) }
    }

    override fun toString(): String {
        return "player ref: $player"
    }

}

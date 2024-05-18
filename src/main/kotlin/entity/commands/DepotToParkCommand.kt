package entity.commands

import com.fasterxml.jackson.annotation.*
import entity.Command
import entity.Player
import service.*

/**
 * removes the top card from the [Player.depot] (last index) and moves it to [Player.cardsToPlay]
 *
 * reduces [Player.numCoins] by one
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property = "id"
)
class DepotToParkCommand @JsonCreator constructor(
    id: Int,
    private val player: Player
): Command(id){

    private val card = player.depot.last()

    override fun apply() {
        player.numCoins--
        onAllRefreshables { refreshAfterChangedBalance(false, -1, player) }
        player.depot.removeLast()
        onAllRefreshables { refreshAfterDepotChanged(false,false, player) }
        player.cardsToPlay.add(card)
        onAllRefreshables { refreshAfterGainedCard(false) }
    }

    override fun revert() {
        player.numCoins++
        onAllRefreshables { refreshAfterChangedBalance(true, 1, player) }
        player.depot.add(card)
        onAllRefreshables { refreshAfterDepotChanged(true,true, player) }
        player.cardsToPlay.remove(card)
        onAllRefreshables { refreshAfterGainedCard(true) }
    }


    override fun toString(): String {
        return "player ref: $player"
    }

}

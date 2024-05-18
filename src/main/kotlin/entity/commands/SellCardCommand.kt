package entity.commands

import com.fasterxml.jackson.annotation.*
import entity.Command
import entity.Depotable
import entity.Player

/**
 * This class stores all the data needed to apply and revert a sellCard()-Invocation.
 *
 * @property player is the current-player, who is calling this function
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property = "id"
)
class SellCardCommand @JsonCreator constructor(
    id: Int,
    private var player: Player
): Command(id) {
    // This var stores a reference to the card, which is being sold
    private lateinit var soldCard: Depotable

    override fun apply() {
        soldCard = player.depot.removeLast()

        player.numCoins -= 2

        onAllRefreshables { refreshAfterChangedBalance(false, -2, player) }
        onAllRefreshables { refreshAfterDepotChanged(false,false, player) }
    }

    override fun revert() {
        check(this::soldCard.isInitialized)
            {"Can't redo this command as it has never been executed."}

        player.depot.add(soldCard)

        player.numCoins += 2

        onAllRefreshables { refreshAfterChangedBalance(true, 2, player) }
        onAllRefreshables { refreshAfterDepotChanged(true,true, player) }
    }


    override fun toString(): String {
        return "player ref: $player"
    }
}
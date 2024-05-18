package entity.commands

import com.fasterxml.jackson.annotation.*
import entity.Command
import entity.Depotable
import entity.Player

/**
 * This class stores all the data needed to apply and revert a buyCard()-Invocation.
 *
 * @property currentPlayer is the current-player, who is calling this function
 * @property buyFromPlayer is the player, who the card is bought from
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property = "id"
)
class BuyCardCommand @JsonCreator constructor(
    id: Int,
    private var currentPlayer: Player,
    private var buyFromPlayer: Player
): Command(id) {
    // This var stores a reference to the card, which is being bought
    private lateinit var boughtCard: Depotable

    override fun apply() {
        boughtCard = buyFromPlayer.depot.removeLast()

        currentPlayer.numCoins -= 2
        buyFromPlayer.numCoins += 1

        currentPlayer.cardsToPlay.add(boughtCard)

        onAllRefreshables { refreshAfterChangedBalance(false, -2, currentPlayer) }
        onAllRefreshables { refreshAfterChangedBalance(false, 1, buyFromPlayer) }
        onAllRefreshables { refreshAfterDepotChanged(false,false, buyFromPlayer) }
        onAllRefreshables { refreshAfterGainedCard(false) }
    }

    override fun revert() {
        check(this::boughtCard.isInitialized)
            {"Can't redo this command as it has never been executed."}

        buyFromPlayer.depot.add(boughtCard)

        currentPlayer.numCoins += 2
        buyFromPlayer.numCoins -= 1

        currentPlayer.cardsToPlay.remove(boughtCard)

        onAllRefreshables { refreshAfterChangedBalance(true, 2, currentPlayer) }
        onAllRefreshables { refreshAfterChangedBalance(true, -1, buyFromPlayer) }
        onAllRefreshables { refreshAfterDepotChanged(false,true, buyFromPlayer) }
        onAllRefreshables { refreshAfterGainedCard(true) }
    }

    override fun toString(): String {
        return "current player ref: $currentPlayer, other player ref: $buyFromPlayer"
    }
}

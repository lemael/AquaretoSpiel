package entity.commands

import com.fasterxml.jackson.annotation.*
import entity.Command
import entity.Empty
import entity.Player

/**
 * This class stores all the data needed to apply and revert a addSmallExtension()-Invocation.
 *
 * @property newTiles are the new tiles, which are supposed to be added
 * @property player is the player, whose map is changed
 * @property data hold the data, how the extension should be placed
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property = "id"
)
class AddSmallExtensionCommand @JsonCreator constructor(
    id: Int,
    private val newTiles: List<Pair<Int, Int>>,
    private val player: Player,
    private val data: Triple<Int, Int, Int>
) : Command(id) {
    override fun apply() {
        player.park.smallExtensions.add(data)

        player.numCoins -= 1
        onAllRefreshables { refreshAfterChangedBalance(false, -1, player) }

        newTiles.forEach { player.park.map.addEntry(it, Empty()) }
        onAllRefreshables { refreshAfterExtension(false) }
    }

    override fun revert() {
        player.park.smallExtensions.remove(data)

        player.numCoins += 1
        onAllRefreshables { refreshAfterChangedBalance(true, 1, player) }

        newTiles.forEach { player.park.map.removeEntry(it) }
        onAllRefreshables { refreshAfterExtension(true) }
    }

    override fun toString(): String {
        return "player ref: $player"
    }
}
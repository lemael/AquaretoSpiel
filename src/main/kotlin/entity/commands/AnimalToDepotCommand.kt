package entity.commands

import com.fasterxml.jackson.annotation.*
import entity.Command
import entity.Depotable
import entity.Player

/**
 * puts the given animal to the top of the depot stack, that is the last index
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property = "id"
)
class AnimalToDepotCommand @JsonCreator constructor(
    id: Int,
    private val animal: Depotable,
    private val player: Player
): Command(id){

    override fun apply() {
        player.depot.add(animal)
        player.cardsToPlay.remove(animal)
        var cardToDepot = true
        onAllRefreshables { refreshAfterDepotChanged(cardToDepot,false, player) }
    }

    override fun revert() {
        player.depot.removeLast()
        player.cardsToPlay.add(animal)
        var cardToDepot = false
        onAllRefreshables { refreshAfterDepotChanged(cardToDepot,true, player) }
    }

    override fun toString(): String {
        return "player ref: $player"
    }
}

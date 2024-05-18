package entity.commands

import com.fasterxml.jackson.annotation.*
import entity.*
import service.RootService

/**
 * This class stores all the data needed to apply and revert a endTurn()-Invocation.
 *
 * actions performed by endTurn:
 * 1) move [Aquaretto.currentPlayer] to the next player that has not yet taken a wagon
 * 2) resets wagons if everyone has taken one
 *
 * @property Aquaretto reference to the currently running game
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property = "id"
)
class EndTurnCommand @JsonCreator constructor(
    id: Int,
    private val game: Aquaretto,
    @JsonIgnore
    private val root: RootService? = null
) : Command(id) {
    private val oldCurr = game.currentPlayer
    private var reset = false
    private lateinit var oldWagons: MutableList<MutableList<Wagonable>>
    override fun apply() {
        // increment current player to first player that has to take a wagon
        do game.currentPlayer = (game.currentPlayer+1)%game.players.size
        while (game.currentPlayer().tookWagon && game.currentPlayer!=oldCurr)

        // curr == currentPlayer indicates that no other player has to play
        if(oldCurr == game.currentPlayer && game.currentPlayer().tookWagon){
            reset = true
            game.players.forEach { it.tookWagon = false }
            // store previous state of wagons
            oldWagons = game.board.transportWagons
            game.board.transportWagons = mutableListOf<MutableList<Wagonable>>().apply{
                if(game.players.size == 2){
                    this.add(mutableListOf(Empty()))
                    this.add(mutableListOf(Empty(), Empty()))
                    this.add(mutableListOf(Empty(), Empty(), Empty()))
                }
                else repeat(game.players.size){ this.add(mutableListOf(Empty(), Empty(), Empty())) }
            }
            if(root != null){
                root.networkService.wagonsSnapshot = game.board.transportWagons.toList()
            }
        }
        onAllRefreshables {
            refreshAfterEndTurn(false, reset)
        }
    }

    override fun revert() {
        game.currentPlayer = oldCurr
        if(reset){
            game.players.forEach { it.tookWagon = true }
            game.board.transportWagons = oldWagons
        }

        onAllRefreshables {
            refreshAfterEndTurn(true, reset)
        }
    }
}

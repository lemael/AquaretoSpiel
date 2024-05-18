package entity

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.annotation.ObjectIdGenerators

/**
 * stores references to all entities, mainly [Player], [Board] and [Command]
 */
@JsonIdentityInfo( // for keeping references correct
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property = "gameID",
    scope = Aquaretto::class
)
class Aquaretto(
    //@JsonManagedReference
    val players: List<Player>,
    @JsonManagedReference
    val board: Board
){
    val gameID: Int = 0
    val undos = mutableListOf<Command>()
    val redos = mutableListOf<Command>()
    var currentPlayer = 0

    /**
     * returns the currently active [Player] entity pointed to by [currentPlayer]
     */
    fun currentPlayer() = players[currentPlayer]



    override fun toString(): String {
        return "players: $players, \nboard: $board \nundos: $undos"
    }
}

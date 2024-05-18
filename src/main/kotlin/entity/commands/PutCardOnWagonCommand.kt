package entity.commands

import com.fasterxml.jackson.annotation.*
import entity.Board
import entity.Command
import entity.Empty

/**
 * This class stores all the data needed to apply and revert a putCardOnWagon()-Invocation.
 *
 * @property board is a reference to the current board
 * @param wagonIndex is the index of the chosen wagon
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property = "id"
)
class PutCardOnWagonCommand @JsonCreator constructor(
    id: Int,
    private val board: Board,
    wagonIndex: Int
): Command(id) {
    private val wagon = board.transportWagons[wagonIndex]
    private val fromMainStack = board.mainStack.isNotEmpty()

    override fun apply() {
        val card = if (fromMainStack) board.mainStack.removeLast() else board.endOfGameStack.removeLast()

        val posOnWagon = wagon.indexOfFirst { it is Empty }
        wagon[posOnWagon] = card

        onAllRefreshables { refreshAfterWagonsChanged(false) }
    }

    override fun revert() {
        val posOnWagon = wagon.indexOfLast { it !is Empty }

        val card = wagon[posOnWagon]
        wagon[posOnWagon] = Empty()

        if (fromMainStack) {
            board.mainStack.add(card)
        } else {
            board.endOfGameStack.add(card)
        }

        onAllRefreshables { refreshAfterWagonsChanged(true) }
    }

    override fun toString(): String {
        return "board ref: $board"
    }
}
package entity.commands

import com.fasterxml.jackson.annotation.*
import entity.*

/**
 * This class stores all the data needed to apply and revert a takeWagon()-Invocation.
 *
 * @param wagonIndex is the index of the chosen wagon
 * @param wagons is the list of lists, storing the wagon-state
 * @property currentPlayer is the index of the current player
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property = "id"
)
class TakeWagonCommand(
    id: Int,
    private val wagonIndex: Int,
    private val board: Board,
    private val currentPlayer: Player
) : Command(id) {
    private lateinit var wagon: MutableList<Wagonable>
    private var gainedCoins = 0
    private val newAnimals = mutableListOf<Animal>()

    override fun apply() {
        currentPlayer.tookWagon = true
        wagon = board.transportWagons.removeAt(wagonIndex)
        onAllRefreshables { refreshAfterTakeWagon(false, wagonIndex) }

        wagon.forEach {
            if (it is CoinCard) {
                gainedCoins += 1
            } else if (it is Animal) {
                newAnimals.add(it)
            }
        }

        currentPlayer.numCoins += gainedCoins
        onAllRefreshables { refreshAfterChangedBalance(false, gainedCoins, currentPlayer) }

        currentPlayer.cardsToPlay.addAll(newAnimals)
        onAllRefreshables { refreshAfterGainedCard(false) }
    }

    override fun revert() {
        check(this::wagon.isInitialized)
            { "Can't redo this command as it has never been executed." }
        currentPlayer.tookWagon = false

        currentPlayer.numCoins -= gainedCoins
        onAllRefreshables { refreshAfterChangedBalance(true, -gainedCoins, currentPlayer) }

        currentPlayer.cardsToPlay.removeAll(newAnimals)
        onAllRefreshables { refreshAfterGainedCard(true) }

        board.transportWagons.add(wagonIndex, wagon)
        onAllRefreshables { refreshAfterTakeWagon(true, wagonIndex) }
    }


    override fun toString(): String {
        return "\ntake: player ref: ${currentPlayer}layer, coins gained: $gainedCoins, new animals: $newAnimals"
    }
}

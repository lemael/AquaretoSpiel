package entity

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators

/**
 * @property boardID identifier for board serialization
 * @property transportWagons a list of transport wagons, being a list of [Wagonable]s.
 * @property mainStack the main draw stack used for placing cards on the [transportWagons]
 * @property endOfGameStack touching this deck indicates the end of the game
 */
@JsonIdentityInfo( // for keeping references correct
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property = "boardID",
    scope = Board::class
)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY) // make name serializable while being private
class Board(val boardID: Int) {

    var transportWagons = mutableListOf<MutableList<Wagonable>>()
    var mainStack = mutableListOf<Wagonable>()
    var endOfGameStack = mutableListOf<Wagonable>()
}

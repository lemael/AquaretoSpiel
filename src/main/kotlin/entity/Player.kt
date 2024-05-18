package entity

import com.fasterxml.jackson.annotation.*
import entity.players.DumbBot
import entity.players.HotseatPlayer
import entity.players.OnlineBGWPlayer
import entity.players.SmartBot

/**
 * stores all things belonging directly to one player
 *
 * holds a [Waterpark]
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes( // for subclasses
    JsonSubTypes.Type(value = DumbBot::class, name = "dumbbot"),
    JsonSubTypes.Type(value = SmartBot::class, name = "smartbot"),
    JsonSubTypes.Type(value = OnlineBGWPlayer::class, name = "onlinebgwplayer"),
    JsonSubTypes.Type(value = HotseatPlayer::class, name = "hotseatplayer")
)
@JsonIdentityInfo( // for keeping references correct
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property = "name"
)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY) // make name serializable while being private
abstract class Player(open val name: String){
    var numCashiers: Int = 0
    var numNurses: Int = 0
    var hasManager = false
    var numCoins: Int = 1
    var tookWagon = false
    val depot = mutableListOf<Depotable>()

    val cardsToPlay = mutableListOf<Depotable>()

    val park = Waterpark()


    //override fun toString(): String {
      //  return "\n(player name: $name with cashiers: $numCashiers, nurses: $numNurses, manager: $hasManager, coins: $numCoins, \ndepot: $depot, \ncards: $cardsToPlay \npark: $park)"
    //}
}

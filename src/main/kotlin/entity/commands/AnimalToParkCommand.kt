package entity.commands

import com.fasterxml.jackson.annotation.*
import entity.*
import service.RootService
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities.Private

/**
 * puts the given animal on the given location of the waterpark
 *
 * adds the given [pool] as a new one when [newPool] is set
 *
 * can perform following modifications when conditions are applicable
 * 1) change gender count
 * 2) add coin and call refreshAfterChangedBalance
 * 3) call refreshAfterGainedWorker
 * 4) add Baby card to [Player.cardsToPlay] and call refreshAfterGainedCard
 *
 * revert assumes that all applicable actions have already been performed
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property = "id"
)
class AnimalToParkCommand @JsonCreator constructor(
    id: Int,
    private val animal: Depotable,
    private val position: Pair<Int, Int>,
    private val player: Player,
    private val pool: Pool,
    private val newPool: Boolean,
    @JsonIgnore
    val rootService: RootService ? = null


): Command(id){

    private val previousContent = player.park.map[position]
    private lateinit var baby: Baby
    private var gainedCoin = false

    override fun apply() {
        // add new Pool if required
        if(newPool) player.park.pools.add(pool)
        // put animal into the park
        player.park.map[position] = animal
        pool.members.add(position)
        onAllRefreshables { refreshAfterParkChanged(false) }
        player.cardsToPlay.remove(animal)
        onAllRefreshables { refreshAfterGainedCard(false) }

        // check for male/female
        if(animal is Animal){
            if(animal.gender == AnimalGender.MALE) pool.numMale++
            if(animal.gender == AnimalGender.FEMALE) pool.numFemale++
        }
        // check pool sizes
        if(gainedCoin || pool.members.size % 3 == 0){
            gainedCoin = true
            player.numCoins++
            onAllRefreshables { refreshAfterChangedBalance(false, 1, player) }
        }
        if(pool.members.size % 5 == 0)
            onAllRefreshables { refreshAfterGainedWorker(false) }
        // check for baby creation
        if(pool.numMale > 0 && pool.numFemale > 0){
            pool.numMale--
            pool.numFemale--
            baby = Baby(pool.type)
            player.cardsToPlay.add(baby)
            onAllRefreshables { refreshAfterGainedCard(false) }



        }
    }

    override fun revert() {
        // remove pool from park if required
        if(newPool) player.park.pools.remove(pool)
        // remove animal from the park
        player.park.map[position] = previousContent
        pool.members.remove(position)
        onAllRefreshables { refreshAfterParkChanged(true) }
        // put played card back on the players hand
        player.cardsToPlay.add(animal)
        onAllRefreshables { refreshAfterGainedCard(true) }
        // check for male/female
        if(animal is Animal){
            if(animal.gender == AnimalGender.MALE) pool.numMale--
            if(animal.gender == AnimalGender.FEMALE) pool.numFemale--
        }
        // take coin if one was given
        if(gainedCoin){
            player.numCoins--
            onAllRefreshables { refreshAfterChangedBalance(true, -1, player) }
        }
        // check for baby creation
        if(this::baby.isInitialized){
            pool.numMale++
            pool.numFemale++
            player.cardsToPlay.remove(baby)
            onAllRefreshables { refreshAfterGainedCard(true) }
        }
    }


    override fun toString(): String {
        return "player ref: $player"
    }

}

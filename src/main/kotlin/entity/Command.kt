package entity

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import entity.commands.*
import service.AbstractRefreshingService

/**
 * gives the structural framework used for re- and undoing player actions
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = AddLargeExtensionCommand::class, name = "large"),
    JsonSubTypes.Type(value = AddSmallExtensionCommand::class, name = "small"),
    JsonSubTypes.Type(value = AnimalToDepotCommand::class, name = "animalToDepot"),
    JsonSubTypes.Type(value = AnimalToParkCommand::class, name = "animalToPark"),
    JsonSubTypes.Type(value = BuyCardCommand::class, name = "buyCard"),
    JsonSubTypes.Type(value = DepotToParkCommand::class, name = "depotToPark"),
    JsonSubTypes.Type(value = EndTurnCommand::class, name = "endTurn"),
    JsonSubTypes.Type(value = PutCardOnWagonCommand::class, name = "onWagon"),
    JsonSubTypes.Type(value = RelocateWorkerCommand::class, name = "reloWorker"),
    JsonSubTypes.Type(value = SellCardCommand::class, name = "sellCard"),
    JsonSubTypes.Type(value = TakeWagonCommand::class, name = "takeWagon"),
)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY) // make map serializable while being private
abstract class Command(val id: Int): AbstractRefreshingService(){
    /**
     * use the action described by the respective subclass
     *
     * does not check any requirements
     */
    abstract fun apply()

    /**
     * restore the game state present before calling apply()
     *
     * assumes that the state before calling this is the one achieved by calling apply()
     */
    abstract fun revert()
}

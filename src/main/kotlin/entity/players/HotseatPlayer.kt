package entity.players

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import entity.Player

/**
 * represents a player that plays the game via inputs on this computer
 */
class HotseatPlayer @JsonCreator constructor(name: String): Player(name)
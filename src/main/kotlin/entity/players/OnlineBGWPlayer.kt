package entity.players

import com.fasterxml.jackson.annotation.JsonCreator
import entity.Player

/**
 * represents a player that sends their moves via the network
 */
class OnlineBGWPlayer @JsonCreator constructor(name: String): Player(name)
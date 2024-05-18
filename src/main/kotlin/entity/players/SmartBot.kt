package entity.players

import com.fasterxml.jackson.annotation.JsonCreator
import entity.Player
import service.SmartBotService

/**
 * represents a player that chooses their moves based as described by [SmartBotService]
 */
class SmartBot @JsonCreator constructor(name: String): Player(name)
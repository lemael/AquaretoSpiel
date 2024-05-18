package entity.players

import com.fasterxml.jackson.annotation.JsonCreator
import entity.Player
import service.DumbBotService

/**
 * represents a player that chooses their moves based as described by [DumbBotService]
 */
class DumbBot @JsonCreator constructor(name: String): Player(name)
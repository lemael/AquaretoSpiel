package entity

/**
 * Used for having entries in transport wagons or the [ParkMap]
 * while no actual game-related thing exists (avoids nullable)
 *
 * implements [Parkable] and [Wagonable] to enable the described feature
 */
class Empty(override var ID: Int = -1): Parkable, Wagonable

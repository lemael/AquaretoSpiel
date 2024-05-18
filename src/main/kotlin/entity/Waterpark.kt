package entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * stores all information needed to maintain correct waterpark constraints
 *
 * @property map [ParkMap] can map any position to its [Parkable]
 * @property pools [Pool] to easily find pools instead of calculation them from scratch each time
 */
@JsonIgnoreProperties(ignoreUnknown = true) // ignore field maxPools
class Waterpark{
    val maxPools
        get() = 3+largeExtensions.size

    /**
     * stores the position (x = first, y = second) of small extensions
     *
     * third represents the orientation, see [TOP_LEFT], [TOP_RIGHT], [BOTTOM_LEFT] and [BOTTOM_RIGHT]
     */
    val smallExtensions = mutableListOf<Triple<Int, Int, Int>>()
    /**
     * stores the position (x = first, y = second) of small extensions
     */
    val largeExtensions = mutableListOf<Pair<Int, Int>>()

    val map = ParkMap()

    val pools = mutableListOf<Pool>()

    override fun toString(): String {
        return "(small: $smallExtensions, large: $largeExtensions, \nmap: $map, \npools: $pools)"
    }

}

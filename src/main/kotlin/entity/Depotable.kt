package entity

/**
 * all implementations of this interface can be placed on the [Player.depot]
 *
 * only implementations are [Animal] and [Baby]
 *
 * implements [Parkable] as all Depot cards may also appear in the Waterpark
 */
sealed interface Depotable: Parkable {
    val type: AnimalType
}

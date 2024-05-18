package entity

/**
 * represents a baby animal
 *
 * implementation of [Depotable]
 *
 * @param type selects the [AnimalType] of this baby
 */
class Baby(override val type: AnimalType): Depotable
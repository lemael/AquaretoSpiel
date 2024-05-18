package entity

/**
 * represents a grown waterpark animal
 *
 * implementation of [Depotable] and [Wagonable]
 *
 * @property type selects the [AnimalType] of this entity
 * @property isFish decides whether nurses have an effect on this animal
 * @property isTrainable decides whether trainers have an effect on this animal
 * @property gender indicates the ability to reproduce
 */
class Animal(
    override val type: AnimalType,
    val isFish: Boolean = false,
    val isTrainable: Boolean = true,
    val gender: AnimalGender = AnimalGender.UNSPECIFIED,
    override var ID: Int = -1
): Depotable, Wagonable {
    override fun toString(): String {
        return "Animal(type=$type, isFish=$isFish, isTrainable=$isTrainable, gender=$gender)"
    }

}

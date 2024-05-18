package entity

/**
 * stores relevant information regarding one pool, including existing genders and [members]
 */
class Pool(val type: AnimalType){
    var numMale: Int = 0
    var numFemale: Int = 0

    /**
     * a list of coordinates
     *
     * members of this [Pool] can be accessed via [Waterpark.map]
     */
    val members = mutableListOf<Pair<Int, Int>>()

    override fun toString(): String {
        return "(type: $type, numMale: $numMale, numFemale: $numFemale, members: $members)"
    }

}

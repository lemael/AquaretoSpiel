package entity

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.IOException

/**
 * @property map translates between the coordinates to the [Parkable] in that position
 *
 * should be accessed via [addEntry], [apply]
 */
@JsonIgnoreProperties(ignoreUnknown = true) // ignore field size
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY) // make map serializable while being private
class ParkMap {
    @JsonSerialize(keyUsing = PairMapKeySerializer::class)
    @JsonDeserialize(keyUsing = PairMapKeyDeserializer::class)
    private val map = mutableMapOf<Pair<Int, Int>, Parkable>()

    /**
     * initialize the map with all default tiles being empty
     */
    init {
        defaultTiles.forEach { addEntry(it, Empty()) }
    }

    val size: Int
        get() = map.size

    /**
     * adds an element to the [map]
     * @param location used as the key for the map
     * @param tile used as the value for the map
     */
    fun addEntry(location: Pair<Int, Int>, tile: Parkable) {
        require(!map.containsKey(location))
        map[location] = tile
    }

    /**
     * removes an element from the [map]
     * @param location the key that has to be removed
     */
    fun removeEntry(location: Pair<Int, Int>) {
        require(map.containsKey(location))
        map.remove(location)
    }

    /**
     * gets a list of neighbors for a tile. Uses the overloaded get().
     * */
    fun getNeighbor(x: Int, y: Int): MutableList<Parkable> {
        val neighbors = mutableListOf<Parkable>()
        if (x - 1 > 0) {
            neighbors += get(x - 1, y)
        } //might fail due to the checkNotNull in the get()
        if (y - 1 > 0) {
            neighbors += get(x, y - 1)
        }
        neighbors += get(x + 1, y)
        neighbors += get(x, y + 1)

        return neighbors
    }

    /**
     * looks the given coordinates up in the [ParkMap.map] and returns the stored value
     * @param x the first part of the key for the map
     * @param y the first part of the key for the map
     * @throws IllegalStateException when the given key is not present in the map
     */
    operator fun get(x: Int, y: Int): Parkable {
        val tile = map[x to y]
        checkNotNull(tile) { "the given location ${x to y} was not stored in the map" }
        return tile
    }

    /**
     * looks the given coordinates up in the [ParkMap.map] and returns the stored value
     * @param pos the used key for the map
     * @throws IllegalStateException when the given key is not present in the map
     */
    operator fun get(pos: Pair<Int, Int>): Parkable {
        val tile = map[pos]
        checkNotNull(tile) { "the given location $pos was not stored in the map" }
        return tile
    }

    /**
     * writes the given [Parkable] to the given position in the [ParkMap.map]
     * @param x the first part of the key for the map
     * @param y the first part of the key for the map
     */
    operator fun set(x: Int, y: Int, newContent: Parkable) {
        map[x to y] = newContent
    }

    /**
     * writes the given [Parkable] to the given position in the [ParkMap.map]
     * @param pos the key for the map
     */
    operator fun set(pos: Pair<Int, Int>, newContent: Parkable) {
        map[pos] = newContent
    }

    /**
     * @return true, iff the given key is present in the map
     */
    operator fun contains(key: Pair<Int, Int>) = key in map

    /**
     * @return a map containing exactly those entries that fulfill the given predicate
     */
    fun filter(predicate: (Map.Entry<Pair<Int, Int>, Parkable>) -> Boolean): Map<Pair<Int, Int>, Parkable> {
        return map.filter { predicate(it) }
    }

    /**
     * @return a list of things that are produced when applying the given predicate to each element
     */
    fun <R> map(predicate: (Map.Entry<Pair<Int, Int>, Parkable>) -> R): List<R> {
        return map.map { predicate(it) }
    }

    /**
     * @return is true if Parkable exists in map
     */
    fun containsType(animalType: AnimalType): Boolean {
        return map.any { it ->
            val tile = it.value
            tile is Depotable && tile.type == animalType
        }
    }


    /**
     * @return a list of all indices that border the given [position] orthogonally or diagonally
     * List only contains those indices contained in the map
     */
    fun allNeighbours(position: Pair<Int, Int>): List<Pair<Int, Int>> {
        return mutableListOf(
            -1 to 1, 0 to 1, 1 to 1,
            -1 to 0, 0 to 0, 1 to 0,
            -1 to -1, 0 to -1, 1 to -1
        ).map { (x, y) ->
            x + position.first to y + position.second
        }.filter { it in map }
    }

    /**
     * @return a list of all indices that border the given [position] orthogonally.
     * List only contains those indices contained in the map
     */
    fun directNeighbours(position: Pair<Int, Int>): List<Pair<Int, Int>> {
        return mutableListOf(
            0 to 1,
            -1 to 0, 0 to 0, 1 to 0,
            0 to -1
        ).map { (x, y) ->
            x + position.first to y + position.second
        }.filter { it in map }
    }

    override fun toString(): String {
        return map.toString()
    }
}

/**
 * enable Pair<..> serialization for the map keys
 */
class PairMapKeySerializer : JsonSerializer<Pair<Int, Int>>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(value: Pair<Int, Int>?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.let { jGen ->
            value?.let { pair ->
                jGen.writeFieldName(mapper.writeValueAsString(pair))
            } ?: jGen.writeNull()
        }
    }
}

/**
 * enable Pair<..> deserialization for the map keys
 */
class PairMapKeyDeserializer : KeyDeserializer() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserializeKey(key: String?, ctxt: DeserializationContext?): Pair<Int, Int>? {
        return key?.let { mapper.readValue(key) }
    }
}

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.FileNotFoundException
import java.io.InputStream
import entity.*
import com.fasterxml.jackson.annotation.JsonCreator
import service.RootService

val csvMapper = CsvMapper().apply {
    enable(CsvParser.Feature.TRIM_SPACES)
    enable(CsvParser.Feature.SKIP_EMPTY_LINES)
    registerKotlinModule()
}

const val CSV_SEPARATOR = ';'

val schema = CsvSchema.builder()
    .addNumberColumn("ID")
    .addColumn("NAME")
    .addColumn("OPTION")
    .setColumnSeparator(CSV_SEPARATOR)
    .build()

object CSVLoader {
    private val tileById: Map<Int, Tile> by lazy {
        val inputStream = loadFile("tiles.csv")
        readCsv(inputStream).associateBy { it.id }
    }

    fun readCsv(inputStream: InputStream): List<Tile> =
        csvMapper.readerFor(Tile::class.java)
            .with(schema.withSkipFirstDataRow(true))
            .readValues<Tile>(inputStream).readAll()

    fun loadFile(filepath: String): InputStream =
        this::class.java.getResourceAsStream("/$filepath")
            ?: throw FileNotFoundException("Failed to load file from resources: $filepath")

    fun getTileById(id: Int): Tile? = tileById[id]

}

object CSVMapper {
    private val animalIdMap: MutableMap<Pair<AnimalType, String>, MutableList<Int>> = loadAnimalIds()

    private val coinIds: MutableList<Int> = coinIds()

    private fun coinIds(): MutableList<Int> {
        return CSVLoader.readCsv(CSVLoader.loadFile("tiles.csv"))
            .filter { it.name == "coin" }
            .map { it.id }
            .toMutableList()
    }

    private fun loadAnimalIds(): MutableMap<Pair<AnimalType, String>, MutableList<Int>> {
        val map = mutableMapOf<Pair<AnimalType, String>, MutableList<Int>>()
        val tileList = CSVLoader.readCsv(CSVLoader.loadFile("tiles.csv"))
        tileList.forEach { tile ->
            if (tile.name != "coin") {
                val animalType = mapToAnimalType(tile.name)
                val option = tile.option
                animalType?.let {
                    val key = Pair(it, option)
                    map.getOrPut(key) { mutableListOf() }.add(tile.id)
                }
            }
        }
        return map
    }

    fun mapToTiles(ids: List<Int>) : List<Any> {
        resetUsedIds()

        resetUsedIds()
        TODO()
    }

    fun mapToWagonableById(id: Int): Wagonable? {
        val tile = CSVLoader.getTileById(id) ?: return null
        return when (tile.name) {
            "coin" -> CoinCard()
            else -> mapToAnimal(tile)
        }
    }

    fun mapToInt(card: Any): Int {
        return when (card) {
            is CoinCard -> {
                coinIds.first()
            }

            is Animal -> {
                val key = Pair(
                    card.type, when {
                        card.gender == AnimalGender.MALE -> "m"
                        card.gender == AnimalGender.FEMALE -> "w"
                        typeIsTrainable(card) && !card.isTrainable -> "l"
                        card.isFish -> "f"
                        else -> "-"
                    }
                )
                val availableIds = animalIdMap[key]
                if (availableIds.isNullOrEmpty()) {
                    throw Exception("No available or unused ID found for this animal: $card. All possible mappings exhausted.")
                }

                val id = availableIds.first()
                return id
            }

            is Baby -> {
                val key = Pair(card.type, "o")
                val availableIds = animalIdMap[key]
                if (availableIds.isNullOrEmpty()) {
                    throw Exception("No available or unused ID found for this animal: $card. All possible mappings exhausted.")
                }

                val id = availableIds.first()
                return id

            }

            else -> throw IllegalArgumentException("Unsupported Wagonable type")
        }
    }
    private fun typeIsTrainable(card: Animal): Boolean{
        return (card.type in listOf(AnimalType.DOLPHIN, AnimalType.ORCA, AnimalType.SEALION))
    }

    private fun mapToAnimalType(name: String): AnimalType? = when (name) {
        "dolphin" -> AnimalType.DOLPHIN
        "orca" -> AnimalType.ORCA
        "sea_lion" -> AnimalType.SEALION
        "sea_turtle" -> AnimalType.TURTLE
        "hippopotamus" -> AnimalType.HIPPO
        "crocodile" -> AnimalType.CROCODILE
        "penguin" -> AnimalType.PENGUIN
        "polar_bear" -> AnimalType.ICEBEAR
        else -> null
    }

    private fun mapToAnimal(tile: Tile): Animal =
        Animal(
            type = mapToAnimalType(tile.name) ?: throw IllegalArgumentException("Unknown animal type: ${tile.name}"),
            isFish = tile.option == "f",
            isTrainable = tile.option != "l",
            gender = when (tile.option) {
                "m" -> AnimalGender.MALE
                "w" -> AnimalGender.FEMALE
                "-" -> AnimalGender.UNSPECIFIED
                else -> AnimalGender.UNSPECIFIED
            },
            ID = tile.id
        )

    fun resetUsedIds() {
        //usedIds.clear()
    }
}

data class Tile @JsonCreator constructor(
    @JsonProperty("ID") val id: Int,
    @JsonProperty("NAME") val name: String,
    @JsonProperty("OPTION") val option: String
)

class InitMessage(val employeeIDs: List<Int>)


fun main() {
    // Testing with a variety of animal types
    CSVMapper.resetUsedIds()
    val rootService = RootService()
    rootService.gameStateService.initializeGame(listOf("A", "B"), mutableListOf(HOTSEAT, HOTSEAT))

    val mainIds = rootService.aquarettoGame.board.mainStack.map { CSVMapper.mapToInt(it) }
    val otherIds = rootService.aquarettoGame.board.endOfGameStack.map { CSVMapper.mapToInt(it) }
    println(mainIds)
    println(otherIds)
}

fun main1() {

    println("Initial Forward Mapping (ID to Animal):")
    // Testing with a variety of animal types
    val initialMessage = InitMessage(listOf(11, 24, 37, 50)) // Mixed animal types
    val initialAnimals = initialMessage.employeeIDs.mapNotNull(CSVMapper::mapToWagonableById)
    initialAnimals.forEach { animal ->
        println(animal)
    }

    // Reverse mapping for initial animals
    println("\nInitial Reverse Mapping (Animal to ID):")
    val initialMappedBackIds = initialAnimals.mapNotNull(CSVMapper::mapToInt)
    println(initialMappedBackIds)

    // Testing with specific duplicates
    println("\n\nForward Mapping with Specific Duplicates (e.g., penguin;f):")
    val duplicateMessage =
        InitMessage(listOf(94, 95, 96, 97, 98, 99)) // Specifically "penguin;f" for testing duplicates
    val duplicateAnimals = duplicateMessage.employeeIDs.mapNotNull(CSVMapper::mapToWagonableById)
    duplicateAnimals.forEach { animal ->
        println(animal)
    }

    // Reverse mapping for duplicates, expecting to hit the limit
    println("\nReverse Mapping with Duplicate Handling:")
    val duplicateFemalePenguin = duplicateAnimals.first() // Assume all are the same for this test.
    val reverseMappedDuplicateIds = mutableListOf<Int>()
    try {
        for (i in 1..7) { // Attempting more than the available duplicates.
            reverseMappedDuplicateIds.add(CSVMapper.mapToInt(duplicateFemalePenguin))
        }
    } catch (e: Exception) {
        println("Expected failure after exhausting duplicates: ${e.message}")
    }
    println("Mapped IDs for duplicates (6 successes expected, 7th fails): $reverseMappedDuplicateIds")

    // Resetting used IDs for further testing
    println("\nResetting used IDs...")
    CSVMapper.resetUsedIds()

    // Reverse mapping after reset, demonstrating ID reuse
    println("\nReverse Mapping After Reset (Testing ID reuse):")
    val idAfterReset = CSVMapper.mapToInt(duplicateFemalePenguin)
    println("First ID after reset: $idAfterReset")

    // Testing reverse mapping
    CSVMapper.resetUsedIds() // Ensure fresh start
    val testAnimal = initialAnimals.firstOrNull() ?: return println("No animals for idempotency test.")
    val firstMapping = CSVMapper.mapToInt(testAnimal)
    CSVMapper.resetUsedIds() // Reset after first mapping
    val secondMapping = CSVMapper.mapToInt(testAnimal)
    println("\nTesting Reverse Mapping Idempotency With Resets:")
    println("First Mapping ID: $firstMapping, Second Mapping ID (After Reset): $secondMapping")


    // Resetting used IDs for coin mapping test
    println("\nResetting used IDs for coin tests...")
    CSVMapper.resetUsedIds()

    // Forward mapping for coin IDs
    println("\nForward Mapping for Coin IDs:")
    val coinIds = listOf(1, 2, 3) // Example coin IDs
    val coins = coinIds.mapNotNull(CSVMapper::mapToWagonableById)
    coins.forEach { coin ->
        println(coin)
    }

    // Reverse mapping for coins
    println("\nReverse Mapping for Coins:")
    val mappedBackCoinIds = coins.mapNotNull(CSVMapper::mapToInt)
    println(mappedBackCoinIds)

    // Attempt reverse mapping again for coins, expecting failure if all coin IDs are used
    println("\nAttempt Reverse Mapping Again for Coins (Expect Failure/Exception):")
    try {
        val extraCoin = CoinCard() // Assuming we're trying to map an additional coin
        val extraCoinId = CSVMapper.mapToInt(extraCoin)
        println(extraCoinId)
    } catch (e: Exception) {
        println("Expected failure after exhausting coin IDs: ${e.message}")
    }
}




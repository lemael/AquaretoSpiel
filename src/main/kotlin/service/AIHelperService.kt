package service

import entity.*

/**
 * This class hold helper functions, needed by the AIs.
 */
class AIHelperService() : AbstractRefreshingService() {
    /**
     * This function returns the amount of animals (of a given type), which are
     * stored in the provided park
     *
     * @param park is the provided park
     * @param type is the type, which is counted
     * @return the amount of animals
     */
    fun countAnimals(park: Waterpark, type: AnimalType): Int {
        val pool = park.pools.find { pool -> pool.type == type }

        // no pool with this type exists
        if (pool == null) return 0

        // return pool size
        return pool.members.size
    }

    /**
     * This function returns a list of all legal tiles, where a card can be placed.
     *
     * @param waterpark is a reference to the affected Waterpark
     * @param animal is the Type of the card, which should be placed
     * @return a list, containing the legal places
     */
    fun legalPlaces(waterpark: Waterpark, animal: AnimalType): List<Pair<Int, Int>> {
        val parkMap = waterpark.map

        // Checks if legal places exist at (too many pools)
        if (!parkMap.containsType(animal) && waterpark.pools.size >= waterpark.maxPools) {
            return listOf<Pair<Int, Int>>()
        }

        // Determines all available empty tiles
        val emptyTileCords = parkMap.filter { it.value is Empty }.map { it.key }

        // Removes all tiles, which are directly neighbouring another animal-type
        val notNextToOtherAnimalCords = emptyTileCords.filter {
            var isNotNextToOtherAnimal = true

            parkMap.directNeighbours(it).map { neighbourCords -> parkMap[neighbourCords] }.forEach { neighbourTile ->
                if (neighbourTile is Depotable && neighbourTile.type != animal) {
                    isNotNextToOtherAnimal = false
                }
            }

            isNotNextToOtherAnimal
        }

        // If no pool exists, all tiles, which are not directly neighbouring another animal-type, are legal
        if (!parkMap.containsType(animal)) {
            return notNextToOtherAnimalCords
        }

        // If a pool of the same type already exists, only the tiles, which are directly neighbouring it, are legal.
        val nextToExistingPoolCords = notNextToOtherAnimalCords.filter {
            var isNextToExistingPool = false

            parkMap.directNeighbours(it).map { neighbourCords -> parkMap[neighbourCords] }.forEach { neighbourTile ->
                if (neighbourTile is Depotable && neighbourTile.type == animal) {
                    isNextToExistingPool = true
                }
            }

            isNextToExistingPool
        }

        return nextToExistingPoolCords
    }

    fun legalSmallExtension(parkMap: ParkMap): List<Pair<Pair<Int, Int>, Int>> {
        val extensions = listOf((1 to 5) to TOP_RIGHT, (5 to 2) to BOTTOM_RIGHT)
        return extensions.filter { extension -> !parkMap.contains(extension.first) }
    }

    fun legalLargeExtension(parkMap: ParkMap): List<Pair<Int, Int>> {
        val extensions = listOf(-2 to 2, 2 to -2)
        return  extensions.filter { extension -> !parkMap.contains(extension) }
    }

    /**
     * 1 draw card -> which wagon
     * 2 take wagon -> which wagon -> which card -> which pos
     * 3 Extension -> large or small(rotation) -> pos
     * 4 Buy/sell -> from -> pos
     */
    fun legalMoves(game: Aquaretto): List<Int> {
        val moves = mutableListOf<Int>()
        val player = game.currentPlayer()
        val park = game.currentPlayer().park

        if (game.board.transportWagons.any() { wagon -> wagon.any() { it is Empty } }) moves.add(1)
        if (game.board.transportWagons.any() { wagon -> wagon.any() { it !is Empty } }) moves.add(2)
        if (player.numCoins >= 1 && park.smallExtensions.size + park.largeExtensions.size < 4) moves.add(3)
        if (player.numCoins >= 2 && game.players.any() { it.depot.isNotEmpty() }) moves.add(4)

        return moves
    }
}
package entity

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

/**
 * positions on the [Waterpark] that can be used to reference the respective employee position
 */
val CASHIER = 0 to 1
/**
 * positions on the [Waterpark] that can be used to reference the respective employee position
 */
val NURSE = 1 to 0
/**
 * positions on the [Waterpark] that can be used to reference the respective employee position
 */
val MANAGER = 0 to 0


/**
 * describes the orientation of a small extension by specifying which corner is missing
 */
const val TOP_LEFT = 0
/**
 * describes the orientation of a small extension by specifying which corner is missing
 */
const val TOP_RIGHT = 1
/**
 * describes the orientation of a small extension by specifying which corner is missing
 */
const val BOTTOM_LEFT = 2
/**
 * describes the orientation of a small extension by specifying which corner is missing
 */
const val BOTTOM_RIGHT = 3


const val HOTSEAT = 0
const val ONLINE = 1
const val DUMB = 2
const val SMART = 3

/**
 * all the positions that are present in the default waterpark (i.e. without extensions)
 */
val defaultTiles = listOf(
            1 to 4, 2 to 4, 3 to 4,
    0 to 3, 1 to 3, 2 to 3, 3 to 3, 4 to 3,
    0 to 2, 1 to 2, 2 to 2, 3 to 2, 4 to 2,
            1 to 1, 2 to 1, 3 to 1, 4 to 1,
                    2 to 0, 3 to 0
)


val mapper = jacksonObjectMapper()

val hostScenePlayerNumber = 2
val hostSceneBotNumber = 0

var onlinePlayerIs = HOTSEAT
    set(x: Int) {
        field = x
        println("online player is now set as: $onlinePlayerIs")
    }

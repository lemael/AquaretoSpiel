package entity

import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * all implementations of this interface can be placed on the transport wagons
 *
 * only implementations are [CoinCard] and [Animal]
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "subType"
)
sealed interface Wagonable{
    var ID: Int
}

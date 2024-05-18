package entity

import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * all implementations of this interface can be placed in the [Waterpark]
 *
 * only implementations are [Empty], [Worker] and all [Depotable]s
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "subType"
)
sealed interface Parkable

package com.kitchenkeeper.data

import java.math.BigDecimal
import java.math.RoundingMode

private const val GRAMS_PER_POUND = 453.59237

/** How pantry and grocery amounts are measured. */
enum class MeasureUnit(val label: String, private val plural: String) {
    LB("lb", "lb"),
    G("g", "g"),
    CUP("cup", "cups"),
    CONTAINER("container", "containers");

    fun labelFor(quantity: Double): String = if (quantity <= 1.0) label else plural
}

/** How a recipe says an ingredient is used. Container amounts are fixed; the rest need a quantity. */
enum class UsageUnit(val label: String, val fixedQuantity: Double?, val measureUnit: MeasureUnit) {
    LB("LB", null, MeasureUnit.LB),
    G("G", null, MeasureUnit.G),
    CUP("Cup", null, MeasureUnit.CUP),
    HALF_CONTAINER("Half container", 0.5, MeasureUnit.CONTAINER),
    FULL_CONTAINER("Full container", 1.0, MeasureUnit.CONTAINER);

    val needsQuantity: Boolean get() = fixedQuantity == null

    fun toAmount(quantity: Double?): Amount? =
        (fixedQuantity ?: quantity)?.takeIf { it > 0 }?.let { Amount(it, measureUnit) }

    companion object {
        /** Sensible default when an ingredient is first picked from the pantry. */
        fun defaultFor(unit: MeasureUnit?): UsageUnit = when (unit) {
            MeasureUnit.LB -> LB
            MeasureUnit.G -> G
            MeasureUnit.CUP -> CUP
            MeasureUnit.CONTAINER, null -> FULL_CONTAINER
        }
    }
}

data class Amount(val quantity: Double, val unit: MeasureUnit) {
    /** This amount expressed in [target], or null if the units can't be compared (e.g. cups vs. pounds). */
    fun convertTo(target: MeasureUnit): Double? = when {
        unit == target -> quantity
        unit == MeasureUnit.LB && target == MeasureUnit.G -> quantity * GRAMS_PER_POUND
        unit == MeasureUnit.G && target == MeasureUnit.LB -> quantity / GRAMS_PER_POUND
        else -> null
    }

    fun formatted(): String = "${formatQuantity(quantity)} ${unit.labelFor(quantity)}"
}

fun formatQuantity(quantity: Double): String =
    BigDecimal(quantity).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()

fun roundQuantity(quantity: Double): Double =
    BigDecimal(quantity).setScale(2, RoundingMode.HALF_UP).toDouble()

/** Both null, or both set — anything else isn't a complete amount. */
fun amountOf(quantity: Double?, unit: MeasureUnit?): Amount? =
    if (quantity != null && unit != null) Amount(quantity, unit) else null

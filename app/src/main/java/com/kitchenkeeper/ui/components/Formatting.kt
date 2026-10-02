package com.kitchenkeeper.ui.components

import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

fun LocalDate.formatted(): String = format(dateFormatter)

/** Parses a user-typed quantity; null if blank, unparseable, or not positive. */
fun parseQuantity(text: String): Double? = text.trim().toDoubleOrNull()?.takeIf { it > 0 }

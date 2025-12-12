package com.tagmypet.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object NumberUtils {

    /**
     * Formata um número grande (ex: 12500 -> 12.5K, 1250000 -> 1.2M).
     * Mantém o formato pt-BR para o separador decimal.
     */
    fun formatCount(count: Int): String {
        if (count < 1000) return count.toString()

        val suffixes = listOf('K', 'M', 'B')
        var value = count.toDouble()
        var suffixIndex = 0

        while (value >= 1000 && suffixIndex < suffixes.size) {
            value /= 1000
            suffixIndex++
        }

        // Formato para uma casa decimal (ex: 1.2)
        val symbols = DecimalFormatSymbols(Locale("pt", "BR"))
        val format = DecimalFormat("#.#", symbols)

        val formattedValue = format.format(value)
        val suffix = suffixes.getOrNull(suffixIndex - 1) ?: ""

        return "$formattedValue$suffix"
    }
}
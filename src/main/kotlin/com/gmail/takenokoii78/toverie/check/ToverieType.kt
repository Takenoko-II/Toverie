package com.gmail.takenokoii78.toverie.check

import com.gmail.takenokoii78.toverie.parse.TypeIdentifier
import java.util.Objects

open class ToverieType(val identifier: String, val parameters: List<ToverieType>) {
    override fun hashCode(): Int {
        return Objects.hash(identifier, parameters)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other === this) return true
        if (other !is ToverieType) return false
        if (identifier != other.identifier) return false
        if (parameters.size != other.parameters.size) return false
        if (!(parameters zip other.parameters).all { (a, b) -> a == b }) return false
        return true
    }

    companion object {
        val BOOL = ToverieType(TypeIdentifier.BOOL.value, listOf())

        val BYTE = ToverieType(TypeIdentifier.BYTE.value, listOf())

        val SHORT = ToverieType(TypeIdentifier.SHORT.value, listOf())

        val INT = ToverieType(TypeIdentifier.INT.value, listOf())

        val LONG = ToverieType(TypeIdentifier.LONG.value, listOf())

        val FLOAT = ToverieType(TypeIdentifier.FLOAT.value, listOf())

        val DOUBLE = ToverieType(TypeIdentifier.DOUBLE.value, listOf())

        val CHAR = ToverieType(TypeIdentifier.CHAR.value, listOf())

        val STRING = ToverieType(TypeIdentifier.STRING.value, listOf())

        val VOID = ToverieType(TypeIdentifier.VOID.value, listOf())
    }
}

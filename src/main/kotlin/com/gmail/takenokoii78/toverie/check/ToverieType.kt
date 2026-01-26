package com.gmail.takenokoii78.toverie.check

import com.gmail.takenokoii78.toverie.parse.TypeIdentifier
import java.util.Objects

class ToverieType(val identifier: String, val parameters: List<ToverieType>) {
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
}

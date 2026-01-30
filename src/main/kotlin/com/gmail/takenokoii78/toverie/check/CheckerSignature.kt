package com.gmail.takenokoii78.toverie.check

import java.util.Objects

class CheckerFunctionArgument(val name: String, val type: ToverieType)

class CheckerSignature(val returns: ToverieType, val arguments: List<CheckerFunctionArgument>) {
    constructor(returns: ToverieType, vararg arguments: ToverieType) : this(
        returns,
        arguments.withIndex().map { (i, t) -> CheckerFunctionArgument("arg$i", t) }
    )

    override fun hashCode(): Int {
        return Objects.hash(returns, arguments)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other === this) return true

        if (other !is CheckerSignature) return false
        if (returns != other.returns) return false
        if (arguments.size != other.arguments.size) return false
        return (arguments zip other.arguments).all { (a, b) -> a.type == b.type }
    }

    fun isCallableBy(passedArguments: List<ToverieType>): Boolean {
        if (arguments.size != passedArguments.size) return false
        return (passedArguments zip arguments).all { (a, b) -> a == b.type }
    }
}

class CheckerSignatures {
    private val signatures = mutableSetOf<CheckerSignature>()

    fun add(signature: CheckerSignature) {
        signatures.add(signature)
    }

    fun lookup(selector: List<ToverieType>): CheckerSignature? {
        return signatures.find { it.isCallableBy(selector) }
    }
}

class CheckerFunctions {
    private val map = mutableMapOf<String, CheckerSignatures>()

    fun register(identifier: String, signature: CheckerSignature) {
        if (map.contains(identifier)) {
            map[identifier]!!.add(signature)
        }
        else {
            val s = CheckerSignatures()
            s.add(signature)
            map[identifier] = s
        }
    }

    fun has(identifier: String): Boolean {
        return map.contains(identifier)
    }

    fun get(identifier: String): CheckerSignatures {
        if (map.contains(identifier)) {
            return map[identifier]!!
        }
        else {
            throw ToverieCheckException("識別子 '$identifier' を持つ関数は存在しません")
        }
    }
}

package com.gmail.takenokoii78.toverie.check

import com.gmail.takenokoii78.toverie.parse.BinaryOperation
import com.gmail.takenokoii78.toverie.parse.UnaryOperation
import kotlin.reflect.KClass

class OperatorReturnRegistry<T : Enum<T>>(private val operation: KClass<T>, initializer: OperatorReturnRegistry<T>.() -> Unit) {
    private val functions = CheckerFunctions()

    init {
        initializer()
    }

    fun register(operation: T, signature: CheckerSignature) {
        functions.register(operation.name, signature)
    }

    fun get(operation: T, arguments: List<ToverieType>): CheckerSignature {
        return functions.get(operation.name).lookup(arguments) ?: throw ToverieCheckException("関数シグネチャ '${operation.name}(${arguments.joinToString(", ")})' が見つかりません")
    }

    companion object {
        val UNARY = OperatorReturnRegistry(UnaryOperation::class) {
            register(UnaryOperation.MINUS, CheckerSignature(
                ToverieType.INT, ToverieType.INT
            ))

            register(UnaryOperation.MINUS, CheckerSignature(
                ToverieType.LONG, ToverieType.LONG
            ))

            register(UnaryOperation.MINUS, CheckerSignature(
                ToverieType.FLOAT, ToverieType.FLOAT
            ))

            register(UnaryOperation.MINUS, CheckerSignature(
                ToverieType.DOUBLE, ToverieType.DOUBLE
            ))
        }

        val BINARY = OperatorReturnRegistry(BinaryOperation::class) {
            register(BinaryOperation.PLUS, CheckerSignature(
                ToverieType.INT, ToverieType.INT, ToverieType.INT
            ))
        }
    }
}

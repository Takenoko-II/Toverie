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
            register(UnaryOperation.MINUS, CheckerSignature(ToverieType.INT, ToverieType.INT))
            register(UnaryOperation.MINUS, CheckerSignature(ToverieType.LONG, ToverieType.LONG))
            register(UnaryOperation.MINUS, CheckerSignature(ToverieType.FLOAT, ToverieType.FLOAT))
            register(UnaryOperation.MINUS, CheckerSignature(ToverieType.DOUBLE, ToverieType.DOUBLE))

            register(UnaryOperation.LOGICAL_NOT, CheckerSignature(ToverieType.BOOL, ToverieType.BOOL))

            register(UnaryOperation.INCREMENT, CheckerSignature(ToverieType.INT, ToverieType.INT))
            register(UnaryOperation.INCREMENT, CheckerSignature(ToverieType.LONG, ToverieType.LONG))

            register(UnaryOperation.DECREMENT, CheckerSignature(ToverieType.INT, ToverieType.INT))
            register(UnaryOperation.DECREMENT, CheckerSignature(ToverieType.LONG, ToverieType.LONG))

            register(UnaryOperation.BIT_INVERSE, CheckerSignature(ToverieType.INT, ToverieType.INT))
        }

        val BINARY = OperatorReturnRegistry(BinaryOperation::class) {
            register(BinaryOperation.PLUS, CheckerSignature(ToverieType.INT, ToverieType.INT, ToverieType.INT))
            register(BinaryOperation.PLUS, CheckerSignature(ToverieType.LONG, ToverieType.LONG, ToverieType.LONG))
            register(BinaryOperation.PLUS, CheckerSignature(ToverieType.FLOAT, ToverieType.FLOAT, ToverieType.FLOAT))
            register(BinaryOperation.PLUS, CheckerSignature(ToverieType.DOUBLE, ToverieType.DOUBLE, ToverieType.DOUBLE))

            register(BinaryOperation.MINUS, CheckerSignature(ToverieType.INT, ToverieType.INT, ToverieType.INT))
            register(BinaryOperation.MINUS, CheckerSignature(ToverieType.LONG, ToverieType.LONG, ToverieType.LONG))
            register(BinaryOperation.MINUS, CheckerSignature(ToverieType.FLOAT, ToverieType.FLOAT, ToverieType.FLOAT))
            register(BinaryOperation.MINUS, CheckerSignature(ToverieType.DOUBLE, ToverieType.DOUBLE, ToverieType.DOUBLE))

            register(BinaryOperation.MULTIPLY, CheckerSignature(ToverieType.INT, ToverieType.INT, ToverieType.INT))
            register(BinaryOperation.MULTIPLY, CheckerSignature(ToverieType.LONG, ToverieType.LONG, ToverieType.LONG))
            register(BinaryOperation.MULTIPLY, CheckerSignature(ToverieType.FLOAT, ToverieType.FLOAT, ToverieType.FLOAT))
            register(BinaryOperation.MULTIPLY, CheckerSignature(ToverieType.DOUBLE, ToverieType.DOUBLE, ToverieType.DOUBLE))

            register(BinaryOperation.DIVIDE, CheckerSignature(ToverieType.INT, ToverieType.INT, ToverieType.INT))
            register(BinaryOperation.DIVIDE, CheckerSignature(ToverieType.LONG, ToverieType.LONG, ToverieType.LONG))
            register(BinaryOperation.DIVIDE, CheckerSignature(ToverieType.FLOAT, ToverieType.FLOAT, ToverieType.FLOAT))
            register(BinaryOperation.DIVIDE, CheckerSignature(ToverieType.DOUBLE, ToverieType.DOUBLE, ToverieType.DOUBLE))
        }
    }
}

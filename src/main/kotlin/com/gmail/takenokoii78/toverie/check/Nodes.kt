package com.gmail.takenokoii78.toverie.check

import com.gmail.takenokoii78.toverie.parse.BinaryOperation
import com.gmail.takenokoii78.toverie.parse.IncrementDecrementMode
import com.gmail.takenokoii78.toverie.parse.UnaryOperation

abstract class ToverieNode

abstract class ToverieExpression(val type: ToverieType) : ToverieNode()

abstract class ToverieStatement : ToverieNode()

class ToverieBlock(
    val nodes: List<ToverieNode>,
    val returns: ToverieType?
) : ToverieNode()

class LiteralValueExpression<T : Any>(
    val value: T,
    type: ToverieType
) : ToverieExpression(type)

open class UnaryExpression(
    val target: ToverieExpression,
    val operation: UnaryOperation,
    type: ToverieType
) : ToverieExpression(type)

class IncrementDecrementExpression(
    target: VariableReferenceExpression,
    operation: UnaryOperation,
    val mode: IncrementDecrementMode,
    type: ToverieType
) : UnaryExpression(target, operation, type)

class BinaryExpression(
    val left: ToverieExpression,
    val right: ToverieExpression,
    val operation: BinaryOperation,
    type: ToverieType
) : ToverieExpression(type)

class VariableReferenceExpression(
    val identifier: String,
    val variable: CheckerVariable
) : ToverieExpression(variable.type)

class FunctionCallExpression(
    val identifier: String,
    val arguments: List<ToverieExpression>,
    val signature: CheckerSignature
) : ToverieExpression(signature.returns)

class FieldReferenceExpression(
    val identifier: String,
    val target: ToverieExpression,
    type: ToverieType
) : ToverieExpression(type)

class MethodCallExpression(
    val target: ToverieExpression,
    val identifier: String,
    val arguments: List<ToverieExpression>,
    val signature: CheckerSignature
) : ToverieExpression(signature.returns)

class ArrayExpression(
    val elements: List<ToverieExpression>,
    type: ToverieType
) : ToverieExpression(type)

class VariableStatement(
    val identifier: String,
    val variable: CheckerVariable,
    val value: ToverieExpression?
) : ToverieStatement()

class FunctionStatement(
    val identifier: String,
    val signature: CheckerSignature,
    val block: ToverieBlock
) : ToverieStatement()

class ReturnStatement(
    val value: ToverieExpression?
) : ToverieStatement()

class IfElseStatement(
    val condition: ToverieExpression,
    val thenBlock: ToverieBlock,
    val elseBlock: ToverieBlock?
) : ToverieStatement()

class ForInStatement(
    val label: String?,
    val identifier: String,
    val variable: CheckerVariable,
    val collection: ToverieExpression,
    val block: ToverieBlock
) : ToverieStatement()

class WhileStatement(
    val label: String?,
    val condition: ToverieExpression,
    val block: ToverieBlock
) : ToverieStatement()

class ContinueStatement(
    val label: String?
) : ToverieStatement()

class BreakStatement(
    val label: String?
) : ToverieStatement()

package com.gmail.takenokoii78.toverie.check

import com.gmail.takenokoii78.toverie.parse.BinaryOperation
import com.gmail.takenokoii78.toverie.parse.UnaryOperation

abstract class ToverieNode

abstract class ToverieExpression(val type: ToverieType) : ToverieNode()

abstract class ToverieStatement : ToverieNode()

class ToverieBlock(
    val nodes: List<ToverieNode>,
    val returns: ToverieType?
) : ToverieNode()

class UnaryExpression(
    val target: ToverieExpression,
    val operation: UnaryOperation,
    type: ToverieType
) : ToverieExpression(type)

class BinaryExpression(
    val left: ToverieExpression,
    val right: ToverieExpression,
    val operation: BinaryOperation,
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

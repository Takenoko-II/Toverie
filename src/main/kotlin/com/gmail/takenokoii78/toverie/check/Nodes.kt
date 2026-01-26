package com.gmail.takenokoii78.toverie.check

import com.gmail.takenokoii78.toverie.parse.UnaryOperation

abstract class ToverieNode

abstract class ToverieExpression(val type: ToverieType) : ToverieNode()

abstract class UnaryExpression(val target: ToverieExpression, val operation: UnaryOperation, type: ToverieType) : ToverieExpression(type)

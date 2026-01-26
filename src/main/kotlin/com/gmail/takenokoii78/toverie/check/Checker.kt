package com.gmail.takenokoii78.toverie.check

import com.gmail.takenokoii78.toverie.parse.UntypedBlockNode
import com.gmail.takenokoii78.toverie.parse.UntypedExpressionNode
import com.gmail.takenokoii78.toverie.parse.UntypedFuncStatementNode
import com.gmail.takenokoii78.toverie.parse.UntypedFunctionArgument
import com.gmail.takenokoii78.toverie.parse.UntypedReturnStatementNode
import com.gmail.takenokoii78.toverie.parse.UntypedTypeAnnotationNode
import com.gmail.takenokoii78.toverie.parse.UntypedUnaryExpressionNode
import com.gmail.takenokoii78.toverie.parse.UntypedVarConstStatementNode

class Checker(private val block: UntypedBlockNode) {
    private val functions = mutableMapOf<String, ToverieFunction>()

    private val variables = mutableMapOf<String, ToverieVariable>()

    private val globalScope = CheckerScope()

    fun check() {
        for (node in block.nodes) {
            when (node) {
                is UntypedFuncStatementNode -> globalFunc(node)
                is UntypedVarConstStatementNode -> globalVar(node)
            }
        }
    }

    private fun globalVar(node: UntypedVarConstStatementNode) {
        if (node.value == null) {
            throw ToverieCheckException("グローバル変数は常に初期化が求められます")
        }

        if (variables.contains(node.identifier)) {
            throw ToverieCheckException("グローバル変数 '${node.identifier}' の宣言が重複しています")
        }

        variables[node.identifier] = ToverieVariable(
            typeAnnotation(node.type),
            expression(globalScope, node.value),
            initialAssigned = true
        )
    }

    private fun globalFunc(stmt: UntypedFuncStatementNode) {
        val returns = typeAnnotation(stmt.returns)

        functions[stmt.identifier] = ToverieFunction(
            returns,
            arguments(stmt.arguments),
            block(globalScope, stmt.block, returns)
        )
    }

    private fun arguments(args: List<UntypedFunctionArgument>): List<ToverieFunctionArgument> {
        return args.map { ToverieFunctionArgument(it.name, typeAnnotation(it.type)) }
    }

    private fun typeAnnotation(annotation: UntypedTypeAnnotationNode): ToverieType {
        return ToverieType(annotation.identifier, annotation.parameters.map { typeAnnotation(it) })
    }

    private fun block(scope: CheckerScope, block: UntypedBlockNode, expectedReturns: ToverieType): Block {
        for (node in block.nodes) {
            val typedNode = when (node) {
                is UntypedVarConstStatementNode -> {
                    variableStatement(node)
                }
                is UntypedReturnStatementNode -> {
                    returnStatement(node)
                    break
                }
                else -> {
                    throw ToverieCheckException("未実装の文ノードです")
                }
            }
        }
    }

    private fun variableStatement(node: UntypedVarConstStatementNode) {

    }

    private fun returnStatement(node: UntypedReturnStatementNode) {

    }

    private fun expression(scope: CheckerScope, node: UntypedExpressionNode): ToverieExpression {
        return when (node) {
            is UntypedUnaryExpressionNode -> unaryExpression(scope, node)
            else -> {
                throw ToverieCheckException("未実装の式ノードです")
            }
        }
    }

    private fun unaryExpression(scope: CheckerScope, node: UntypedUnaryExpressionNode): UnaryExpression {
        val target = expression(scope, node.target)
    }
}

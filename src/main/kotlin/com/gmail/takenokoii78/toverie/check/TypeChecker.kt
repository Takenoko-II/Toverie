package com.gmail.takenokoii78.toverie.check

import com.gmail.takenokoii78.toverie.parse.UntypedBinaryExpressionNode
import com.gmail.takenokoii78.toverie.parse.UntypedBlockNode
import com.gmail.takenokoii78.toverie.parse.UntypedExpressionNode
import com.gmail.takenokoii78.toverie.parse.UntypedFuncStatementNode
import com.gmail.takenokoii78.toverie.parse.UntypedFunctionArgument
import com.gmail.takenokoii78.toverie.parse.UntypedIfElseStatementNode
import com.gmail.takenokoii78.toverie.parse.UntypedReturnStatementNode
import com.gmail.takenokoii78.toverie.parse.UntypedStatementNode
import com.gmail.takenokoii78.toverie.parse.UntypedTypeAnnotationNode
import com.gmail.takenokoii78.toverie.parse.UntypedUnaryExpressionNode
import com.gmail.takenokoii78.toverie.parse.UntypedVarConstStatementNode

class TypeChecker(private val block: UntypedBlockNode) {
    fun check(): ToverieBlock {
        val globalScope = collectGlobal()

        val nodes = mutableListOf<FunctionStatement>()

        for (node in block.nodes) {
            if (node !is UntypedFuncStatementNode) continue

            val statement = function(globalScope, node)
            nodes.add(statement)
        }

        return ToverieBlock(nodes, null)
    }

    private fun collectGlobal(): CheckerScope {
        val scope = CheckerScope()

        for (node in block.nodes) {
            if (node !is UntypedFuncStatementNode) continue

            val function = header(node)
            scope.addSignature(node.identifier, function)
        }

        return scope
    }

    private fun header(stmt: UntypedFuncStatementNode): CheckerSignature {
        val returns = typeAnnotation(stmt.returns)

        return CheckerSignature(
            returns,
            arguments(stmt.arguments)
        )
    }

    private fun arguments(args: List<UntypedFunctionArgument>): List<CheckerFunctionArgument> {
        return args.map { CheckerFunctionArgument(it.name, typeAnnotation(it.type)) }
    }

    private fun typeAnnotation(annotation: UntypedTypeAnnotationNode): ToverieType {
        return ToverieType(annotation.identifier, annotation.parameters.map { typeAnnotation(it) })
    }

    private fun function(globalScope: CheckerScope, node: UntypedFuncStatementNode): FunctionStatement {
        val arguments = arguments(node.arguments)
        val signature = globalScope.getSignature(node.identifier, arguments.map { it.type })

        val returns = signature.returns

        val block = block(globalScope, node.block) {
            addFlag(ScopeFlag.FUNCTION)
        }

        if (block.returns == null || block.returns == returns) {
            return FunctionStatement(node.identifier, signature, block)
        }
        else {
            throw ToverieCheckException("関数の戻り値が不正です: $returns に対して ${block.returns} が渡されました")
        }
    }

    private fun block(scope: CheckerScope, block: UntypedBlockNode, initializer: CheckerScope.() -> Unit): ToverieBlock {
        val blockScope = CheckerScope(scope)
        blockScope.initializer()

        val nodes = mutableListOf<ToverieNode>()
        var returns: ToverieType? = null

        for (node in block.nodes) {
            when (node) {
                is UntypedExpressionNode -> {
                    nodes.add(expression(scope, node))
                }
                is UntypedStatementNode -> {
                    val statement = statement(scope, node)

                    when (statement) {
                        is ReturnStatement -> {
                            returns = statement.value?.type ?: ToverieType.VOID
                        }
                        is IfElseStatement -> {
                            if (statement.thenBlock.returns != null && statement.elseBlock?.returns != null) {
                                if (statement.thenBlock.returns == statement.elseBlock.returns) {
                                    returns = statement.thenBlock.returns
                                }
                            }
                        }
                    }

                    nodes.add(statement)
                }
                else -> {
                    throw ToverieCheckException("未実装の文ノードです")
                }
            }
        }

        return ToverieBlock(nodes, returns)
    }

    private fun statement(scope: CheckerScope, node: UntypedStatementNode): ToverieStatement {
        return when (node) {
            is UntypedVarConstStatementNode -> varStatement(scope, node)
            is UntypedReturnStatementNode -> returnStatement(scope, node)
            is UntypedIfElseStatementNode -> ifElseStatement(scope, node)
            else -> throw ToverieCheckException("未実装の文クラスです: " + node.javaClass.name)
        }
    }

    private fun varStatement(scope: CheckerScope, node: UntypedVarConstStatementNode): VariableStatement {
        val type = typeAnnotation(node.type)

        val (variable, value) = if (node.value == null) {
            CheckerVariable(type, initialAssigned = false) to null
        }
        else {
            val value = expression(scope, node.value)

            if (value.type != type) {
                throw ToverieCheckException("変数宣言 '${node.identifier}' の型と式の型が一致しません")
            }

            CheckerVariable(type, initialAssigned = true) to value
        }

        scope.addVariable(node.identifier, variable)

        return VariableStatement(node.identifier, variable, value)
    }

    private fun returnStatement(scope: CheckerScope, node: UntypedReturnStatementNode): ReturnStatement {
        if (node.value == null) {
            return ReturnStatement(null)
        }
        else {
            val value = expression(scope, node.value)
            return ReturnStatement(value)
        }
    }

    private fun ifElseStatement(scope: CheckerScope, node: UntypedIfElseStatementNode): IfElseStatement {
        val condition = expression(scope, node.condition)

        if (condition.type != ToverieType.BOOL) {
            throw ToverieCheckException("if-else の条件式は真偽値でなければなりません")
        }

        val thenBlock = block(scope, node.thenBlock) {}
        val elseBlock = if (node.elseBlock == null) null else block(scope, node.elseBlock) {}

        return IfElseStatement(
            condition,
            thenBlock,
            elseBlock
        )
    }

    private fun expression(scope: CheckerScope, node: UntypedExpressionNode): ToverieExpression {
        return when (node) {
            is UntypedUnaryExpressionNode -> unaryExpression(scope, node)
            is UntypedBinaryExpressionNode -> binaryExpression(scope, node)
            else -> throw ToverieCheckException("未実装の式ノードです")
        }
    }

    private fun unaryExpression(scope: CheckerScope, node: UntypedUnaryExpressionNode): UnaryExpression {
        val target = expression(scope, node.target)

        val signature = OperatorReturnRegistry.UNARY.get(node.operation, listOf(target.type))

        return UnaryExpression(
            target,
            node.operation,
            signature.returns
        )
    }

    private fun binaryExpression(scope: CheckerScope, node: UntypedBinaryExpressionNode): BinaryExpression {
        val left = expression(scope, node.leftTarget)
        val right = expression(scope, node.rightTarget)

        val signature = OperatorReturnRegistry.BINARY.get(node.operation, listOf(left.type, right.type))

        return BinaryExpression(
            left,
            right,
            node.operation,
            signature.returns
        )
    }
}

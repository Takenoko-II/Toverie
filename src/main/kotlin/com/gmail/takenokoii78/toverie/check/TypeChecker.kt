package com.gmail.takenokoii78.toverie.check

import com.gmail.takenokoii78.toverie.parse.TypeIdentifier
import com.gmail.takenokoii78.toverie.parse.UntypedArrayExpressionNode
import com.gmail.takenokoii78.toverie.parse.UntypedBinaryExpressionNode
import com.gmail.takenokoii78.toverie.parse.UntypedBlockNode
import com.gmail.takenokoii78.toverie.parse.UntypedBreakStatementNode
import com.gmail.takenokoii78.toverie.parse.UntypedContinueStatementNode
import com.gmail.takenokoii78.toverie.parse.UntypedExpressionNode
import com.gmail.takenokoii78.toverie.parse.UntypedFieldReferenceExpressionNode
import com.gmail.takenokoii78.toverie.parse.UntypedForInStatementNode
import com.gmail.takenokoii78.toverie.parse.UntypedFuncStatementNode
import com.gmail.takenokoii78.toverie.parse.UntypedFunctionArgument
import com.gmail.takenokoii78.toverie.parse.UntypedFunctionCallExpressionNode
import com.gmail.takenokoii78.toverie.parse.UntypedIfElseStatementNode
import com.gmail.takenokoii78.toverie.parse.UntypedIncrementDecrementExpressionNode
import com.gmail.takenokoii78.toverie.parse.UntypedLiteralValueExpressionNode
import com.gmail.takenokoii78.toverie.parse.UntypedReturnStatementNode
import com.gmail.takenokoii78.toverie.parse.UntypedStatementNode
import com.gmail.takenokoii78.toverie.parse.UntypedTypeAnnotationNode
import com.gmail.takenokoii78.toverie.parse.UntypedUnaryExpressionNode
import com.gmail.takenokoii78.toverie.parse.UntypedVarConstStatementNode
import com.gmail.takenokoii78.toverie.parse.UntypedVariableReferenceExpressionNode
import com.gmail.takenokoii78.toverie.parse.UntypedWhileStatementNode

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
            for (argument in arguments) {
                addVariable(argument.name, CheckerVariable(
                    argument.type,
                    initialAssigned = true
                ))
            }
        }

        if ((block.returns == null && signature.returns == ToverieType.VOID) || block.returns == returns) {
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
                    nodes.add(expression(blockScope, node))
                }
                is UntypedStatementNode -> {
                    val statement = statement(blockScope, node)

                    when (statement) {
                        is ReturnStatement -> {
                            if (!blockScope.hasFlag(ScopeFlag.FUNCTION)) {
                                throw ToverieCheckException("関数スコープの外では return ステートメントを使用できません")
                            }

                            returns = statement.value?.type ?: ToverieType.VOID
                        }
                        is ContinueStatement -> {
                            if (!blockScope.hasFlag(ScopeFlag.ITERATION)) {
                                throw ToverieCheckException("反復スコープの外では continue ステートメントを使用できません")
                            }
                        }
                        is BreakStatement -> {
                            if (!blockScope.hasFlag(ScopeFlag.ITERATION)) {
                                throw ToverieCheckException("反復スコープの外では break ステートメントを使用できません")
                            }
                        }
                        is IfElseStatement -> {
                            if (statement.thenBlock.returns != null && statement.elseBlock?.returns != null) {
                                if (statement.thenBlock.returns == statement.elseBlock.returns) {
                                    returns = statement.thenBlock.returns
                                }
                                else {
                                    throw ToverieCheckException("if-else からの戻り値は型を一致させる必要があります")
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
            is UntypedForInStatementNode -> forInStatement(scope, node)
            is UntypedWhileStatementNode -> whileStatement(scope, node)
            is UntypedContinueStatementNode -> continueStatement(scope, node)
            is UntypedBreakStatementNode -> breakStatement(scope, node)
            else -> throw ToverieCheckException("未実装の文クラスです: " + node.javaClass.name)
        }
    }

    private fun varStatement(scope: CheckerScope, node: UntypedVarConstStatementNode): VariableStatement {
        val type = typeAnnotation(node.type)

        val (variable, value) = if (node.value == null) {
            CheckerVariable(type, initialAssigned = false) to null
        }
        else {
            val value = expression(scope, node.value, type)

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

    private fun forInStatement(scope: CheckerScope, node: UntypedForInStatementNode): ForInStatement {
        val collection = expression(scope, node.collection)

        if (collection.type.identifier != TypeIdentifier.ARRAY.value) {
            throw ToverieCheckException("for-in には配列を渡す必要があります")
        }

        val elementType = collection.type.parameters[0]
        val variable = CheckerVariable(elementType, initialAssigned = true)

        val block = block(scope, node.block) {
            addFlag(ScopeFlag.ITERATION)
            addVariable(node.identifier, variable)
            if (node.label != null) addLabel(node.label)
        }

        return ForInStatement(node.label, node.identifier, variable, collection, block)
    }

    private fun whileStatement(scope: CheckerScope, node: UntypedWhileStatementNode): WhileStatement {
        val condition = expression(scope, node.condition)

        if (condition.type != ToverieType.BOOL) {
            throw ToverieCheckException("while の条件式は bool でなければなりません")
        }

        val block = block(scope, node.block) {
            addFlag(ScopeFlag.ITERATION)
            if (node.label != null) addLabel(node.label)
        }

        return WhileStatement(
            node.label,
            condition,
            block
        )
    }

    private fun continueStatement(scope: CheckerScope, node: UntypedContinueStatementNode): ContinueStatement {
        if (node.labelName != null && !scope.hasLabel(node.labelName)) {
            throw ToverieCheckException("ラベル '${node.labelName}' は可視スコープに存在しません")
        }

        return ContinueStatement(node.labelName)
    }

    private fun breakStatement(scope: CheckerScope, node: UntypedBreakStatementNode): BreakStatement {
        if (node.labelName != null && !scope.hasLabel(node.labelName)) {
            throw ToverieCheckException("ラベル '${node.labelName}' は可視スコープに存在しません")
        }

        return BreakStatement(node.labelName)
    }

    private fun expression(scope: CheckerScope, node: UntypedExpressionNode, asserts: ToverieType? = null): ToverieExpression {
        val expr = when (node) {
            is UntypedLiteralValueExpressionNode<*> -> literalExpression(scope, node)
            is UntypedUnaryExpressionNode -> unaryExpression(scope, node)
            is UntypedBinaryExpressionNode -> binaryExpression(scope, node)
            is UntypedVariableReferenceExpressionNode -> variableReferenceExpression(scope, node)
            is UntypedFunctionCallExpressionNode -> functionCallExpression(scope, node)
            is UntypedFieldReferenceExpressionNode -> fieldReferenceExpression(scope, node)
            is UntypedArrayExpressionNode -> arrayExpression(scope, node, asserts)
            else -> throw ToverieCheckException("未実装の式ノードです: " + node.javaClass.name)
        }

        if (asserts != null && asserts != expr.type) {
            throw ToverieCheckException("式のアサーションに失敗しました: $asserts に対して ${expr.type} が渡されました")
        }

        return expr
    }

    private fun <T : Any> literalExpression(scope: CheckerScope, node: UntypedLiteralValueExpressionNode<T>): LiteralValueExpression<T> {
        return LiteralValueExpression(
            node.value,
            ToverieType(node.typeId.value, listOf())
        )
    }

    private fun arrayExpression(scope: CheckerScope, node: UntypedArrayExpressionNode, asserts: ToverieType?): ArrayExpression {
        val elements = node.elements.map {
            expression(scope, it)
        }

        if (elements.isEmpty()) {
            if (asserts?.identifier == TypeIdentifier.ARRAY.value) {
                return ArrayExpression(listOf(), asserts)
            }
            else {
                throw ToverieCheckException("配列のアサーションに失敗しました: $asserts")
            }
        }
        else {
            val elementType = elements[0].type
            val arrayType = ToverieType(TypeIdentifier.ARRAY.value, listOf(elementType))

            if (elements.all { it.type == elementType }) {
                if (asserts == null || asserts == arrayType) {
                    return ArrayExpression(elements, arrayType)
                }
                else {
                    throw ToverieCheckException("配列のアサーションに失敗しました: $asserts")
                }
            }
            else {
                throw ToverieCheckException("配列の全要素の型が一致していないことを検出しました")
            }
        }
    }

    private fun unaryExpression(scope: CheckerScope, node: UntypedUnaryExpressionNode): UnaryExpression {
        val target = expression(scope, node.target)

        val signature = OperatorReturnRegistry.UNARY.get(node.operation, listOf(target.type))

        if (node is UntypedIncrementDecrementExpressionNode) {
            if (target !is VariableReferenceExpression) {
                throw ToverieCheckException("インクリメント・デクリメントの対象となる式は変数参照でなければなりません")
            }

            return IncrementDecrementExpression(
                target,
                node.operation,
                node.mode,
                signature.returns
            )
        }

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

    private fun variableReferenceExpression(scope: CheckerScope, node: UntypedVariableReferenceExpressionNode): VariableReferenceExpression {
        val variable = scope.getVariable(node.identifier)

        return VariableReferenceExpression(
            node.identifier,
            variable
        )
    }

    private fun functionCallExpression(scope: CheckerScope, node: UntypedFunctionCallExpressionNode): FunctionCallExpression {
        val arguments = node.arguments.map {
            expression(scope, it)
        }

        val signature = scope.getSignature(node.identifier, arguments.map { it.type })

        return FunctionCallExpression(
            node.identifier,
            arguments,
            signature
        )
    }

    private fun fieldReferenceExpression(scope: CheckerScope, node: UntypedFieldReferenceExpressionNode): FieldReferenceExpression {
        val target = expression(scope, node.target)

        return FieldReferenceExpression(
            node.identifier,
            target,
            FieldReturnRegistry.get(target.type, node.identifier)
        )
    }

    /*private fun methodCallExpression(scope: CheckerScope, node: UntypedMethodCallExpressionNode): MethodCallExpression {
        val target = expression(scope, node.target)
        val arguments = node.arguments.map {
            expression(scope, it)
        }


    }*/
}

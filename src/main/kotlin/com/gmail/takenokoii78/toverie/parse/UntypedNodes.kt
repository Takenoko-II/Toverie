package com.gmail.takenokoii78.toverie.parse

abstract class UntypedNode

abstract class UntypedExpressionNode : UntypedNode()

abstract class UntypedStatementNode : UntypedNode()

open class UntypedTypeAnnotationNode(
    val identifier: String,
    val parameters: Array<UntypedTypeAnnotationNode> = arrayOf()
) : UntypedNode()

open class UntypedUnaryExpressionNode(
    val operation: UnaryOperation,
    val target: UntypedExpressionNode
) : UntypedExpressionNode()

enum class IncrementDecrementMode {
    PRE_OPERATION,
    POST_OPERATION
}

class UntypedIncrementDecrementExpressionNode(
    operation: UnaryOperation,
    val mode: IncrementDecrementMode,
    target: UntypedExpressionNode
) : UntypedUnaryExpressionNode(operation, target)

class UntypedBinaryExpressionNode(
    val operation: BinaryOperation,
    val leftTarget: UntypedExpressionNode,
    val rightTarget: UntypedExpressionNode
) : UntypedExpressionNode()

class UntypedTernaryExpressionNode(
    val condition: UntypedExpressionNode,
    val whenTrue: UntypedExpressionNode,
    val whenFalse: UntypedExpressionNode
) : UntypedExpressionNode()

class UntypedAssignmentExpressionNode(
    val operation: AssignmentOperation,
    val destination: UntypedExpressionNode,
    val value: UntypedExpressionNode
) : UntypedExpressionNode()

class UntypedCastExpressionNode(
    val target: UntypedExpressionNode,
    val castsTo: UntypedTypeAnnotationNode
) : UntypedExpressionNode()

class UntypedVariableReferenceExpressionNode(
    val identifier: String
) : UntypedExpressionNode()

class UntypedFieldReferenceExpressionNode(
    val target: UntypedExpressionNode,
    val identifier: String
) : UntypedExpressionNode()

class UntypedFunctionCallExpressionNode(
    val identifier: String,
    val arguments: List<UntypedExpressionNode>
) : UntypedExpressionNode()

class UntypedMethodCallExpressionNode(
    val target: UntypedExpressionNode,
    val identifier: String,
    val arguments: List<UntypedExpressionNode>
) : UntypedExpressionNode()

class UntypedLambdaCallExpressionNode(
    val target: UntypedExpressionNode,
    val arguments: List<UntypedExpressionNode>
) : UntypedExpressionNode()

class UntypedArrayExpressionNode(
    val elements: List<UntypedExpressionNode>
) : UntypedExpressionNode()

class UntypedSetExpressionNode(
    val valueType: UntypedTypeAnnotationNode?,
    val values: List<UntypedExpressionNode>
) : UntypedExpressionNode()

class UntypedMapExpressionNode(
    val keyType: UntypedTypeAnnotationNode?,
    val valueType: UntypedTypeAnnotationNode?,
    val entries: Map<UntypedExpressionNode, UntypedExpressionNode>
) : UntypedExpressionNode()

data class UntypedFunctionArgument(
    val name: String,
    val type: UntypedTypeAnnotationNode,
    val isVariadic: Boolean
)

class UntypedLiteralValueExpressionNode<T : Any>(val typeId: TypeIdentifier, val value: T) : UntypedExpressionNode()

class UntypedBlockNode(
    val nodes: List<UntypedNode>
) : UntypedNode()

class UntypedVarConstStatementNode(
    val type: UntypedTypeAnnotationNode,
    val identifier: String,
    val value: UntypedExpressionNode?
) : UntypedStatementNode()

class UntypedFuncStatementNode(
    val returns: UntypedTypeAnnotationNode,
    val identifier: String,
    val arguments: List<UntypedFunctionArgument>,
    val block: UntypedBlockNode
) : UntypedStatementNode()

class UntypedIfElseStatementNode(
    val condition: UntypedExpressionNode,
    val thenBlock: UntypedBlockNode,
    val elseBlock: UntypedBlockNode?
) : UntypedStatementNode()

class UntypedForStatementNode(
    val label: String?,
    val initialization: UntypedVarConstStatementNode,
    val condition: UntypedExpressionNode,
    val post: UntypedExpressionNode,
    val block: UntypedBlockNode
) : UntypedStatementNode()

class UntypedForInStatementNode(
    val label: String?,
    val identifier: String,
    val collection: UntypedExpressionNode,
    val block: UntypedBlockNode
) : UntypedStatementNode()

class UntypedWhileStatementNode(
    val label: String?,
    val condition: UntypedExpressionNode,
    val block: UntypedBlockNode
) : UntypedStatementNode()

class UntypedContinueStatementNode(
    val labelName: String?
) : UntypedStatementNode()

class UntypedBreakStatementNode(
    val labelName: String?
) : UntypedStatementNode()

class UntypedReturnStatementNode(
    val value: UntypedExpressionNode?
) : UntypedStatementNode()

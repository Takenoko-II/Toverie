package com.gmail.takenokoii78.toverie.parse

import com.gmail.takenokoii78.toverie.analyze.ExpressionSymbol
import com.gmail.takenokoii78.toverie.analyze.Token
import com.gmail.takenokoii78.toverie.analyze.TokenType

/**
 * 構文解析を行う
 */
class Parser(private val tokens: List<Token>) {
    private var index: Int = 0

    private fun isOver(): Boolean {
        return this.index >= this.tokens.size
    }

    private fun peek(): Token {
        if (this.isOver()) {
            throw ToverieParseException("Over at: index of $index")
        }

        return tokens[index]
    }

    private fun peekOffset(offset: Int): Token? {
        return tokens.getOrNull(index + offset)
    }

    private fun readWord(): String {
        val peek = peek()
        if (peek.type == TokenType.WORD) {
            next()
            return peek.value
        }
        else {
            throw ToverieParseException("Unexpected type token")
        }
    }

    private fun next() {
        if (this.isOver()) {
            throw ToverieParseException("Over")
        }

        index++
    }

    private fun expect(token: Token) {
        if (peek() == token) {
            next()
        }
        else {
            throw ToverieParseException("Unexpected token: ${peek()} (expected $token)")
        }
    }

    private fun nextIf(token: Token): Boolean {
        if (isOver()) return false
        else if (peek() == token) {
            next()
            return true
        }
        return false
    }

    private fun type(): UntypedTypeAnnotationNode {
        val word = readWord()

        val annotation = if (peek() == Token(ExpressionSymbol.LOGICAL_LESS)) {
            UntypedTypeAnnotationNode(word, typeParameters())
        }
        else {
            UntypedTypeAnnotationNode(word)
        }

        if (peekOffset(0) == Token(ExpressionSymbol.ARRAY_START) && peekOffset(1) == Token(ExpressionSymbol.ARRAY_END)) {
            next()
            next()
            return UntypedTypeAnnotationNode(TypeIdentifier.ARRAY.value, arrayOf(annotation))
        }

        return annotation
    }

    private fun typeParameters(limit: Int? = null): Array<UntypedTypeAnnotationNode> {
        expect(Token(ExpressionSymbol.LOGICAL_LESS))

        val parameters = mutableListOf<UntypedTypeAnnotationNode>()

        if (peek() == Token(ExpressionSymbol.LOGICAL_MORE)) {
            next()
            return arrayOf()
        }

        while (!isOver()) {
            parameters.add(type())

            if (peek() == Token(ExpressionSymbol.COMMA)) {
                next()
            }
            else if (peek() == Token(ExpressionSymbol.LOGICAL_MORE)) {
                break
            }
        }

        expect(Token(ExpressionSymbol.LOGICAL_MORE))

        if (limit != null) {
            if (parameters.size > limit) {
                throw ToverieParseException("型パラメータリストのサイズが想定をオーバーしています: ${parameters.size} > $limit")
            }
        }

        return parameters.toTypedArray()
    }

    private fun typeParameterDefs(): List<String> {
        expect(Token(ExpressionSymbol.LOGICAL_LESS))

        val parameters = mutableListOf<String>()

        while (!isOver()) {
            parameters.add(readWord())

            if (peek() == Token(ExpressionSymbol.COMMA)) {
                next()
            }
            else if (peek() == Token(ExpressionSymbol.LOGICAL_MORE)) {
                break
            }
        }

        expect(Token(ExpressionSymbol.LOGICAL_MORE))

        return parameters
    }

    private fun expression(): UntypedExpressionNode {
        return assignment()
    }

    private fun assignment(): UntypedExpressionNode {
        var node = ternary()

        if (isOver()) return node

        val message = "代入式の左辺は変数参照またはフィールド参照である必要があります"

        when (peek()) {
            Token(ExpressionSymbol.PLUS_ASSIGN) -> {
                next()
                if (node !is UntypedVariableReferenceExpressionNode && node !is UntypedFieldReferenceExpressionNode) throw ToverieParseException(message)
                node =  UntypedAssignmentExpressionNode(AssignmentOperation.PLUS_ASSIGN, node, assignment())
            }
            Token(ExpressionSymbol.MINUS_ASSIGN) -> {
                next()
                if (node !is UntypedVariableReferenceExpressionNode && node !is UntypedFieldReferenceExpressionNode) throw ToverieParseException(message)
                node = UntypedAssignmentExpressionNode(AssignmentOperation.MINUS_ASSIGN, node, assignment())
            }
            Token(ExpressionSymbol.MULTIPLY_ASSIGN) -> {
                next()
                if (node !is UntypedVariableReferenceExpressionNode && node !is UntypedFieldReferenceExpressionNode) throw ToverieParseException(message)
                node = UntypedAssignmentExpressionNode(AssignmentOperation.MULTIPLY_ASSIGN, node, assignment())
            }
            Token(ExpressionSymbol.DIVIDE_ASSIGN) -> {
                next()
                if (node !is UntypedVariableReferenceExpressionNode && node !is UntypedFieldReferenceExpressionNode) throw ToverieParseException(message)
                node = UntypedAssignmentExpressionNode(AssignmentOperation.DIVIDE_ASSIGN, node, assignment())
            }
            Token(ExpressionSymbol.MOD_ASSIGN) -> {
                next()
                if (node !is UntypedVariableReferenceExpressionNode && node !is UntypedFieldReferenceExpressionNode) throw ToverieParseException(message)
                node = UntypedAssignmentExpressionNode(AssignmentOperation.MOD_ASSIGN, node, assignment())
            }
            Token(ExpressionSymbol.POWER_ASSIGN) -> {
                next()
                if (node !is UntypedVariableReferenceExpressionNode && node !is UntypedFieldReferenceExpressionNode) throw ToverieParseException(message)
                node = UntypedAssignmentExpressionNode(AssignmentOperation.POWER_ASSIGN, node, assignment())
            }
            Token(ExpressionSymbol.BIT_AND_ASSIGN) -> {
                next()
                if (node !is UntypedVariableReferenceExpressionNode && node !is UntypedFieldReferenceExpressionNode) throw ToverieParseException(message)
                node = UntypedAssignmentExpressionNode(AssignmentOperation.BIT_AND_ASSIGN, node, assignment())
            }
            Token(ExpressionSymbol.BIT_OR_ASSIGN) -> {
                next()
                if (node !is UntypedVariableReferenceExpressionNode && node !is UntypedFieldReferenceExpressionNode) throw ToverieParseException(message)
                node = UntypedAssignmentExpressionNode(AssignmentOperation.BIT_OR_ASSIGN, node, assignment())
            }
            Token(ExpressionSymbol.BIT_XOR_ASSIGN) -> {
                next()
                if (node !is UntypedVariableReferenceExpressionNode && node !is UntypedFieldReferenceExpressionNode) throw ToverieParseException(message)
                node = UntypedAssignmentExpressionNode(AssignmentOperation.BIT_XOR_ASSIGN, node, assignment())
            }
            Token(ExpressionSymbol.BIT_SHIFT_LEFT_ASSIGN) -> {
                next()
                if (node !is UntypedVariableReferenceExpressionNode && node !is UntypedFieldReferenceExpressionNode) throw ToverieParseException(message)
                node = UntypedAssignmentExpressionNode(AssignmentOperation.BIT_SHIFT_LEFT_ASSIGN, node, assignment())
            }
            Token(ExpressionSymbol.BIT_SHIFT_RIGHT_ASSIGN) -> {
                next()
                if (node !is UntypedVariableReferenceExpressionNode && node !is UntypedFieldReferenceExpressionNode) throw ToverieParseException(message)
                node = UntypedAssignmentExpressionNode(AssignmentOperation.BIT_SHIFT_RIGHT_ASSIGN, node, assignment())
            }
            Token(ExpressionSymbol.BIT_SHIFT_RIGHT_UNSIGNED_ASSIGN) -> {
                next()
                if (node !is UntypedVariableReferenceExpressionNode && node !is UntypedFieldReferenceExpressionNode) throw ToverieParseException(message)
                node = UntypedAssignmentExpressionNode(AssignmentOperation.BIT_SHIFT_RIGHT_UNSIGNED_ASSIGN, node, assignment())
            }
            Token(ExpressionSymbol.ASSIGN) -> {
                next()
                if (node !is UntypedVariableReferenceExpressionNode && node !is UntypedFieldReferenceExpressionNode) throw ToverieParseException(message)
                node = UntypedAssignmentExpressionNode(AssignmentOperation.ASSIGN, node, assignment())
            }
        }

        return node
    }

    private fun ternary(): UntypedExpressionNode {
        val node = logicalOr()

        if (isOver()) return node

        if (peek() == Token(ExpressionSymbol.QUESTION)) {
            next()
            val trueExpr = expression()
            expect(Token(ExpressionSymbol.COLON))
            val falseExpr = ternary()

            return UntypedTernaryExpressionNode(node, trueExpr, falseExpr)
        }

        return node
    }

    private fun logicalOr(): UntypedExpressionNode {
        var node = logicalAnd()

        if (isOver()) return node

        while (peek() == Token(ExpressionSymbol.LOGICAL_OR)) {
            next()
            node =UntypedBinaryExpressionNode(BinaryOperation.LOGICAL_OR, node, logicalAnd())
        }

        return node
    }

    private fun logicalAnd(): UntypedExpressionNode {
        var node = comparison()

        if (isOver()) return node

        while (peek() == Token(ExpressionSymbol.LOGICAL_AND)) {
            next()
            node = UntypedBinaryExpressionNode(BinaryOperation.LOGICAL_AND, node, comparison())
        }

        return node
    }

    private fun comparison(): UntypedExpressionNode {
        var node = bitOr()

        while (!isOver()) {
            when (peek()) {
                Token(ExpressionSymbol.LOGICAL_EQUALS) -> {
                    next()
                    node = UntypedBinaryExpressionNode(BinaryOperation.LOGICAL_EQUALS, node, bitOr())
                }
                Token(ExpressionSymbol.LOGICAL_NOT_EQUALS) -> {
                    next()
                    node = UntypedBinaryExpressionNode(BinaryOperation.LOGICAL_NOT_EQUALS, node, bitOr())
                }
                Token(ExpressionSymbol.LOGICAL_LESS) -> {
                    next()
                    node = UntypedBinaryExpressionNode(BinaryOperation.LOGICAL_LESS, node, bitOr())
                }
                Token(ExpressionSymbol.LOGICAL_MORE) -> {
                    next()
                    node = UntypedBinaryExpressionNode(BinaryOperation.LOGICAL_MORE, node, bitOr())
                }
                Token(ExpressionSymbol.LOGICAL_LESS_OR_EQUALS) -> {
                    next()
                    node = UntypedBinaryExpressionNode(BinaryOperation.LOGICAL_LESS_OR_EQUALS, node, bitOr())
                }
                Token(ExpressionSymbol.LOGICAL_MORE_OR_EQUALS) -> {
                    next()
                    node = UntypedBinaryExpressionNode(BinaryOperation.LOGICAL_MORE_OR_EQUALS, node, bitOr())
                }
                else -> break
            }
        }

        return node
    }

    private fun bitOr(): UntypedExpressionNode {
        var node = bitXor()

        if (isOver()) return node

        while (peek() == Token(ExpressionSymbol.BIT_OR)) {
            next()
            node = UntypedBinaryExpressionNode(BinaryOperation.BIT_OR, node, bitXor())
        }

        return node
    }

    private fun bitXor(): UntypedExpressionNode {
        var node = bitAnd()

        if (isOver()) return node

        while (peek() == Token(ExpressionSymbol.BIT_XOR)) {
            next()
            node = UntypedBinaryExpressionNode(BinaryOperation.BIT_XOR, node, bitAnd())
        }

        return node
    }

    private fun bitAnd(): UntypedExpressionNode {
        var node = bitShift()

        if (isOver()) return node

        while (peek() == Token(ExpressionSymbol.BIT_AND)) {
            next()
            node = UntypedBinaryExpressionNode(BinaryOperation.BIT_AND, node, bitShift())
        }

        return node
    }

    private fun bitShift(): UntypedExpressionNode {
        var node = arithmetic()

        while (!isOver()) {
            if (peek() == Token(ExpressionSymbol.BIT_SHIFT_LEFT)) {
                next()
                node = UntypedBinaryExpressionNode(BinaryOperation.BIT_SHIFT_LEFT, node, arithmetic())
            }
            else if (peekOffset(0) == Token(ExpressionSymbol.LOGICAL_MORE)
                && peekOffset(1) == Token(ExpressionSymbol.LOGICAL_MORE)
                && peekOffset(2) == Token(ExpressionSymbol.LOGICAL_MORE)
            ) {
                next(); next(); next()
                node = UntypedBinaryExpressionNode(BinaryOperation.BIT_SHIFT_RIGHT_UNSIGNED, node, arithmetic())
            }
            else if (peekOffset(0) == Token(ExpressionSymbol.LOGICAL_MORE)
                && peekOffset(1) == Token(ExpressionSymbol.LOGICAL_MORE)
            ) {
                next(); next()
                node = UntypedBinaryExpressionNode(BinaryOperation.BIT_SHIFT_RIGHT, node, arithmetic())
            }
            else break
        }

        return node
    }

    private fun arithmetic(): UntypedExpressionNode {
        var node = term()

        while (!isOver()) {
            when (peek()) {
                Token(ExpressionSymbol.PLUS) -> {
                    next()
                    node = UntypedBinaryExpressionNode(BinaryOperation.PLUS, node, term())
                }
                Token(ExpressionSymbol.MINUS) -> {
                    next()
                    node = UntypedBinaryExpressionNode(BinaryOperation.MINUS, node, term())
                }
                else -> break
            }
        }

        return node
    }

    private fun term(): UntypedExpressionNode {
        var node = power()

        while (!isOver()) {
            when (peek()) {
                Token(ExpressionSymbol.MULTIPLY) -> {
                    next()
                    node = UntypedBinaryExpressionNode(BinaryOperation.MULTIPLY, node, power())
                }
                Token(ExpressionSymbol.DIVIDE) -> {
                    next()
                    node = UntypedBinaryExpressionNode(BinaryOperation.DIVIDE, node, power())
                }
                Token(ExpressionSymbol.MOD) -> {
                    next()
                    node = UntypedBinaryExpressionNode(BinaryOperation.MOD, node, power())
                }
                else -> break
            }
        }

        return node
    }

    private fun power(): UntypedExpressionNode {
        var node = cast()

        if (isOver()) return node

        while (peek() == Token(ExpressionSymbol.POWER)) {
            next()
            val right = cast()
            node = UntypedBinaryExpressionNode(BinaryOperation.POWER, node, right)
        }

        return node
    }

    private fun cast(): UntypedExpressionNode {
        var node = prefixUnary()

        while (!isOver()) {
            if (peek() == Token(Keyword.AS)) {
                next()
                node = UntypedCastExpressionNode(node, type())
            }
            else {
                break
            }
        }

        return node
    }

    private fun prefixUnary(): UntypedExpressionNode {
        return when (peek()) {
            Token(ExpressionSymbol.LOGICAL_NOT) -> {
                next()
                UntypedUnaryExpressionNode(UnaryOperation.LOGICAL_NOT, prefixUnary())
            }
            Token(ExpressionSymbol.BIT_NOT) -> {
                next()
                UntypedUnaryExpressionNode(UnaryOperation.BIT_INVERSE, prefixUnary())
            }
            Token(ExpressionSymbol.MINUS) -> {
                next()
                UntypedUnaryExpressionNode(UnaryOperation.MINUS, prefixUnary())
            }
            Token(ExpressionSymbol.INCREMENT) -> {
                next()
                val node = prefixUnary()
                if (node !is UntypedVariableReferenceExpressionNode && node !is UntypedFieldReferenceExpressionNode) {
                    throw ToverieParseException("インクリメントの対象となる式は変数参照またはフィールド参照である必要があります")
                }
                UntypedIncrementDecrementExpressionNode(UnaryOperation.INCREMENT, IncrementDecrementMode.PRE_OPERATION, node)
            }
            Token(ExpressionSymbol.DECREMENT) -> {
                next()
                val node = prefixUnary()
                if (node !is UntypedVariableReferenceExpressionNode && node !is UntypedFieldReferenceExpressionNode) {
                    throw ToverieParseException("デクリメントの対象となる式は変数参照またはフィールド参照である必要があります")
                }
                UntypedIncrementDecrementExpressionNode(UnaryOperation.DECREMENT, IncrementDecrementMode.PRE_OPERATION, node)
            }
            else -> suffixUnary()
        }
    }

    private fun suffixUnary(): UntypedExpressionNode {
        var node = access()

        if (isOver()) return node

        while (peek() == Token(ExpressionSymbol.INCREMENT) || peek() == Token(ExpressionSymbol.DECREMENT)) {
            if (node !is UntypedVariableReferenceExpressionNode && node !is UntypedFieldReferenceExpressionNode) {
                throw ToverieParseException("インクリメントの対象となる式は変数参照またはフィールド参照である必要があります")
            }

            val operation = if (peek() == Token(ExpressionSymbol.INCREMENT)) UnaryOperation.INCREMENT else UnaryOperation.DECREMENT
            next()

            node = UntypedIncrementDecrementExpressionNode(operation, IncrementDecrementMode.POST_OPERATION, node)
        }

        return node
    }

    private fun access(): UntypedExpressionNode {
        var node = factor()

        while (!isOver()) {
            if (peek() == Token(ExpressionSymbol.DOT)) {
                next()

                val word = readWord()

                node = if (!isOver() && peek() == Token(ExpressionSymbol.PARENTHESIS_START)) {
                    UntypedMethodCallExpressionNode(node, word, arguments())
                }
                else {
                    UntypedFieldReferenceExpressionNode(node, word)
                }
            }
            else if (peek() == Token(ExpressionSymbol.PARENTHESIS_START)) {
                node = UntypedLambdaCallExpressionNode(node, arguments())
            }
            else break
        }

        return node
    }

    private fun factor(): UntypedExpressionNode {
        when (peek().type) {
            TokenType.SYMBOL -> {
                if (peek() == Token(ExpressionSymbol.PARENTHESIS_START)) {
                    next()
                    val expr = expression()
                    expect(Token(ExpressionSymbol.PARENTHESIS_END))
                    return expr
                }
                else if (peek() == Token(ExpressionSymbol.ARRAY_START)) {
                    // 配列を予測
                    return list()
                }
                else {
                    throw ToverieParseException("factorとして予期しないトークンです: ${peek()} ${tokens.slice(index..<tokens.size)}")
                }
            }
            TokenType.DECIMAL -> {
                val token = peek()
                next()
                return UntypedLiteralValueExpressionNode(TypeIdentifier.DOUBLE, token.value.toDouble())
            }
            TokenType.BYTE -> {
                val token = peek()
                next()
                return UntypedLiteralValueExpressionNode(TypeIdentifier.BYTE, token.value.toByte())
            }
            TokenType.SHORT -> {
                val token = peek()
                next()
                return UntypedLiteralValueExpressionNode(TypeIdentifier.SHORT, token.value.toShort())
            }
            TokenType.INT -> {
                val token = peek()
                next()
                return UntypedLiteralValueExpressionNode(TypeIdentifier.INT, token.value.toInt())
            }
            TokenType.LONG -> {
                val token = peek()
                next()
                return UntypedLiteralValueExpressionNode(TypeIdentifier.LONG, token.value.toLong())
            }
            TokenType.FLOAT -> {
                val token = peek()
                next()
                return UntypedLiteralValueExpressionNode(TypeIdentifier.FLOAT, token.value.toFloat())
            }
            TokenType.DOUBLE -> {
                val token = peek()
                next()
                return UntypedLiteralValueExpressionNode(TypeIdentifier.DOUBLE, token.value.toDouble())
            }
            TokenType.CHARACTER -> {
                val token = peek()

                if (token.value.length != 1) {
                    throw ToverieParseException("文字値の解析に失敗しました: 文字列はダブルクォートのみ有効です")
                }

                next()
                return UntypedLiteralValueExpressionNode(TypeIdentifier.CHAR, token.value[0])
            }
            TokenType.STRING -> {
                val token = peek()
                next()
                return UntypedLiteralValueExpressionNode(TypeIdentifier.STRING, token.value)
            }
            TokenType.WORD -> {
                return when (val word = readWord()) {
                    Literal.NULL.value -> throw ToverieParseException("nullリテラルは禁止されています: optional<T> を使用してください")
                    Literal.FALSE.value -> UntypedLiteralValueExpressionNode(TypeIdentifier.BOOL, false)
                    Literal.TRUE.value -> UntypedLiteralValueExpressionNode(TypeIdentifier.BOOL, true)
                    TypeIdentifier.MAP.value -> map()
                    TypeIdentifier.SET.value -> set()
                    else -> {
                        if (peek() == Token(ExpressionSymbol.PARENTHESIS_START)) {
                            val args = arguments()
                            UntypedFunctionCallExpressionNode(word, args)
                        }
                        else {
                            UntypedVariableReferenceExpressionNode(word)
                        }
                    }
                }
            }
        }
    }

    private fun arguments(): List<UntypedExpressionNode> {
        expect(Token(ExpressionSymbol.PARENTHESIS_START))

        val args = mutableListOf<UntypedExpressionNode>()

        while (!isOver()) {
            if (peek() == Token(ExpressionSymbol.PARENTHESIS_END)) {
                next()
                return args
            }

            args.add(expression())

            if (peek() == Token(ExpressionSymbol.COMMA)) {
                next()
            }
            else if (peek() == Token(ExpressionSymbol.PARENTHESIS_END)) {
                next()
                return args
            }
            else throw ToverieParseException(peek().value)
        }

        throw ToverieParseException("関数呼び出しの引数の解析に失敗しました: 丸括弧が閉じられていません")
    }

    private fun list(): UntypedArrayExpressionNode {
        expect(Token(ExpressionSymbol.ARRAY_START))

        val elements = mutableListOf<UntypedExpressionNode>()

        if (peek() == Token(ExpressionSymbol.ARRAY_END)) {
            next()
            return UntypedArrayExpressionNode(elements)
        }
        else while (!isOver()) {
            elements.add(expression())

            if (peek() == Token(ExpressionSymbol.COMMA)) {
                next()
            }
            else if (peek() == Token(ExpressionSymbol.ARRAY_END)) {
                next()
                return UntypedArrayExpressionNode(elements)
            }
            else {
                throw ToverieParseException("リストリテラルの解析中に無効な文字を検出しました: ${peek()}")
            }
        }

        throw ToverieParseException("リストリテラルの解析に失敗しました: 角括弧が閉じられていません")
    }

    private fun map(): UntypedMapExpressionNode {
        val types = if (peek() == Token(ExpressionSymbol.LOGICAL_LESS)) {
            typeParameters(2).toList()
        }
        else {
            null
        }

        expect(Token(ExpressionSymbol.BRACE_START))

        val initialValues = mutableMapOf<UntypedExpressionNode, UntypedExpressionNode>()

        if (peek() == Token(ExpressionSymbol.BRACE_END)) {
            next()
            return UntypedMapExpressionNode(types?.get(0), types?.get(1), initialValues)
        }
        else while (!isOver()) {
            val k = expression()
            expect(Token(ExpressionSymbol.COLON))
            val v = expression()

            if (initialValues.contains(k)) {
                throw ToverieParseException("Mapリテラルの解析に失敗しました: キー '${k}' が重複しています")
            }

            initialValues[k] = v

            if (peek() == Token(ExpressionSymbol.COMMA)) {
                next()
            }
            else if (peek() == Token(ExpressionSymbol.BRACE_END)) {
                break
            }
        }

        expect(Token(ExpressionSymbol.BRACE_END))

        return UntypedMapExpressionNode(types?.get(0), types?.get(1), initialValues)
    }

    private fun set(): UntypedSetExpressionNode {
        val type = if (peek() == Token(ExpressionSymbol.LOGICAL_LESS)) {
            typeParameters(1)[0]
        } else null

        expect(Token(ExpressionSymbol.BRACE_START))

        val initialValues = mutableListOf<UntypedExpressionNode>()

        if (peek() == Token(ExpressionSymbol.BRACE_END)) {
            next()
            return UntypedSetExpressionNode(type, initialValues)
        }
        else while (!isOver()) {
            val e = expression()

            initialValues.add(e)

            if (peek() == Token(ExpressionSymbol.COMMA)) {
                next()
            }
            else if (peek() == Token(ExpressionSymbol.BRACE_END)) {
                break
            }
        }

        expect(Token(ExpressionSymbol.BRACE_END))

        return UntypedSetExpressionNode(type, initialValues)
    }

    private fun global(): UntypedBlockNode {
        val nodes = mutableListOf<UntypedStatementNode>()

        while (!isOver()) {
            if (peekOffset(0)?.type == TokenType.WORD && peekOffset(1)?.type == TokenType.WORD) {
                if (peekOffset(2) == Token(ExpressionSymbol.PARENTHESIS_START)) {
                    nodes.add(function())
                }
                else {
                    nodes.add(stateVariable())
                }
            }
            else {
                throw ToverieParseException("unknown global statement")
            }
        }

        return UntypedBlockNode(nodes)
    }

    private fun block(): UntypedBlockNode {
        expect(Token(ExpressionSymbol.BRACE_START))

        val nodes = mutableListOf<UntypedNode>()

        while (!isOver()) {
            if (peek() == Token(ExpressionSymbol.BRACE_END)) {
                next()
                break
            }
            else {
                val node = statement()

                if (node == null) {
                    next()
                    continue
                }

                nodes.add(node)
            }
        }

        return UntypedBlockNode(nodes)
    }

    private fun statement(): UntypedNode? {
        return when (peek()) {
            Token(Keyword.IF) -> ifElse()
            Token(Keyword.FOR) -> forLoop()
            Token(Keyword.WHILE) -> whileLoop()
            Token(Keyword.CONTINUE) -> labelledContinue()
            Token(Keyword.BREAK) -> labelledBreak()
            Token(Keyword.RETURN) -> returnStatement()
            Token(TypeIdentifier.BOOL),
            Token(TypeIdentifier.BYTE),
            Token(TypeIdentifier.SHORT),
            Token(TypeIdentifier.INT),
            Token(TypeIdentifier.LONG),
            Token(TypeIdentifier.CHAR),
            Token(TypeIdentifier.FLOAT),
            Token(TypeIdentifier.DOUBLE),
            Token(TypeIdentifier.STRING),
            Token(TypeIdentifier.VOID) -> stateVariable()
            Token(ExpressionSymbol.SEMICOLON) -> null
            else -> {
                val expr = expression()
                nextIf(Token(ExpressionSymbol.SEMICOLON))
                expr
            }
        }
    }

    private fun stateVariable(): UntypedVarConstStatementNode {
        val type = type()
        val identifier = readWord()
        val value = if (peek() == Token(ExpressionSymbol.ASSIGN)) {
            next()
            expression()
        }
        else null

        nextIf(Token(ExpressionSymbol.SEMICOLON))

        return UntypedVarConstStatementNode(type, identifier, value)
    }

    private fun functionArgumentDefs(): List<UntypedFunctionArgument> {
        // 丸括弧を要求
        expect(Token(ExpressionSymbol.PARENTHESIS_START))

        // 引数を読む
        val args = mutableListOf<UntypedFunctionArgument>()

        if (peek() == Token(ExpressionSymbol.PARENTHESIS_END)) {
            // 括弧が直後に閉じられた場合 -> 引数無し
            next()
        }
        else while (!isOver()) {
            var isVariadic = false

            if (
                peek() == Token(ExpressionSymbol.DOT)
                && peekOffset(1) == Token(ExpressionSymbol.DOT)
                && peekOffset(2) == Token(ExpressionSymbol.DOT)
            ) {
                next(); next(); next()
                isVariadic = true
            }

            // 引数名
            val name = readWord()

            // 仮引数に型注釈は必須
            expect(Token(ExpressionSymbol.COLON))
            val type = type()

            // リストに追加
            args.add(UntypedFunctionArgument(name, type, isVariadic))

            if (peek() == Token(ExpressionSymbol.COMMA)) {
                // コンマが続けば引き続き読み取り
                next()

                if (isVariadic) {
                    throw ToverieParseException("可変長引数の後に引数を続けることはできません")
                }
            }
            else if (peek() == Token(ExpressionSymbol.PARENTHESIS_END)) {
                // 丸括弧が閉じられたら
                next()
                break
            }
            else throw ToverieParseException("関数定義のヘッダの解析に失敗しました: 引数リストが閉じられていません")
        }

        if (isOver()) {
            throw ToverieParseException("関数定義のヘッダの解析に失敗しました: 引数リストが閉じられていません")
        }

        return args
    }

    private fun function(): UntypedFuncStatementNode {
        val ret = type()
        val identifier = readWord()
        val args = functionArgumentDefs()

        return UntypedFuncStatementNode(
            ret,
            identifier,
            args,
            block(),
        )
    }

    private fun ifElse(): UntypedIfElseStatementNode {
        expect(Token(Keyword.IF))

        val hasParenthesis = nextIf(Token(ExpressionSymbol.PARENTHESIS_START))
        val condition = expression()
        if (hasParenthesis) expect(Token(ExpressionSymbol.PARENTHESIS_END))

        val thenBlock = if (peek() == Token(ExpressionSymbol.BRACE_START)) block() else UntypedBlockNode(listOfNotNull(statement()))

        val elseBlock = if (isOver()) null else if (peek() == Token(Keyword.ELSE)) {
            next()
            if (peek() == Token(ExpressionSymbol.BRACE_START)) block()
            else UntypedBlockNode(listOfNotNull(statement()))
        }
        else null

        return UntypedIfElseStatementNode(condition, thenBlock, elseBlock)
    }

    private fun forLoop(): UntypedStatementNode {
        expect(Token(Keyword.FOR))
        val hasParenthesis = nextIf(Token(ExpressionSymbol.PARENTHESIS_START))

        if (peek() == Token(Keyword.VAR) || peek() == Token(Keyword.CONST)) {
            val initialization = stateVariable()
            nextIf(Token(ExpressionSymbol.SEMICOLON))
            val condition = expression()
            nextIf(Token(ExpressionSymbol.SEMICOLON))
            val post = expression()
            nextIf(Token(ExpressionSymbol.SEMICOLON))

            if (hasParenthesis) expect(Token(ExpressionSymbol.PARENTHESIS_END))

            val block = if (peek() == Token(ExpressionSymbol.BRACE_START)) block() else UntypedBlockNode(listOfNotNull(statement()))

            return UntypedForStatementNode(initialization, condition, post, block)
        }
        else if (peek().type == TokenType.WORD) {
            val identifier = readWord()
            expect(Token(Keyword.IN))
            val iterable = expression()

            if (hasParenthesis) expect(Token(ExpressionSymbol.PARENTHESIS_END))

            val block = if (peek() == Token(ExpressionSymbol.BRACE_START)) block() else UntypedBlockNode(listOfNotNull(statement()))

            return UntypedForInStatementNode(identifier, iterable, block)
        }
        else {
            throw ToverieParseException("for文の解析に失敗しました: 無効な文字列 (${peek()}) を検出しました")
        }
    }

    private fun whileLoop(): UntypedWhileStatementNode {
        expect(Token(Keyword.WHILE))
        val hasParenthesis = nextIf(Token(ExpressionSymbol.PARENTHESIS_START))
        val condition = expression()
        if (hasParenthesis) expect(Token(ExpressionSymbol.PARENTHESIS_END))

        val block = if (peek() == Token(ExpressionSymbol.BRACE_START)) block() else UntypedBlockNode(listOfNotNull(statement()))

        return UntypedWhileStatementNode(condition, block)
    }

    private fun labelledContinue(): UntypedContinueStatementNode {
        expect(Token(Keyword.CONTINUE))
        val label = readWord()
        nextIf(Token(ExpressionSymbol.SEMICOLON))
        return UntypedContinueStatementNode(label)
    }

    private fun labelledBreak(): UntypedBreakStatementNode {
        expect(Token(Keyword.BREAK))
        val label = readWord()
        nextIf(Token(ExpressionSymbol.SEMICOLON))
        return UntypedBreakStatementNode(label)
    }

    private fun returnStatement(): UntypedReturnStatementNode {
        expect(Token(Keyword.RETURN))

        if (peek() == Token(ExpressionSymbol.SEMICOLON)) {
            next()
            return UntypedReturnStatementNode(null)
        }
        else {
            val expr = expression()
            nextIf(Token(ExpressionSymbol.SEMICOLON))
            return UntypedReturnStatementNode(expr)
        }
    }

    fun parse(): UntypedBlockNode {
        try {
            return global()
        }
        finally {
            index = 0
        }
    }
}

package com.gmail.takenokoii78.toverie.analyze

/**
 * 字句解析を行う
 */
class LexicalAnalyzer(private val text: String) {
    private var location: Int = 0

    private fun isOver(): Boolean {
        return location >= text.length
    }

    private fun peek(): Char {
        if (isOver()) {
            throw ToverieAnalyzeException("字句解析中に文字列外に到達しました")
        }

        return text[location]
    }

    private fun next() {
        if (isOver()) {
            throw ToverieAnalyzeException("字句解析中に文字列外に到達しました")
        }

        location++
    }

    private fun nextIf(symbol: ExpressionSymbol): Boolean {
        val sliced = text.substring(location)

        if (sliced.startsWith(symbol.value)) {
            location += symbol.value.length
            return true
        }

        return false
    }

    private fun testAnyOf(vararg chars: Char): Boolean {
        return chars.any { peek() == it }
    }

    private fun whitespace() {
        while (!isOver()) {
            if (!nextIf(ExpressionSymbol.WHITESPACE)) break
        }
    }

    private fun readInt(): String {
        var str = ""

        while (!isOver()) {
            if (testAnyOf(*NUMBER_CHARS.toCharArray())) {
                str += peek()
                next()
            }
            else break
        }

        return str
    }

    private fun readNumber(): Token {
        val intPart: String = readInt()
        val str: String
        val type: TokenType

        if (isOver()) {
            return Token(TokenType.INT, intPart)
        }

        if (nextIf(ExpressionSymbol.DOT)) {
            if (NUMBER_CHARS.contains(peek())) {
                val decPart: String = readInt()
                str = intPart + ExpressionSymbol.DOT.value + decPart

                if (peek().toString().lowercase() == NumberSuffix.FLOAT.value) {
                    next()
                    type = TokenType.FLOAT
                }
                else if (peek().toString().lowercase() == NumberSuffix.DOUBLE.value) {
                    next()
                    type = TokenType.DOUBLE
                }
                else if (peek().isLetter()) {
                    throw ToverieAnalyzeException("整数の読み取り中に無効な文字を検出しました: ${peek()}")
                }
                else {
                    type = TokenType.DECIMAL
                }
            }
            else {
                // プロパティアクセス用
                str = intPart
                type = TokenType.INT
            }
        }
        else {
            str = intPart

            if (peek().toString().lowercase() == NumberSuffix.BYTE.value) {
                next()
                type = TokenType.BYTE
            }
            else if (peek().toString().lowercase() == NumberSuffix.SHORT.value) {
                next()
                type = TokenType.SHORT
            }
            else if (peek().toString().lowercase() == NumberSuffix.LONG.value) {
                next()
                type = TokenType.LONG
            }
            else if (peek().toString().lowercase() == NumberSuffix.FLOAT.value) {
                next()
                type = TokenType.FLOAT
            }
            else if (peek().toString().lowercase() == NumberSuffix.DOUBLE.value) {
                next()
                type = TokenType.DOUBLE
            }
            else if (peek().isLetter()) {
                throw ToverieAnalyzeException("整数の読み取り中に無効な文字を検出しました: ${peek()}")
            }
            else {
                type = TokenType.INT
            }
        }

        if (!isOver()) if (peek().isLetterOrDigit()) {
            throw ToverieAnalyzeException("整数の読み取り中に無効な文字を検出しました: ${peek()}")
        }

        return Token(type, str)
    }

    private fun readQuoted(): Token {
        if (!testAnyOf(ExpressionSymbol.SINGLE_QUOTE.value[0], ExpressionSymbol.DOUBLE_QUOTE.value[0]) || isOver()) {
            throw ToverieAnalyzeException("aaa")
        }

        val quote = peek()
        next()

        if (testAnyOf(quote)) {
            next()
            return Token(TokenType.STRING, "")
        }

        var str = ""

        var endedCorrectly = false

        while (!isOver()) {
            if (testAnyOf(quote)) {
                // クォートだったら解析終了
                endedCorrectly = true
                next()
                break
            }
            else if (testAnyOf('\n', '\r')) {
                // 制御文字は無視！
                next()
            }
            else if (nextIf(ExpressionSymbol.ESCAPE)) {
                // エスケープだったら次の文字を今回のループにおける文字とする
                var c = peek()

                if (c == 'n') {
                    // これは流石に改行
                    c = '\n'
                }
                else if (c == 'r') {
                    // これは流石にキャリッジリターン
                    c = '\r'
                }

                str += c
                next()
            }
            else {
                // それ以外なら普通に連結
                str += peek()
                next()
            }
        }

        if (!endedCorrectly) {
            throw ToverieAnalyzeException(str)
        }

        if (!isOver()) {
            if (peek().isLetterOrDigit()) {
                throw ToverieAnalyzeException("文字または文字列リテラルの終了後に無効な文字を検出しました: ${peek()}")
            }
        }

        if (quote.toString() == ExpressionSymbol.SINGLE_QUOTE.value) {
            if (str.length != 1) {
                throw ToverieAnalyzeException("文字リテラルは一文字である必要があります: $str")
            }

            return Token(TokenType.CHARACTER, str)
        }

        return Token(TokenType.STRING, str)
    }

    private fun comment() {
        if (nextIf(ExpressionSymbol.COMMENT)) {
            while (!isOver()) {
                if (peek().toString() == ExpressionSymbol.LINEBREAK.value) {
                    break
                }
                else next()
            }
        }
        else if (nextIf(ExpressionSymbol.COMMENT_START)) {
            var count = 1
            while (!isOver()) {
                if (nextIf(ExpressionSymbol.COMMENT_END)) {
                    count--
                }
                else if (nextIf(ExpressionSymbol.COMMENT_START)) {
                    count++
                }
                else next()

                if (count == 0) break
            }
        }
        else if (nextIf(ExpressionSymbol.HASH)) {
            while (!isOver()) {
                if (peek().toString() == ExpressionSymbol.LINEBREAK.value) {
                    break
                }
                next()
            }
        }
    }

    private fun token(): Token? {
        whitespace()

        if (isOver()) {
            return null
        }

        comment()

        if (isOver()) {
            return null
        }

        if (testAnyOf(ExpressionSymbol.SINGLE_QUOTE.value[0], ExpressionSymbol.DOUBLE_QUOTE.value[0])) {
            return readQuoted()
        }

        val sortedSymbols = ExpressionSymbol.entries
            .sortedWith { a, b -> b.value.length - a.value.length }

        for (symbol in sortedSymbols) {
            if (nextIf(symbol)) {
                return Token(TokenType.SYMBOL, symbol.value)
            }
        }

        if (testAnyOf(*NUMBER_CHARS.toCharArray())) {
            return readNumber()
        }

        var word = ""

        while (!isOver()) {
            if (ExpressionSymbol.entries.toList().any { it.value == peek().toString() }) {
                return Token(TokenType.WORD, word)
            }

            word += peek()
            next()
        }

        return if (word.isEmpty()) null else Token(TokenType.WORD, word)
    }

    fun analyze(): List<Token> {
        try {
            val tokens: MutableList<Token> = mutableListOf()

            while (!isOver()) {
                val token = token()
                if (token === null) break
                tokens.add(token)
            }

            return tokens.filter { it.value != ExpressionSymbol.CARRIAGE_RETURN.value && it.value != ExpressionSymbol.LINEBREAK.value }
        }
        finally {
            location = 0
        }
    }
}

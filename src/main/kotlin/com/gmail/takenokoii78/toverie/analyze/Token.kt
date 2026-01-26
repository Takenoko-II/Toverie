package com.gmail.takenokoii78.toverie.analyze

import com.gmail.takenokoii78.toverie.parse.TypeIdentifier
import com.gmail.takenokoii78.toverie.parse.Keyword

data class Token(val type: TokenType, val value: String) {
    constructor(symbol: ExpressionSymbol) : this(TokenType.SYMBOL, symbol.value)

    constructor(keyword: Keyword) : this(TokenType.WORD, keyword.value)

    constructor(identifier: TypeIdentifier) : this(TokenType.WORD, identifier.value)
}

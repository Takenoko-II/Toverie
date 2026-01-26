package com.gmail.takenokoii78.toverie.parse

enum class UnaryOperation(val symbol: String) {
    LOGICAL_NOT("!"),
    BIT_INVERSE("~"),
    MINUS("-"),
    INCREMENT("++"),
    DECREMENT("--")
}

enum class BinaryOperation(val symbol: String) {
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    MOD("%"),
    POWER("**"),
    BIT_AND("&"),
    BIT_OR("|"),
    BIT_XOR("^"),
    BIT_SHIFT_LEFT("<<"),
    BIT_SHIFT_RIGHT(">>"),
    BIT_SHIFT_RIGHT_UNSIGNED(">>>"),
    LOGICAL_AND("&&"),
    LOGICAL_OR("||"),
    LOGICAL_MORE(">"),
    LOGICAL_LESS("<"),
    LOGICAL_MORE_OR_EQUALS(">="),
    LOGICAL_LESS_OR_EQUALS("<="),
    LOGICAL_EQUALS("=="),
    LOGICAL_NOT_EQUALS("!=")
}

enum class AssignmentOperation(val symbol: String, val origin: BinaryOperation?) {
    ASSIGN("=", null),
    PLUS_ASSIGN("+=", BinaryOperation.PLUS),
    MINUS_ASSIGN("-=", BinaryOperation.MINUS),
    MULTIPLY_ASSIGN("*=", BinaryOperation.MULTIPLY),
    DIVIDE_ASSIGN("/=", BinaryOperation.DIVIDE),
    MOD_ASSIGN("%=", BinaryOperation.MOD),
    POWER_ASSIGN("**=", BinaryOperation.POWER),
    BIT_AND_ASSIGN("&=", BinaryOperation.BIT_AND),
    BIT_OR_ASSIGN("|=", BinaryOperation.BIT_OR),
    BIT_XOR_ASSIGN("^=", BinaryOperation.BIT_XOR),
    BIT_SHIFT_LEFT_ASSIGN("<<=", BinaryOperation.BIT_SHIFT_LEFT),
    BIT_SHIFT_RIGHT_ASSIGN(">>=", BinaryOperation.BIT_SHIFT_RIGHT),
    BIT_SHIFT_RIGHT_UNSIGNED_ASSIGN(">>>=", BinaryOperation.BIT_SHIFT_RIGHT_UNSIGNED)
}

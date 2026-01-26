package com.gmail.takenokoii78.toverie.parse

enum class Literal(val value: String) {
    TRUE("true"),
    FALSE("false"),
    NULL("null")
}

enum class TypeIdentifier(val value: String) {
    BOOL("bool"),
    BYTE("byte"),
    SHORT("short"),
    INT("int"),
    LONG("long"),
    FLOAT("float"),
    DOUBLE("double"),
    CHAR("char"),
    STR("str"),
    ARRAY("array"),
    LIST("list"),
    MAP("map"),
    SET("set"),
    VOID("void")
}

enum class Keyword(val value: String) {
    IF("if"),
    ELSE("else"),
    VAR("var"),
    CONST("const"),
    FUNC("func"),
    FOR("for"),
    WHILE("while"),
    CONTINUE("continue"),
    BREAK("break"),
    RETURN("return"),
    MATCH("match"),
    AS("as"),
    IN("in"),
    FALL("fall"),
    BUILTIN("builtin")
}

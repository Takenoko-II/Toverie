package com.gmail.takenokoii78.toverie;

import com.gmail.takenokoii78.json.JSONFile
import com.gmail.takenokoii78.toverie.analyze.LexicalAnalyzer
import com.gmail.takenokoii78.toverie.parse.Parser
import com.gmail.takenokoii78.toverie.parse.UntypedNode

fun main() {
    println("Hello, World!")

    val file = JSONFile("src/test/resources/out.json")

    if (!file.exists()) file.create()

    val a = Parser(LexicalAnalyzer("""
    int main() {
        # comment
        c("data modify entity @s a");
        int x = 1 + 2 * (3 - 4);
        int[] a = [x, 1, 2, 3];
        println("Hello, World!");
    }
    """.trimIndent()).analyze()).parse()

    val reflector = JSONReflector(UntypedNode::class.java)

    file.write(reflector.reflect(a))
}

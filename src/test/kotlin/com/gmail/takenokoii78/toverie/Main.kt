package com.gmail.takenokoii78.toverie;

import com.gmail.takenokoii78.json.JSONFile
import com.gmail.takenokoii78.toverie.analyze.LexicalAnalyzer
import com.gmail.takenokoii78.toverie.check.ToverieNode
import com.gmail.takenokoii78.toverie.check.TypeChecker
import com.gmail.takenokoii78.toverie.parse.Parser

fun main() {
    println("Hello, World!")

    val file = JSONFile("src/test/resources/out.json")

    if (!file.exists()) file.create()

    val a = TypeChecker(Parser(LexicalAnalyzer("""
    void command(s: string) {
        
    }
    
    void println(s: string) {
    
    }

    int main() {
        # comment
        
        command("data modify entity @s a");

        int x = 1 + 2 * (3 - 4);

        println("Hello, World!");
        
        "aiueo".length + x;

        for:a i in [1, 2, 3] {
            i++;
            continue a;
        }

        return 1;
    }
    """).analyze()).parse()).check()

    val reflector = JSONReflector(ToverieNode::class.java)

    file.write(reflector.reflect(a))
}

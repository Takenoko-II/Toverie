package com.gmail.takenokoii78.toverie.transpile

import com.gmail.takenokoii78.toverie.check.FunctionStatement
import com.gmail.takenokoii78.toverie.check.ToverieBlock
import com.gmail.takenokoii78.toverie.check.ToverieExpression

class Transpiler(private val block: ToverieBlock) {
    fun transpile() {
        for (node in block.nodes) {
            when (node) {
                is FunctionStatement -> {
                    function(node)
                }
            }
        }
    }

    private fun function(node: FunctionStatement) {

    }

    private fun block(block: ToverieBlock) {
        for (node in block.nodes) {
            when (node) {
                is ToverieExpression -> {

                }
            }
        }
    }
}

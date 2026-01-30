package com.gmail.takenokoii78.toverie.check

class CheckerScope private constructor(private val parent: CheckerScope?, private val flags: MutableSet<ScopeFlag>) {
    private val variables = mutableMapOf<String, CheckerVariable>()

    private val assigned = mutableSetOf<String>()

    private val functions = CheckerFunctions()

    private val labels = mutableSetOf<String>()

    constructor(parent: CheckerScope, vararg flags: ScopeFlag): this(parent, flags.toMutableSet())

    constructor(vararg flags: ScopeFlag): this(null, flags.toMutableSet())

    fun hasFlag(flag: ScopeFlag): Boolean {
        return flags.contains(flag) || (parent?.hasFlag(flag) ?: false)
    }

    fun addFlag(flag: ScopeFlag) {
        flags.add(flag)
    }

    fun hasVariable(identifier: String): Boolean {
        return variables.contains(identifier) || (parent?.hasVariable(identifier) ?: false)
    }

    fun addVariable(identifier: String, variable: CheckerVariable) {
        if (variables.contains(identifier)) {
            throw ToverieCheckException("変数 '$identifier' が同一スコープ内で重複しています")
        }
        else {
            variables[identifier] = variable
        }
    }

    fun getVariable(identifier: String): CheckerVariable {
        return variables[identifier] ?: parent?.getVariable(identifier) ?: throw ToverieCheckException("変数 '$identifier' がスコープに見つかりません")
    }

    fun isAssignedVariable(identifier: String): Boolean {
        return assigned.contains(identifier) || (parent?.isAssignedVariable(identifier) ?: false)
    }

    fun assignVariable(identifier: String) {
        assigned.add(identifier)
    }

    fun hasSignature(identifier: String, arguments: List<ToverieType>): Boolean {
        return if (functions.has(identifier)) {
            functions.get(identifier).lookup(arguments) != null
        }
        else {
            parent?.hasSignature(identifier, arguments) ?: false
        }
    }

    fun getSignature(identifier: String, arguments: List<ToverieType>): CheckerSignature {
        return if (functions.has(identifier)) {
            functions.get(identifier).lookup(arguments) ?: throw ToverieCheckException("関数シグネチャ '$identifier(${arguments.joinToString(", ")})' は存在しません")
        }
        else {
            parent?.getSignature(identifier, arguments) ?: throw ToverieCheckException("関数シグネチャ '$identifier(${arguments.joinToString(", ")})' は存在しません")
        }
    }

    fun addSignature(identifier: String, signature: CheckerSignature) {
        functions.register(identifier, signature)
    }

    fun hasLabel(name: String): Boolean {
        return if (labels.contains(name)) true else parent?.hasLabel(name) ?: false
    }

    fun addLabel(name: String) {
        if (hasLabel(name)) {
            throw ToverieCheckException("ラベル '$name' は既に可視スコープに存在します")
        }

        labels.add(name)
    }
}

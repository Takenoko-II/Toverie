package com.gmail.takenokoii78.toverie.check

import com.gmail.takenokoii78.toverie.parse.TypeIdentifier

class FieldReturnRegistry(private val typeId: TypeIdentifier, initializer: FieldReturnRegistry.() -> Unit) {
    private val map = mutableMapOf<String, (ToverieType) -> ToverieType>()

    init {
        initializer()
    }

    fun register(name: String, t: (ToverieType) -> ToverieType) {
        map[name] = t
    }

    fun get(name: String): (ToverieType) -> ToverieType {
        return map[name] ?: throw ToverieCheckException("フィールド '$name' が見つかりません")
    }

    companion object {
        private val map = mutableMapOf<String, FieldReturnRegistry>()

        private fun get(typeId: String): FieldReturnRegistry {
            return map[typeId] ?: throw ToverieCheckException("フィールドレジストリ '$typeId' にアクセスできません")
        }

        private fun register(typeId: TypeIdentifier, i: FieldReturnRegistry.() -> Unit) {
            val r = FieldReturnRegistry(typeId, i)
            map[typeId.value] = r
        }

        fun get(type: ToverieType, name: String): ToverieType {
            return get(type.identifier).get(name)(type)
        }

        val STRING = register(TypeIdentifier.STRING) {
            register("length") { ToverieType.INT }
        }

        val ARRAY = register(TypeIdentifier.ARRAY) {
            register("length") { ToverieType.INT }
        }
    }
}

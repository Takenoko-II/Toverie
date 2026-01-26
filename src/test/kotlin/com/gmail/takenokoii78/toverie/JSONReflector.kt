package com.gmail.takenokoii78.toverie

import com.gmail.takenokoii78.json.JSONValue
import com.gmail.takenokoii78.json.values.JSONArray
import com.gmail.takenokoii78.json.values.JSONNull
import com.gmail.takenokoii78.json.values.JSONObject
import com.gmail.takenokoii78.json.values.JSONString
import java.lang.reflect.Field

class JSONReflector(private vararg val targets: Class<*>) {
    private fun fields(clazz: Class<*>): Set<Field> {
        return if (clazz == Any::class.java) {
            setOf()
        }
        else {
            clazz.declaredFields.toSet() + fields(clazz.superclass)
        }
    }

    private fun jsonValue(obj: Any): JSONValue<*> {
        return when (obj) {
            is Enum<*> -> JSONString.valueOf(obj.name)
            is Boolean, is Number, is String -> JSONValue.valueOf(obj)
            is Iterable<*> -> JSONArray.valueOf(obj.map { extract(it) })
            is Array<*> -> JSONArray.valueOf(obj.map { extract(it) })
            is Map<*, *> -> {
                JSONObject.valueOf(obj.mapKeys { it.toString() }.mapValues { extract(it.value) })
            }
            is JSONValue<*> -> obj
            else -> {
                if (targets.any { target -> target.isInstance(obj) }) extract(obj)
                else JSONString.valueOf(obj.toString())
            }
        }
    }

    private fun jsonObject(obj: Any): JSONObject {
        val map = mutableMapOf<String, JSONValue<*>>(
            "CLASS_NAME" to JSONString.valueOf(obj.javaClass.simpleName)
        )

        for (field in fields(obj.javaClass)) {
            if (field.trySetAccessible()) {
                val value = field.get(obj)

                map[field.name] = if (value == null) {
                    JSONNull.NULL
                }
                else {
                    jsonValue(value)
                }
            }
            else {
                throw IllegalStateException("JSON REFLECT FAILURE")
            }
        }

        return JSONObject.valueOf(map)
    }

    private fun extract(obj: Any?): JSONValue<*> {
        return if (obj == null) {
            JSONNull.NULL
        }
        else {
            jsonObject(obj)
        }
    }

    fun reflect(value: Any?): JSONObject {
        val obj = extract(value)

        if (obj is JSONObject) return obj
        else throw IllegalArgumentException("不正な出力を得ました")
    }
}

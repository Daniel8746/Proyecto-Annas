package com.annas.data.js

object JsEngine {
    fun <T> render(template: String, vars: Map<String, T>): String {
        var result = template

        vars.forEach { (key, value) ->
            result = result.replace("__${key}__",  value.toString())
        }

        return result
    }
}
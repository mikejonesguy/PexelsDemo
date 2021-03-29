@file:Suppress("MemberVisibilityCanBePrivate")

package com.mike.pexelsdemo.helper

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import retrofit2.converter.gson.GsonConverterFactory

object GsonHelper {
    val builder = GsonBuilder()
    val gson: Gson get() = builder.create()
    val converterFactory: GsonConverterFactory = GsonConverterFactory.create(gson)

    fun toJson(data: Any): String {
        return gson.toJson(data)
    }

    fun <T> parseJson(json: String, classOfT: Class<T>): T {
        return gson.fromJson(json, classOfT)
    }

    fun <T> tryParseJson(json: String?, classOfT: Class<T>): T? {
        json ?: return null
        return try {
            parseJson(json, classOfT)
        } catch (e: JsonSyntaxException) {
            null
        }
    }
}
package io.kuberig.dsl.vanilla.plugin

import com.fasterxml.jackson.core.JsonProcessingException
import com.mashape.unirest.http.ObjectMapper
import com.mashape.unirest.http.Unirest
import java.io.IOException

fun configureUnirest() {
    Unirest.setObjectMapper(object : ObjectMapper {
        private val objectMapper = com.fasterxml.jackson.databind.ObjectMapper()

        init {
            objectMapper.findAndRegisterModules()
        }

        override fun <T> readValue(value: String, valueType: Class<T>): T {
            return try {
                objectMapper.readValue(value, valueType)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        override fun writeValue(value: Any): String {
            return try {
                objectMapper.writeValueAsString(value)
            } catch (e: JsonProcessingException) {
                throw RuntimeException(e)
            }
        }
    })
}
package eu.siacs.conversations.volley.parser

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json

object VolleyParser {
    fun <T> String.parser(deserializer: DeserializationStrategy<T>):T{
        val jsonParser = Json(from = Json){
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }
        val serializerBean = jsonParser.decodeFromString(deserializer, this)

        return serializerBean
    }
}
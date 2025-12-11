package com.tagmypet.data.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson

// Anotação personalizada para usar no Model
@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class PetOwnerId

class PetOwnerAdapter {

    @FromJson
    @PetOwnerId
    fun fromJson(reader: JsonReader): String {
        // Verifica o tipo do próximo token no JSON
        return when (reader.peek()) {
            JsonReader.Token.STRING -> {
                // Se for string (ex: "65a9..."), retorna direto
                reader.nextString()
            }

            JsonReader.Token.BEGIN_OBJECT -> {
                // Se for objeto (populado ou Mongo Extended JSON), tenta extrair o ID
                var id = ""
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "_id" -> id =
                            reader.nextString() // Populated object (ex: { "_id": "ID", "name": "..." })
                        else -> reader.skipValue() // Ignora outros campos (name, photoUrl, etc)
                    }
                }
                reader.endObject()
                id
            }

            JsonReader.Token.NULL -> {
                reader.nextNull<Any>()
                ""
            }

            else -> {
                reader.skipValue()
                ""
            }
        }
    }

    @ToJson
    fun toJson(@PetOwnerId id: String): String {
        return id
    }
}
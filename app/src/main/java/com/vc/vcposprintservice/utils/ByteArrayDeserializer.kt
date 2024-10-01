package com.vc.vcposprintservice.utils

import android.util.Base64
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class ByteArrayDeserializer : JsonDeserializer<ByteArray> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ByteArray {
        val base64String = json.asString
        return Base64.decode(base64String, Base64.DEFAULT)
    }

}
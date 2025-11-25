package com.example.flybook.data.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class RouteDeserializer : JsonDeserializer<Route> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Route {
        if (json == null || !json.isJsonObject) {
            throw IllegalStateException("Expected JSON object but got: ${json?.javaClass?.simpleName}")
        }
        val jsonObject = json.asJsonObject
        
        val duocPheDuyet: Boolean? = when {
            jsonObject.has("duoc_phe_duyet") -> {
                val element = jsonObject.get("duoc_phe_duyet")
                when {
                    element.isJsonNull -> null
                    element.asJsonPrimitive.isBoolean -> element.asBoolean
                    element.asJsonPrimitive.isNumber -> element.asInt != 0
                    else -> null
                }
            }
            else -> null
        }
        
        // Handle san_bay_di - could be object or null
        val sanBayDi: Airport? = try {
            val element = jsonObject.get("san_bay_di")
            if (element.isJsonNull || !element.isJsonObject) {
                null
            } else {
                context?.deserialize(element, Airport::class.java)
            }
        } catch (e: Exception) {
            null
        }
        
        // Handle san_bay_den - could be object or null
        val sanBayDen: Airport? = try {
            val element = jsonObject.get("san_bay_den")
            if (element.isJsonNull || !element.isJsonObject) {
                null
            } else {
                context?.deserialize(element, Airport::class.java)
            }
        } catch (e: Exception) {
            null
        }
        
        val id = try {
            jsonObject.get("id").asInt
        } catch (e: Exception) {
            0
        }
        
        val khoangCach = try {
            if (jsonObject.has("khoang_cach") && !jsonObject.get("khoang_cach").isJsonNull) {
                jsonObject.get("khoang_cach").asDouble
            } else null
        } catch (e: Exception) {
            null
        }
        
        val thoiGianBay = try {
            if (jsonObject.has("thoi_gian_bay") && !jsonObject.get("thoi_gian_bay").isJsonNull) {
                jsonObject.get("thoi_gian_bay").asInt
            } else null
        } catch (e: Exception) {
            null
        }
        
        return Route(
            id = id,
            san_bay_di = sanBayDi,
            san_bay_den = sanBayDen,
            khoang_cach = khoangCach,
            thoi_gian_bay = thoiGianBay,
            duoc_phe_duyet = duocPheDuyet
        )
    }
}


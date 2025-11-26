package com.example.flybook.data.models

import android.util.Log
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
            Log.w("RouteDeserializer", "Expected JSON object but got: ${json?.javaClass?.simpleName}")
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
            if (!jsonObject.has("san_bay_di")) {
                Log.w("RouteDeserializer", "Route missing san_bay_di field")
                null
            } else {
                val element = jsonObject.get("san_bay_di")
                if (element.isJsonNull) {
                    Log.w("RouteDeserializer", "Route san_bay_di is null")
                    null
                } else if (!element.isJsonObject) {
                    Log.w("RouteDeserializer", "Route san_bay_di is not an object: ${element.javaClass.simpleName}")
                    null
                } else {
                    val airport = context?.deserialize<Airport>(element, Airport::class.java)
                    Log.d("RouteDeserializer", "Parsed san_bay_di: ${airport?.ma_san_bay} - ${airport?.ten_san_bay}")
                    airport
                }
            }
        } catch (e: Exception) {
            Log.e("RouteDeserializer", "Error parsing san_bay_di: ${e.message}", e)
            null
        }
        
        // Handle san_bay_den - could be object or null
        val sanBayDen: Airport? = try {
            if (!jsonObject.has("san_bay_den")) {
                Log.w("RouteDeserializer", "Route missing san_bay_den field")
                null
            } else {
                val element = jsonObject.get("san_bay_den")
                if (element.isJsonNull) {
                    Log.w("RouteDeserializer", "Route san_bay_den is null")
                    null
                } else if (!element.isJsonObject) {
                    Log.w("RouteDeserializer", "Route san_bay_den is not an object: ${element.javaClass.simpleName}")
                    null
                } else {
                    val airport = context?.deserialize<Airport>(element, Airport::class.java)
                    Log.d("RouteDeserializer", "Parsed san_bay_den: ${airport?.ma_san_bay} - ${airport?.ten_san_bay}")
                    airport
                }
            }
        } catch (e: Exception) {
            Log.e("RouteDeserializer", "Error parsing san_bay_den: ${e.message}", e)
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


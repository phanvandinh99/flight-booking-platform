package com.example.flybook.data.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import java.lang.reflect.Type

class AirlineAircraftDeserializer : JsonDeserializer<AirlineAircraft> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): AirlineAircraft {
        if (json == null || !json.isJsonObject) {
            throw JsonSyntaxException("Expected JSON object but got: ${json?.javaClass?.simpleName}")
        }
        val jsonObject = json.asJsonObject

        val id = try {
            jsonObject.get("id").asInt
        } catch (e: Exception) {
            0
        }

        val maHangHangKhong = try {
            if (jsonObject.has("ma_hang_hang_khong") && !jsonObject.get("ma_hang_hang_khong").isJsonNull) {
                jsonObject.get("ma_hang_hang_khong").asInt
            } else null
        } catch (e: Exception) {
            null
        }

        val loaiMayBay = try {
            jsonObject.get("loai_may_bay").asString
        } catch (e: Exception) {
            ""
        }

        val tongSoGhe = try {
            jsonObject.get("tong_so_ghe").asInt
        } catch (e: Exception) {
            0
        }

        // Handle so_do_ghe - could be string (JSON string) or object or null
        val soDoGhe: Map<String, Any>? = try {
            val element = jsonObject.get("so_do_ghe")
            if (element == null || element.isJsonNull) {
                null
            } else if (element.isJsonPrimitive && element.asJsonPrimitive.isString) {
                // If it's a string, try to parse it as JSON
                val jsonString = element.asString
                if (jsonString.isBlank()) {
                    null
                } else {
                    try {
                        val parsed = com.google.gson.JsonParser.parseString(jsonString)
                        if (parsed.isJsonObject) {
                            // Convert JsonObject to Map<String, Any>
                            convertJsonObjectToMap(parsed.asJsonObject)
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
            } else if (element.isJsonObject) {
                // If it's already an object, convert it to Map
                convertJsonObjectToMap(element.asJsonObject)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }

        // Handle hang_hang_khong - could be object or null
        val hangHangKhong: Airline? = try {
            val element = jsonObject.get("hang_hang_khong")
            if (element == null || element.isJsonNull || !element.isJsonObject) {
                null
            } else {
                context?.deserialize(element, Airline::class.java)
            }
        } catch (e: Exception) {
            null
        }

        return AirlineAircraft(
            id = id,
            ma_hang_hang_khong = maHangHangKhong,
            loai_may_bay = loaiMayBay,
            tong_so_ghe = tongSoGhe,
            so_do_ghe = soDoGhe,
            hang_hang_khong = hangHangKhong
        )
    }

    private fun convertJsonObjectToMap(jsonObject: com.google.gson.JsonObject): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        jsonObject.entrySet().forEach { entry ->
            val key = entry.key
            val value = entry.value
            when {
                value.isJsonNull -> map[key] = ""
                value.isJsonPrimitive -> {
                    val primitive = value.asJsonPrimitive
                    when {
                        primitive.isBoolean -> map[key] = primitive.asBoolean
                        primitive.isNumber -> {
                            val num = primitive.asNumber
                            // Try to keep as Int if possible, otherwise Double
                            if (num.toDouble() == num.toInt().toDouble()) {
                                map[key] = num.toInt()
                            } else {
                                map[key] = num.toDouble()
                            }
                        }
                        primitive.isString -> map[key] = primitive.asString
                        else -> map[key] = primitive.toString()
                    }
                }
                value.isJsonArray -> {
                    // Convert array to List
                    val list = mutableListOf<Any>()
                    value.asJsonArray.forEach { item ->
                        when {
                            item.isJsonPrimitive -> {
                                val prim = item.asJsonPrimitive
                                when {
                                    prim.isBoolean -> list.add(prim.asBoolean)
                                    prim.isNumber -> {
                                        val num = prim.asNumber
                                        if (num.toDouble() == num.toInt().toDouble()) {
                                            list.add(num.toInt())
                                        } else {
                                            list.add(num.toDouble())
                                        }
                                    }
                                    prim.isString -> list.add(prim.asString)
                                    else -> list.add(prim.toString())
                                }
                            }
                            item.isJsonObject -> list.add(convertJsonObjectToMap(item.asJsonObject))
                            else -> list.add(item.toString())
                        }
                    }
                    map[key] = list
                }
                value.isJsonObject -> map[key] = convertJsonObjectToMap(value.asJsonObject)
                else -> map[key] = value.toString()
            }
        }
        return map
    }
}


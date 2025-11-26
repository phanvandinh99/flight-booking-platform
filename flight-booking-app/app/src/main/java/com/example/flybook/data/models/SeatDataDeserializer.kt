package com.example.flybook.data.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class SeatDataDeserializer : JsonDeserializer<SeatData> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): SeatData {
        if (json == null || !json.isJsonObject) {
            throw IllegalStateException("Expected JSON object for SeatData")
        }
        
        val jsonObject = json.asJsonObject
        
        val maChuyenBay = jsonObject.get("ma_chuyen_bay")?.asInt ?: 0
        val tongSoGhe = jsonObject.get("tong_so_ghe")?.asInt ?: 0
        
        // Parse ghe_da_dat
        val gheDaDat = mutableListOf<String>()
        val gheDaDatElement = jsonObject.get("ghe_da_dat")
        if (gheDaDatElement != null && gheDaDatElement.isJsonArray) {
            gheDaDatElement.asJsonArray.forEach { element ->
                if (element.isJsonPrimitive) {
                    gheDaDat.add(element.asString)
                }
            }
        }
        
        // Parse ghe_giu_cho
        val gheGiuCho = mutableListOf<String>()
        val gheGiuChoElement = jsonObject.get("ghe_giu_cho")
        if (gheGiuChoElement != null && gheGiuChoElement.isJsonArray) {
            gheGiuChoElement.asJsonArray.forEach { element ->
                if (element.isJsonPrimitive) {
                    gheGiuCho.add(element.asString)
                }
            }
        }
        
        // Parse so_do_ghe - could be array or object/map
        val soDoGhe: List<SeatLayout>? = try {
            val soDoGheElement = jsonObject.get("so_do_ghe")
            if (soDoGheElement == null || soDoGheElement.isJsonNull) {
                null
            } else if (soDoGheElement.isJsonArray) {
                // If it's an array, parse directly
                soDoGheElement.asJsonArray.mapNotNull { element ->
                    if (element.isJsonObject) {
                        val seatObj = element.asJsonObject
                        SeatLayout(
                            number = seatObj.get("number")?.asString ?: "",
                            row = seatObj.get("row")?.asInt ?: 0,
                            letter = seatObj.get("letter")?.asString ?: ""
                        )
                    } else null
                }
            } else if (soDoGheElement.isJsonObject) {
                // If it's an object/map, convert to list
                // This handles the case where so_do_ghe is a map like { "1A": {...}, "1B": {...} }
                val seatMap = soDoGheElement.asJsonObject
                seatMap.entrySet().mapNotNull { entry ->
                    val seatObj = entry.value
                    if (seatObj.isJsonObject) {
                        val seat = seatObj.asJsonObject
                        SeatLayout(
                            number = entry.key, // Use key as number
                            row = seat.get("row")?.asInt ?: entry.key.filter { it.isDigit() }.toIntOrNull() ?: 0,
                            letter = seat.get("letter")?.asString ?: entry.key.filter { it.isLetter() }
                        )
                    } else null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("SeatDataDeserializer", "Error parsing so_do_ghe: ${e.message}", e)
            null
        }
        
        // Parse gia_ve
        val giaVe = mutableListOf<FareClassPrice>()
        val giaVeElement = jsonObject.get("gia_ve")
        if (giaVeElement != null && giaVeElement.isJsonArray) {
            giaVeElement.asJsonArray.forEach { element ->
                if (element.isJsonObject) {
                    val priceObj = element.asJsonObject
                    giaVe.add(
                        FareClassPrice(
                            hang_ve = priceObj.get("hang_ve")?.asString ?: "",
                            gia = priceObj.get("gia")?.asDouble ?: 0.0
                        )
                    )
                }
            }
        }
        
        return SeatData(
            ma_chuyen_bay = maChuyenBay,
            so_do_ghe = soDoGhe,
            tong_so_ghe = tongSoGhe,
            ghe_da_dat = gheDaDat,
            ghe_giu_cho = gheGiuCho,
            gia_ve = giaVe
        )
    }
}


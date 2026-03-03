package cat.copernic.aguamap1.data

import android.content.Context
import android.widget.Toast
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import java.util.Date

class DatabasePopulator {

    companion object {
        fun importTerrassaFountains(context: Context) {
            val db = FirebaseFirestore.getInstance()

            try {
                // Leer el archivo json desde res/raw/fuentes.json
                val inputStream = context.resources.openRawResource(R.raw.fuentes)
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                val response = Gson().fromJson(jsonString, OverpassResponse::class.java)

                var batch = db.batch()
                var count = 0

                response.elements.forEach { el ->
                    val tags = el.tags
                    if (tags != null) {

                        val latitude = el.getFinalLat()
                        val longitude = el.getFinalLon()

                        if (latitude != 0.0 && longitude != 0.0) {
                            // Usamos el ID de OSM para evitar duplicados si le das al Play varias veces
                            val docRef = db.collection("fountains").document("osm_${el.id}")
                            val hash = GeoFireUtils.getGeoHashForLocation(GeoLocation(latitude, longitude))

                            val esPotable = tags.get("amenity") == "drinking_water" ||
                                    tags.get("drinking_water") == "yes"

                            // Extraer el nombre real o poner uno genérico
                            val nombreReal = tags.get("name") ?: if (esPotable) "Font Potable" else "Font Ornamental"

                            val categoriaAsignada = if (esPotable) {
                                Category(
                                    id = "7QzfLNg2YQb0cOyWBjvX",
                                    name = "Potable",
                                    description = "Agua potable",
                                    imageUrl = ""
                                )
                            } else {
                                Category(
                                    id = "cEDF91SDc2CinWoKWTwp",
                                    name = "Ornamental",
                                    description = "Fuentes ornamentales",
                                    imageUrl = ""
                                )
                            }

                            val fountain = Fountain(
                                id = docRef.id,
                                name = nombreReal,
                                latitude = latitude,
                                longitude = longitude,
                                geohash = hash,
                                operational = true,
                                category = categoriaAsignada,
                                description = "Fuente de Terrassa importada de OpenStreetMap (OSM).",
                                dateCreated = Date(),
                                status = StateFountain.ACCEPTED,
                                createdBy = "ADMIN",
                                ratingAverage = 0.0,
                                totalRatings = 0,
                                positiveVotes = 0,
                                negativeVotes = 0,
                                votedByPositive = emptyList(),
                                votedByNegative = emptyList()
                            )

                            batch.set(docRef, fountain)
                            count++

                            // Límite de 500 por lote de Firestore
                            if (count % 500 == 0) {
                                batch.commit()
                                batch = db.batch()
                            }
                        }
                    }
                }

                batch.commit().addOnSuccessListener {
                    Toast.makeText(context, "IMPORTACIÓN EXITOSA: $count fuentes añadidas.", Toast.LENGTH_LONG).show()
                }.addOnFailureListener { e ->
                    Toast.makeText(context, "Error al subir: ${e.message}", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

// --- CLASES DE APOYO (Aquí estaba el error, faltaba el campo 'id') ---

data class OverpassResponse(val elements: List<OverpassElement>)

data class OverpassElement(
    val type: String,
    val id: Long, // <--- CAMBIO: Añadido el ID para que no de error
    val lat: Double?,
    val lon: Double?,
    val center: Center?,
    val tags: Map<String, String>?
) {
    fun getFinalLat(): Double = lat ?: center?.lat ?: 0.0
    fun getFinalLon(): Double = lon ?: center?.lon ?: 0.0
}

data class Center(val lat: Double, val lon: Double)
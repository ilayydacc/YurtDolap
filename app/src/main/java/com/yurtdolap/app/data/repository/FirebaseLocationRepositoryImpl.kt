package com.yurtdolap.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.yurtdolap.app.domain.model.City
import com.yurtdolap.app.domain.model.Dormitory
import com.yurtdolap.app.domain.repository.LocationRepository
import com.yurtdolap.app.domain.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class FirebaseLocationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : LocationRepository {

    override fun getCities(): Flow<Resource<List<City>>> = callbackFlow {
        trySend(Resource.Loading())

        val subscription = firestore.collection("cities")
            .orderBy("order", Query.Direction.ASCENDING)
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Şehirler yüklenirken hata oluştu."))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val cities = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(City::class.java)?.copy(id = doc.id)
                    }
                    trySend(Resource.Success(cities))
                }
            }

        awaitClose { subscription.remove() }
    }

    override fun getDormitoriesByCity(cityName: String): Flow<Resource<List<Dormitory>>> = callbackFlow {
        trySend(Resource.Loading())

        val subscription = firestore.collection("dormitories")
            .whereEqualTo("cityName", cityName)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Yurtlar yüklenirken hata oluştu."))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val dormitories = snapshot.documents
                        .mapNotNull { doc -> doc.toObject(Dormitory::class.java)?.copy(id = doc.id) }
                        .sortedBy { it.name }
                    trySend(Resource.Success(dormitories))
                }
            }

        awaitClose { subscription.remove() }
    }
}

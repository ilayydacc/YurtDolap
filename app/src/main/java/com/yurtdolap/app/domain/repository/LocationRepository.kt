package com.yurtdolap.app.domain.repository

import com.yurtdolap.app.domain.model.City
import com.yurtdolap.app.domain.model.Dormitory
import com.yurtdolap.app.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getCities(): Flow<Resource<List<City>>>
    fun getDormitoriesByCity(cityName: String): Flow<Resource<List<Dormitory>>>
}

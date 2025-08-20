package com.example.stokkontrolveyonetimsistemi.domain.usecase.location

import android.util.Log
import com.example.stokkontrolveyonetimsistemi.data.model.location.*
import com.example.stokkontrolveyonetimsistemi.data.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetLocationHierarchyUseCase(
    private val locationRepository: LocationRepository
) {

    companion object {
        private const val TAG = "GetLocationHierarchyUseCase"
    }

    suspend fun getAllBolge(): Flow<LocationResult<List<BolgeDto>>> {
        Log.d(TAG, "Loading all regions")
        return locationRepository.getAllBolge()
    }

    suspend fun getIllerByBolge(bolgeId: Long): Flow<LocationResult<List<IlDto>>> {
        Log.d(TAG, "Loading provinces for region: $bolgeId")

        if (bolgeId <= 0) {
            return flow {
                emit(LocationResult.Error("Geçersiz bölge ID"))
            }
        }

        return locationRepository.getIllerByBolge(bolgeId)
    }

    suspend fun getIlcelerByIl(ilId: Long): Flow<LocationResult<List<IlceDto>>> {
        Log.d(TAG, "Loading districts for province: $ilId")

        if (ilId <= 0) {
            return flow {
                emit(LocationResult.Error("Geçersiz il ID"))
            }
        }

        return locationRepository.getIlcelerByIl(ilId)
    }

    suspend fun getDepolarByIlce(ilceId: Long): Flow<LocationResult<List<DepoDto>>> {
        Log.d(TAG, "Loading warehouses for district: $ilceId")

        if (ilceId <= 0) {
            return flow {
                emit(LocationResult.Error("Geçersiz ilçe ID"))
            }
        }

        return locationRepository.getDepolarByIlce(ilceId)
    }
}
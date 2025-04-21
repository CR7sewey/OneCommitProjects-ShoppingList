package com.mike.shoppinglist

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mike.shoppinglist.api.GeoCodingRepository
import com.mike.shoppinglist.api.GeoCodingService
import com.mike.shoppinglist.api.Retrofit
import kotlinx.coroutines.launch

class MVVM_Location: ViewModel() {

    // ViewModel logic for location updates
    // This is where you would handle the location updates and permissions
    // For example, you could use LiveData to observe location changes
    // and update the UI accordingly

    private val GeoRepository: GeoCodingRepository
        get() {
            return GeoCodingRepository(Retrofit.retrofit.create(GeoCodingService::class.java))
        }

    private val _locationUpdates =
        mutableStateOf<Location?>(null)// MutableLiveData<Location?> by lazy {
       // MutableLiveData<Location?>(null)
    //}
    var locationUpdates: State<Location?> = _locationUpdates
        //< LiveData<Location?>
       // get() = _locationUpdates

    fun updateLocationWithouAPI(location: Location) {
        // Update the location in the ViewModel
        // This will notify observers of the change
        // and update the UI accordingly
        _locationUpdates.value = location
    }


    fun updateLocation(location: Location) {
        // Update the location in the ViewModel
        // This will notify observers of the change
        // and update the UI accordingly
        viewModelScope.launch {
            val repoResponse = GeoRepository.getLocationName(
                "${location.latitude},${location.longitude}",
                "" // API KEY...
            )
            if (repoResponse.isSuccess) {
                Log.d("MVVM_Location", "getLocationName: ${repoResponse.getOrNull()}")
                val address = try {
                    repoResponse.getOrNull()?.results?.firstOrNull()?.formatted_address
                        ?: "Unknown address"
                } catch (e: Exception) {
                    Log.e("MVVM_Location", "Error parsing address: ${e.message}")
                    location.address
                }
                _locationUpdates.value = location.copy(
                    address = address
                )
            } else {
                _locationUpdates.value = location.copy(
                    address = location.address
                )
            }
        }
    }



}

data class Location
    (
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)
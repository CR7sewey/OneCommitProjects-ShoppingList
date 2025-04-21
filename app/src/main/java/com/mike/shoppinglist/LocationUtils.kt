package com.mike.shoppinglist

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.text.intl.Locale
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlin.let

class LocationUtils(val context: Context) {
    private val _fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    fun getLocationPermission(context: Context): Boolean {
        // Check if the location permission is granted
        val permission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        return permission // true if granted, false otherwise
    }

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(locationViewModel: MVVM_Location) {
        // Request location updates
        val locationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                // Handle location updates here
                //val location = locationResult.lastLocation
                //Log.d("Location", "${location?.latitude}")
                val location = locationViewModel.locationUpdates.value
                location?.let { it ->
                    // If location is not null, invoke the success callback with latitude and longitude
                    var loc = Location(
                        latitude = it.latitude,
                        longitude = it.longitude,
                    ).let { i ->
                        i.copy(
                            address = reverseGeocodeLocation(i)
                        )
                    }
                    locationViewModel.updateLocation(
                        loc
                    ) // update the location in the view model
                    // Reverse geocode the location to get the address

                }
            }

        }
        // Start location updates
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        ).build()

        _fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

    }

    fun reverseGeocodeLocation(location: Location): String? {
        // Implement reverse geocoding logic here
        // This is a placeholder implementation
        // In a real application, you would use a geocoding library or API to get the address
        val geocoder = Geocoder(context, java.util.Locale.getDefault())
        val coordinate = LatLng(location.latitude, location.longitude)
            val address: MutableList<Address>? = geocoder.getFromLocation(
                coordinate.latitude,
                coordinate.longitude,
                1
            ) ?: mutableListOf()
        address.let { addresses ->
           return if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                // Update the location with the address
                "${address.locality}, ${address.countryName}"
            }
            else {
                // Handle the case where no address is found
                Log.d("Location", "No address found for the location")
                null
           }
        }

    }

}
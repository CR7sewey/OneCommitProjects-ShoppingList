package com.mike.shoppinglist

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat

@Composable
fun ValidationPermissionLocation(
    location: Location,
    onLocationSelected: (Location) -> Unit,
    locationViewModel: MVVM_Location, locationUtils: LocationUtils, context: Context, locationPermissionLauncher:  ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>, modifier: Modifier = Modifier

) {



        // 3. Invoke the appropriate callback based on the permission result
        /*if (arePermissionsGranted) {
            onPermissionGranted.invoke()
        } else {
            onPermissionDenied.invoke()
        }*/




                // Handle button click
                if (locationUtils.getLocationPermission(context)) { // if permissions and location already granted
                    // Permission granted, proceed with location updates
                    // Use the location updates as needed
                    //locationUtils.getLocationUpdates(locationViewModel) // get the location updates
                    Log.d("Location 2", "${locationViewModel.locationUpdates.value?.latitude}")
                    LocationSelectionScreen(
                        location = location,
                        onLocationSelected = onLocationSelected,
                    )

                } else {
                    // Permission not granted, request permission
                    // You can use ActivityCompat.requestPermissions() here
                    locationPermissionLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }







}

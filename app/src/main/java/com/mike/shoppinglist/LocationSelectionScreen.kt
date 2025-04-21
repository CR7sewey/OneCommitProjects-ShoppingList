package com.mike.shoppinglist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState


@Composable
fun LocationSelectionScreen(
    location: Location,
    onLocationSelected: (Location) -> Unit,
    navHostController: NavHostController? = null,
    modifier: Modifier = Modifier) {

    var userLocation by remember { mutableStateOf(
        LatLng(
            location.latitude,
            location.longitude
        )
    ) } // phone location

    var cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            userLocation, 10f
        )
    } // zoom in / out


    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        GoogleMap(
            modifier = modifier
                .weight(1f)
                .padding(8.dp),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                userLocation = latLng
            }
        ) {
            // Add markers or other map elements here if needed
            // For example, you can add a marker at the user's location
            Marker(
                state = MarkerState(position = userLocation),
                title = "Your Location",
                snippet = "This is your current location"
            )
        }

        var newLocation: Location
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            onClick = {
                newLocation = Location(
                    userLocation.latitude,
                    userLocation.longitude
                )
                onLocationSelected(
                    newLocation
                )
                navHostController?.popBackStack()

            }
        ) {
            Text(text = "Select Location")
        }
    }
    
}
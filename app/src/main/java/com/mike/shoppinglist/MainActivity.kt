package com.mike.shoppinglist

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mike.shoppinglist.ui.theme.ShoppingListTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val locationViewModel by viewModels<MVVM_Location>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val locationUtils = LocationUtils(this)
        setContent {
            ShoppingListTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppRoutes(
                        locationViewModel = locationViewModel,
                        locationUtils = locationUtils,
                        context = this,
                        modifier = Modifier.padding(innerPadding))

                }
            }
        }
    }
}

@Composable
fun AppRoutes(locationViewModel: MVVM_Location, locationUtils: LocationUtils, context: Context, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val graph = navController.createGraph(
        startDestination = "shoppingcart"
    ) {
        composable("shoppingcart") { ShoppingCart(
            locationViewModel = locationViewModel,
            locationUtils = locationUtils,
            context = context,
            navController = navController,
        ) }
        // Add other destinations here
        dialog("locationscreen") { backStackEntry ->
            LocationSelectionScreen(
                location = locationViewModel.locationUpdates.value ?: Location(0.0, 0.0),
                onLocationSelected = { location ->

                    var loc = location.copy(
                        address = locationUtils.reverseGeocodeLocation(location)
                    )
                    locationViewModel.updateLocation(loc)
                    navController.popBackStack()
                },
                navHostController = navController
            )
        }
    }

    NavHost(navController = navController, graph = graph, modifier = modifier)



}

@Composable
fun ShoppingCart(locationViewModel: MVVM_Location, locationUtils: LocationUtils, context: Context, navController: NavHostController, modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(false) }
    var showLoc by remember { mutableStateOf(false) }
    var listOfItems by remember { mutableStateOf<List<Item>>(
        listOf(
            Item("1","Item 1", "1", false),
            Item("2","Item 2", "2", false),
        ))
    } //emptyList<Item>().toMutableList()) }
    var index by remember { mutableStateOf(0) }
    var updateScreen by remember { mutableStateOf(false) }
    Log.d("ShoppingCart", "listOfItems: $listOfItems")

    val locationPermissionLauncher = rememberLauncherForActivityResult( // request to start an activity for a result!
        contract = ActivityResultContracts.RequestMultiplePermissions() // contract to request multiple permissions
    ) { permissionsMap -> // return a map of the location permissions requests
        // Check if all requested permissions are granted
        /*val arePermissionsGranted = permissionsMap.values.reduce { acc, next ->
            acc && next
        }*/
        //Initialize it where you need it

        if (permissionsMap[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
            && permissionsMap[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {

            // Permission granted, proceed with location updates
            //locationUtils.getLocationUpdates(locationViewModel) // get the location updates


        } else { // ask for permission
            val rationaleRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                context as MainActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (rationaleRequired) { // user dont allow
                Toast.makeText(
                    context,
                    "Location Permission is required for this feature to work",
                    Toast.LENGTH_LONG
                ).show()
            } else { // app not allowed in settings
                Toast.makeText(
                    context,
                    "You need to go to the settings to enable location permission",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    Column(modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
        ) {
        when (showLoc) {
            true -> ValidationPermissionLocation(
                location = locationViewModel.locationUpdates.value ?: Location(0.0,0.0),
                onLocationSelected = { location ->
                    listOfItems = listOfItems.mapIndexed { i, item ->
                        if (i == index) {
                            var loc = location.copy(
                                address = item.location.address ?: locationUtils.reverseGeocodeLocation(location)
                            )
                            item.copy(location = loc)
                        } else {
                            item
                        }
                    }
                    showLoc = false
                },
                locationViewModel = locationViewModel,
                locationUtils = locationUtils,
                context = context,
                locationPermissionLauncher = locationPermissionLauncher
            )


     /*           LocationSelectionScreen(
                location = locationViewModel.locationUpdates.value ?: Location(0.0,0.0),
                onLocationSelected = { location ->
                    listOfItems = listOfItems.mapIndexed { i, item ->
                        if (i == index) {
                            item.copy(location = location)
                        } else {
                            item
                        }
                    }
                    showLoc = false
                })*/
            false -> {}
        }
        Button(
            modifier = Modifier
                .padding(16.dp),
            onClick = {
            showDialog = true
        }) {
            Text(text = "Add Item")
        }

        LazyColumn(reverseLayout = true, modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
            items(listOfItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(Color.Transparent)
                        .border(
                            BorderStroke(2.dp, color = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    shape = RoundedCornerShape(16.dp)
                    ,
                ) {
                    Row {
                        if (item.isEditing) {
                           EditItem(item = item, updateList = { updatedItem ->
                               /*var prevList = listOfItems.map {
                                    if (it.id == item.id) {
                                        it.name = updatedItem.name
                                        it.quantity = updatedItem.quantity
                                        it.isEditing = false
                                    }
                                    return@map it
                                }
                               listOfItems = emptyList<Item>()
                               listOfItems = prevList*/
                               listOfItems = listOfItems.map { it.copy(isEditing = false)}
                               val editedItem = listOfItems.find { it.id == item.id } // Referencia para o Item a ser editado
                               editedItem?.let {
                                   it.name = updatedItem.name
                                   it.quantity = updatedItem.quantity
                               }

                            })
                        } else {
                            ShoppingListItem(
                                item = item,
                                onEditClick = {
                                var prevList = listOfItems.map {
                                    if (it.id == item.id) {
                                        it.isEditing = true
                                    }
                                    return@map it
                                }
                                listOfItems = emptyList<Item>()
                                listOfItems = prevList
                                Log.d("ShoppingCart 2", "listOfItems: $listOfItems")
                            },
                                onDeleteClick = {
                                    var prevList = listOfItems.filter { it -> it.id != item.id }
                                    listOfItems = emptyList<Item>()
                                    listOfItems = prevList
                                    Log.d("ShoppingCart 4", "listOfItems: $listOfItems")
                                },
                                onLocationClick = {
                                    locationViewModel.updateLocation(item.location)
                                    showLoc = true
                                    index = listOfItems.indexOf(item)
                                    Log.d("ShoppingCart 3", "listOfItems: $listOfItems")

                                },


                                )
                        }
                    }
                }
            }
        }

        when (showDialog) {
            true -> MinimalDialog(
                onDismissRequest = { showDialog = false },
                onConfirm = { itemName, itemQuantity, itemLocation ->
                    listOfItems = listOfItems.plus(
                        Item(
                            id = (listOfItems[listOfItems.size-1].id.toInt()+1).toString(),
                            name = itemName,
                            quantity = itemQuantity,
                            isEditing = false,
                            location = itemLocation ?: Location(0.0,0.0, "Random")
                        )
                    )
                    showDialog = false
                },
                locationViewModel = locationViewModel,
                locationUtils = locationUtils,
                context = context,
                locationPermissionLauncher = locationPermissionLauncher,
                navHostController = navController,
                index = index

            )
            false -> {}
        }



    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinimalDialog(onDismissRequest: () -> Unit, onConfirm: (String, String, Location?) -> Unit, locationViewModel: MVVM_Location, locationUtils: LocationUtils, context: Context, locationPermissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>, navHostController: NavHostController, index: Int) {
    var itemName by remember { mutableStateOf("") }
    var itemQuantity by remember { mutableStateOf("1") }
    var itemLocation by remember { mutableStateOf<Location?>(locationViewModel.locationUpdates.value) }
    var showLoc by remember { mutableStateOf(false) }
    var show by remember { mutableStateOf(true) }
    val context = LocalContext.current

    when (showLoc) {
        true -> ValidationPermissionLocation(
            location = locationViewModel.locationUpdates.value ?: Location(0.0, 0.0),
            onLocationSelected = { location ->

                var loc = location.copy(
                    address = locationUtils.reverseGeocodeLocation(location)
                )
                itemLocation = loc
                showLoc = false
                show = true
            },
            locationViewModel = locationViewModel,
            locationUtils = locationUtils,
            context = context,
            locationPermissionLauncher = locationPermissionLauncher
        )


        /*           LocationSelectionScreen(
                   location = locationViewModel.locationUpdates.value ?: Location(0.0,0.0),
                   onLocationSelected = { location ->
                       listOfItems = listOfItems.mapIndexed { i, item ->
                           if (i == index) {
                               item.copy(location = location)
                           } else {
                               item
                           }
                       }
                       showLoc = false
                   })*/
        false -> {}
    }

    if (show) {


        AlertDialog(
            onDismissRequest = { onDismissRequest() },

            ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Add Item",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    TextInput(
                        value = itemName,
                        onValueChange = { it -> itemName = it },
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .wrapContentSize(Alignment.Center)
                    )
                    TextInput(
                        value = itemQuantity,
                        onValueChange = { it -> itemQuantity = it },
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .wrapContentSize(Alignment.Center)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))
                        var locPrinted = itemLocation?.address?.length?.let {
                            if (it > 10)  {
                                itemLocation?.address?.substring(0, 10) + "..."
                            } else {
                                itemLocation?.address
                            }
                        }
                        Text(
                            text = "Loc: ${locPrinted ?: "No location"}",
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .wrapContentSize(Alignment.Center)
                        )

                        IconButton(
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .wrapContentSize(Alignment.Center),
                            onClick= {
                                    //show = false
                                    //showLoc = true
                                if(locationUtils.getLocationPermission(context)) {
                                    navHostController.navigate("locationscreen") {
                                        this.launchSingleTop
                                    }
                                }
                                else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location"
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            modifier = Modifier
                                .padding(16.dp)
                                .weight(1f),
                            onClick = {
                                if (!itemName.isNotBlank()) {
                                    Toast
                                        .makeText(
                                            context,
                                            "Please insert a name",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                    return@IconButton
                                }
                                onConfirm.invoke(itemName, itemQuantity,
                                    locationViewModel.locationUpdates.value?: Location(0.0, 0.0).copy(
                                        address = itemLocation?.address
                                    )
                                )
                            }) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = "Add"
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        IconButton(
                            modifier = Modifier
                                .padding(16.dp)
                                .weight(1f),
                            onClick = { onDismissRequest() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Add"
                            )
                        }
                    }

                }
            }
        }
    }
}


@Composable
fun TextInput(value: String = "", onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
            .focusRequester(focusRequester),
        value = value,
        onValueChange = { it -> onValueChange.invoke(it)
        },
        placeholder = { Text("Enter value") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
                focusRequester.freeFocus()
                focusManager.clearFocus()
            }
        ),
        shape = RoundedCornerShape(8.dp),
        singleLine = true,
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItem(item:Item, updateList: (Item) -> Unit, modifier: Modifier = Modifier) {
    var itemName by remember { mutableStateOf(item.name) }
    var itemQuantity by remember { mutableStateOf(item.quantity) }
    var isEditing by remember { mutableStateOf(item.isEditing) }

    if (isEditing) {
        Row(modifier = Modifier
            .padding(2.dp)
            .fillMaxWidth()
            .background(Color.White),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                                ) {
                TextField(
                    value = itemName,
                    onValueChange = { it -> itemName = it },
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(4.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        containerColor = Color.White
                    ),
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextField(
                    value = itemQuantity,
                    onValueChange = { it -> itemQuantity = it },
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(4.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        containerColor = Color.White
                    ),
                )
            }
            IconButton(
                modifier = Modifier,
                onClick = {
                    updateList.invoke(
                        Item(
                            id = item.id,
                            name = itemName,
                            quantity = itemQuantity.toIntOrNull()?.toString() ?: "1",
                            isEditing = false
                        )
                    )
                    isEditing = false

                }) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "Add"
                )
            }
        }
    }
    
}

@Composable
fun ShoppingListItem(item: Item, onEditClick: () -> Unit, onDeleteClick: () -> Unit, onLocationClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = Modifier
        .padding(2.dp)
        .fillMaxWidth()
        .background(Color.White),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically

    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
        Row {
            Text(
                text = item.name,
                modifier = Modifier
                    .padding(start = 8.dp),
                textAlign = TextAlign.Start,
            )
            Text(
                text = "Qty: ${item.quantity}",
                modifier = Modifier
                    .padding(start = 16.dp),
                textAlign = TextAlign.End,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
            var locPrinted = item.location.address?.length?.let {
                if (it > 10)  {
                    item.location.address?.substring(0, 10) + "..."
                } else {
                    item.location.address
                }
            }
        Text(
            text = "Loc: ${locPrinted ?: "No location"}",
            modifier = Modifier
                .padding(start = 8.dp)
            ,
            textAlign = TextAlign.Start,
        )

        }
        Row {
            IconButton(onClick = {
                onEditClick.invoke()

            }) {
                Icon(
                    imageVector = Icons.Default.Create,
                    contentDescription = "Delete"
                )

            }
            Spacer(modifier = Modifier.height(8.dp))
            IconButton(onClick = {
                onDeleteClick.invoke()

            }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete"
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            IconButton(onClick = {
                onLocationClick.invoke()

            }) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location"
                )
            }
        }
    }

}
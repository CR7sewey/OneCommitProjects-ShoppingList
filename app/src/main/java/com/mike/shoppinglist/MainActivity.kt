package com.mike.shoppinglist

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShoppingListTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ShoppingCart(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ShoppingCart(modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(false) }
    var listOfItems by remember { mutableStateOf<List<Item>>(
        listOf(
            Item("1","Item 1", "1", false),
            Item("2","Item 2", "2", false),
        ))
    } //emptyList<Item>().toMutableList()) }
    var updateScreen by remember { mutableStateOf(false) }
    Log.d("ShoppingCart", "listOfItems: $listOfItems")

    Column(modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
        ) {
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
                                }


                                )
                        }
                    }
                }
            }
        }

        when (showDialog) {
            true -> MinimalDialog(
                onDismissRequest = { showDialog = false },
                onConfirm = { itemName, itemQuantity ->
                    listOfItems = listOfItems.plus(
                        Item(
                            id = (listOfItems[listOfItems.size-1].id.toInt()+1).toString(),
                            name = itemName,
                            quantity = itemQuantity,
                            isEditing = false
                        )
                    )
                    showDialog = false
                }

            )
            false -> {}
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinimalDialog(onDismissRequest: () -> Unit, onConfirm: (String, String) -> Unit) {
    var itemName by remember { mutableStateOf("") }
    var itemQuantity by remember { mutableStateOf("1") }
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { onDismissRequest() },

    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
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
                                    .makeText(context, "Please insert a name", Toast.LENGTH_SHORT)
                                    .show()
                                return@IconButton
                            }
                            onConfirm.invoke(itemName, itemQuantity)
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
        Row(modifier = Modifier.padding(2.dp)
            .fillMaxWidth().background(Color.White),
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
                        .wrapContentSize().padding(4.dp),
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
                        .wrapContentSize().padding(4.dp),
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
fun ShoppingListItem(item: Item, onEditClick: () -> Unit, onDeleteClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = Modifier.padding(2.dp)
        .fillMaxWidth().background(Color.White),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically

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
        }
    }

}
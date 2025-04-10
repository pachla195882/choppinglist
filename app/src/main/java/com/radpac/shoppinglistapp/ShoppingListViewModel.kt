package com.radpac.shoppinglistapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShoppingItem(
    val id: Int,
    var name: String,
    var quantity: Int,
    var isEditing: Boolean = false
)

class ShoppingListViewModel : ViewModel() {

    private val _sItems = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val sItems: StateFlow<List<ShoppingItem>> = _sItems.asStateFlow()

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _itemName = MutableStateFlow("")
    val itemName: StateFlow<String> = _itemName.asStateFlow()

    private val _itemQuantity = MutableStateFlow("")
    val itemQuantity: StateFlow<String> = _itemQuantity.asStateFlow()

    private val _quantityError = MutableStateFlow<String?>(null)
    val quantityError: StateFlow<String?> = _quantityError.asStateFlow()

    fun addItem(item: ShoppingItem) {
        viewModelScope.launch {
            _sItems.update { currentItems -> currentItems + item }
        }
    }

    fun removeItem(item: ShoppingItem) {
        viewModelScope.launch {
            _sItems.update { currentItems -> currentItems.filter { it != item } }
        }
    }

    fun updateItem(item: ShoppingItem, editedName: String, editedQuantity: Int) {
        viewModelScope.launch {
            _sItems.update { currentItems ->
                currentItems.map {
                    if (it.id == item.id) {
                        it.copy(name = editedName, quantity = editedQuantity, isEditing = false)
                    } else {
                        it
                    }
                }
            }
        }
    }

    fun updateItemName(name: String) {
        _itemName.update { name }
    }

    fun updateItemQuantity(quantity: String) {
        _itemQuantity.update { quantity }
        _quantityError.update { validateQuantity(quantity) }
    }

    fun validateQuantity(quantity: String): String? {
        return if (quantity.isNotEmpty() && quantity.toIntOrNull() == null) {
            "Quantity must be a number!"
        } else {
            null
        }
    }

    fun showDialog() {
        _showDialog.update { true }
    }

    fun hideDialog() {
        _showDialog.update { false }
        _quantityError.update { null }
    }

    fun clearItemData() {
        _itemName.update { "" }
        _itemQuantity.update { "" }
        _quantityError.update { null }
    }

    fun toggleEditing(item: ShoppingItem) {
        viewModelScope.launch {
            _sItems.update { currentItems ->
                currentItems.map {
                    if (it.id == item.id) {
                        it.copy(isEditing = !it.isEditing)
                    } else {
                        it.copy(isEditing = false)
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingListItem(
    item: ShoppingItem,
    onEditClick: () -> Unit, //lambda function is executed when edit action is triggered
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        //onClick = {}, // Add an empty onClick to make the whole card clickable if needed
        colors = CardDefaults.cardColors(
            containerColor = Color.White, // Use primaryContainer for a subtle background
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp) // Add a subtle shadow
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp) // Increase padding inside the card
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.name, style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            ) // Use a larger text style
            Text(
                text = "Qty: ${item.quantity}", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary // Use a color that contrasts with the background
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.primary // Use the error color for delete
                    )
                }
            }
        }
    }
}

@Composable
fun ShoppingListApp(viewModel: ShoppingListViewModel = ShoppingListViewModel()) {
    val sItems by viewModel.sItems.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val itemName by viewModel.itemName.collectAsState()
    val itemQuantity by viewModel.itemQuantity.collectAsState()
    val quantityError by viewModel.quantityError.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showDialog() },
                modifier = Modifier.padding(8.dp),
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add") },
                text = { Text(text = "Add Item") })
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(sItems) { item ->
                if (item.isEditing) {
                    ShoppingItemEditor(item = item, onEditComplete = { editedName, editedQuantity ->
                        viewModel.updateItem(item, editedName, editedQuantity)
                    },
                        validateQuantity = viewModel::validateQuantity // Pass the function
                    )
                } else {
                    // Display the item
                    ShoppingListItem(
                        item = item,
                        onEditClick = {
                            // finding out which item we are editing and changing its "isEditing"
                            // value to true
                            viewModel.toggleEditing(item)
                        },
                        onDeleteClick = {
                            viewModel.removeItem(item)
                        }
                    )
                }
            }

        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.hideDialog()
                viewModel.clearItemData()
            },
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ElevatedButton(
                        onClick = {
                            if (itemName.isNotBlank() && itemQuantity.isNotBlank() &&
                                quantityError == null
                            ) {
                                val newItem = ShoppingItem(
                                    id = sItems.size + 1,
                                    name = itemName,
                                    quantity = itemQuantity.toInt()
                                )
                                viewModel.addItem(newItem)
                                viewModel.hideDialog()
                                viewModel.clearItemData()
                                Log.d("ShoppingListApp", "Item added: $newItem")
                            }
                        },
                        enabled = quantityError == null,
                        elevation = ButtonDefaults.buttonElevation(8.dp),
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                    ) {
                        Text(text = "Add")
                    }
                    ElevatedButton(
                        onClick = {
                            viewModel.hideDialog()
                            viewModel.clearItemData()
                        },
                        elevation = ButtonDefaults.buttonElevation(8.dp),
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                    ) {
                        Text(text = "Cancel")
                    }
                }
            },
            title = { Text("Add shopping item") },
            text = {
                Column {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { viewModel.updateItemName(it) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                    OutlinedTextField(
                        value = itemQuantity,
                        onValueChange = { viewModel.updateItemQuantity(it) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                    Text(
                        text = quantityError ?: "", // Display the error message
                        color = MaterialTheme.colorScheme.error, // Use error color (usually red)
                        style = MaterialTheme.typography.bodySmall, // Use a smaller text style
                        modifier = Modifier.padding(start = 8.dp) // Add some left padding
                    )
                }
            },
        )
    }
}

@Composable
fun ShoppingItemEditor(item: ShoppingItem, onEditComplete: (String, Int) -> Unit,
                       validateQuantity: (String) -> String?) {
    var editedName by remember { mutableStateOf(item.name) }
    var editedQuantity by remember { mutableStateOf(item.quantity.toString()) }
    var isEditing by remember { mutableStateOf(item.isEditing) }
    var quantityError by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        //onClick = {}, // Add an empty onClick to make the whole card clickable if needed
        colors = CardDefaults.cardColors(
            containerColor = Color.White, // Use primaryContainer for a subtle background
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // Add a subtle shadow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.padding(8.dp)
                )

                OutlinedTextField(
                    value = editedQuantity,
                    onValueChange = { editedQuantity = it
                                    quantityError = validateQuantity(it)
                                    },
                    label = { Text("Quantity") },
                    singleLine = true,
                    modifier = Modifier.padding(8.dp)
                )
                Text(
                    text = quantityError ?: "", // Display the error message
                    color = MaterialTheme.colorScheme.error, // Use error color (usually red)
                    style = MaterialTheme.typography.bodySmall, // Use a smaller text style
                    modifier = Modifier.padding(start = 8.dp) // Add some left padding
                )

                ElevatedButton(
                    modifier = Modifier.padding(8.dp),
                    onClick = {
                        isEditing = false
                        onEditComplete(editedName, editedQuantity.toIntOrNull() ?: 1)
                        Log.d(
                            "ShoppingItemEditor", "Item edited: $editedName," +
                                    " $editedQuantity"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                    elevation = ButtonDefaults.buttonElevation(8.dp),
                    enabled = quantityError == null
                ) {
                    Text("Modify")
                }
            }
        }
    }
}



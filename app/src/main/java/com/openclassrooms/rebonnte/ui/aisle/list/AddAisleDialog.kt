package com.openclassrooms.rebonnte.ui.aisle.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.openclassrooms.rebonnte.R

@Composable
fun AddAisleDialog(
    onConfirm: (name: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_aisle_dialog_title)) },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.new_aisle_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name) }
            ) { Text(stringResource(R.string.save_button)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )
}

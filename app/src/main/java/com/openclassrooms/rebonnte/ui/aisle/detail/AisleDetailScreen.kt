package com.openclassrooms.rebonnte.ui.aisle.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclassrooms.rebonnte.R
import com.openclassrooms.rebonnte.ui.components.MedicineItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AisleDetailScreen(
    aisleName: String,
    onBack: () -> Unit,
    onMedicineClick: (medicineId: String, aisleId: String) -> Unit,
    viewModel: AisleDetailViewModel = hiltViewModel()
) {
    val medicines by viewModel.medicines.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(aisleName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_cd))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize()
        ) {
            items(medicines) { medicine ->
                MedicineItem(
                    medicine = medicine,
                    onClick = { onMedicineClick(medicine.id, medicine.aisleId) }
                )
            }
        }
    }
}

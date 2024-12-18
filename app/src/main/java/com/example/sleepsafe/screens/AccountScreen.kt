package com.example.sleepsafe.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sleepsafe.viewmodel.AccountViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    viewModel: AccountViewModel = viewModel()
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile") },
                actions = {
                    if (!viewModel.isEditing) {
                        IconButton(onClick = { viewModel.startEditing() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit profile")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (viewModel.isEditing) {
                EditProfileContent(viewModel)
            } else {
                DisplayProfileContent(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileContent(viewModel: AccountViewModel) {
    OutlinedTextField(
        value = viewModel.tempName,
        onValueChange = { viewModel.updateTempName(it) },
        label = { Text("Name") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = viewModel.tempEmail,
        onValueChange = { viewModel.updateTempEmail(it) },
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = viewModel.tempAge,
        onValueChange = { viewModel.updateTempAge(it) },
        label = { Text("Age") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = viewModel.tempWeight,
        onValueChange = { viewModel.updateTempWeight(it) },
        label = { Text("Weight (kg)") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = viewModel.tempHeight,
        onValueChange = { viewModel.updateTempHeight(it) },
        label = { Text("Height (cm)") },
        modifier = Modifier.fillMaxWidth()
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = { viewModel.cancelEditing() },
            modifier = Modifier.weight(1f)
        ) {
            Text("Cancel")
        }
        Button(
            onClick = { viewModel.saveProfile() },
            modifier = Modifier.weight(1f)
        ) {
            Text("Save")
        }
    }
}

@Composable
private fun DisplayProfileContent(viewModel: AccountViewModel) {
    val profile = viewModel.userProfile

    ProfileField("Name", profile.name)
    ProfileField("Email", profile.email)
    ProfileField("Age", profile.age.toString())
    ProfileField("Weight", "${profile.weight} kg")
    ProfileField("Height", "${profile.height} cm")
}

@Composable
private fun ProfileField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value.ifEmpty { "Not set" },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

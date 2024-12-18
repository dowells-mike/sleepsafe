package com.example.sleepsafe.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val age: Int = 0,
    val weight: Float = 0f,
    val height: Float = 0f
)

class AccountViewModel : ViewModel() {
    var userProfile by mutableStateOf(UserProfile())
        private set

    var isEditing by mutableStateOf(false)
        private set

    // Temporary states for editing
    var tempName by mutableStateOf("")
    var tempEmail by mutableStateOf("")
    var tempAge by mutableStateOf("")
    var tempWeight by mutableStateOf("")
    var tempHeight by mutableStateOf("")

    fun startEditing() {
        tempName = userProfile.name
        tempEmail = userProfile.email
        tempAge = userProfile.age.toString()
        tempWeight = userProfile.weight.toString()
        tempHeight = userProfile.height.toString()
        isEditing = true
    }

    fun cancelEditing() {
        isEditing = false
    }

    fun saveProfile() {
        viewModelScope.launch {
            val newProfile = UserProfile(
                name = tempName,
                email = tempEmail,
                age = tempAge.toIntOrNull() ?: 0,
                weight = tempWeight.toFloatOrNull() ?: 0f,
                height = tempHeight.toFloatOrNull() ?: 0f
            )
            userProfile = newProfile
            isEditing = false
        }
    }

    fun updateTempName(name: String) {
        tempName = name
    }

    fun updateTempEmail(email: String) {
        tempEmail = email
    }

    fun updateTempAge(age: String) {
        tempAge = age
    }

    fun updateTempWeight(weight: String) {
        tempWeight = weight
    }

    fun updateTempHeight(height: String) {
        tempHeight = height
    }
}

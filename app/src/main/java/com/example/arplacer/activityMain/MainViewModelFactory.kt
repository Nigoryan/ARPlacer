package com.example.arplacer.activityMain

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.arplacer.network.LocationInformationRepository

class MainViewModelFactory(val context: Context,
    private val repository: LocationInformationRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass == MainViewModel::class.java)
            MainViewModel(context, repository) as T
        else
            throw IllegalArgumentException("Wrong ViewModel class")
    }
}
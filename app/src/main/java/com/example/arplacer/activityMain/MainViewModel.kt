package com.example.arplacer.activityMain

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.arplacer.R
import com.example.arplacer.entity.AdditionalInformResponse
import com.example.arplacer.entity.LocationInformationResponse
import com.example.arplacer.network.LocationInformationRepository
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(val context: Context, val repository: LocationInformationRepository) : ViewModel() {
    val coroutineScope = CoroutineScope(Dispatchers.IO)
    val placeLiveData = MutableLiveData<MutableList<LocationInformationResponse>>()
    val addInformLiveData = MutableLiveData<AdditionalInformResponse>()

    fun getPlaceInform() {
        coroutineScope.launch {

            val result =
                LocationInformationRepository.getLocationInformationService().getLocationInform(
                    3000,
                    27.4343001,
                    53.8542905,
                    context.getString(R.string.response_type),
                    context.getString(R.string.opentripmap_key)
                )
            placeLiveData.postValue(result)
        }
    }

    fun getAdditionalInform(){

    }
}
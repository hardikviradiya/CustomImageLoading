package com.hardik.customimageloading.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hardik.customimageloading.repository.MainRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val mainRepository = MainRepository()
    val photosLiveData = mainRepository.photosLiveData

    fun loadPhotos(page: Int) {
        viewModelScope.launch {
            mainRepository.getImages(page)
        }
    }
}

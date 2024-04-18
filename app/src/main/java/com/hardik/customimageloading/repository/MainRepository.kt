package com.hardik.customimageloading.repository

import androidx.lifecycle.MutableLiveData
import com.hardik.customimageloading.models.UnsplashImage
import com.hardik.customimageloading.network.MainAPIService

class MainRepository {
    val photosLiveData: MutableLiveData<MutableList<UnsplashImage>> = MutableLiveData()

   suspend fun getImages(currentPage: Int) {
       val response = MainAPIService.getImages(currentPage)

       if(response.isNotEmpty()){
           photosLiveData.value = response
       }
   }
}
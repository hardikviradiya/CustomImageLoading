package com.hardik.customimageloading.network

import com.hardik.customimageloading.models.UnsplashImage
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object MainAPIService {
    private val retrofit = Retrofit.Builder().baseUrl("https://api.unsplash.com/")
            .addConverterFactory(GsonConverterFactory.create()).build()

    private val imageApi = retrofit.create(ImageApi::class.java)

    suspend fun getImages(page: Int): MutableList<UnsplashImage> {
        try {
            return imageApi.getImages(
                page,
                30,
                "popular",
                "tj2egI5P0n1lkvtjUx7W5fylNPwxDrkI4ECUmQ5Da-k"
            )
        } catch (e: Exception) {
            return mutableListOf<UnsplashImage>()
        }

    }
}

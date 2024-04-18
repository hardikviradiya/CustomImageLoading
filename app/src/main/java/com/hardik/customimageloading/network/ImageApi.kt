package com.hardik.customimageloading.network

import com.hardik.customimageloading.models.UnsplashImage
import retrofit2.http.GET
import retrofit2.http.Query

interface ImageApi {
    @GET("photos")
    suspend fun getImages(
        @Query("page") page: Int,
        @Query("per_page") pageSize: Int,
        @Query("order_by") orderBy: String,
        @Query("client_id") clientId: String
    ): MutableList<UnsplashImage>
}
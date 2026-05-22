package com.example.data.api

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.Response

interface CyberStreamApi {
    @GET("storage/sources")
    suspend fun getStorageSources(): Response<ApiResponse<List<StorageSourceResponse>>>

    @GET("storage/provider-types")
    suspend fun getProviderTypes(): Response<ApiResponse<List<StorageProviderType>>>

    @POST("storage/sources")
    suspend fun addStorageSource(@retrofit2.http.Body request: AddStorageSourceRequest): Response<ApiResponse<Any>>

    @POST("storage/preview")
    suspend fun previewStorage(@retrofit2.http.Body request: StoragePreviewRequest): Response<ApiResponse<StoragePreviewData>>

    @GET("scan")
    suspend fun getScanStatus(): Response<ApiResponse<ScanStatus>>

    @GET("homepage")
    suspend fun getHomepage(): Response<ApiResponse<HomepageData>>

    @POST("storage/sources/{id}/scan")
    suspend fun scanSource(@Path("id") id: Int): Response<ApiResponse<Any>>
}

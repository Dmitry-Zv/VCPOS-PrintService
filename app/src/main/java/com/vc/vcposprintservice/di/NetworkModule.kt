package com.vc.vcposprintservice.di

import com.google.gson.GsonBuilder
import com.vc.vcposprintservice.data.network.FileApi
import com.vc.vcposprintservice.domain.repository.FileRepository
import com.vc.vcposprintservice.domain.usecases.fileusecases.FileUseCases
import com.vc.vcposprintservice.domain.usecases.fileusecases.GetFiles
import com.vc.vcposprintservice.domain.usecases.fileusecases.PutStatus
import com.vc.vcposprintservice.utils.ByteArrayDeserializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    fun provideOkhttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        val gson = GsonBuilder()
            .registerTypeAdapter(ByteArray::class.java, ByteArrayDeserializer())
            .create()

        return Retrofit.Builder()
            .baseUrl("http://192.168.31.212:19128/api/v1/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    fun provideFileApi(retrofit: Retrofit): FileApi =
        retrofit.create(FileApi::class.java)

    @Provides
    @Singleton
    fun provideFileUseCases(repository: FileRepository) =
        FileUseCases(
            getFiles = GetFiles(repository),
            putStatus = PutStatus(repository)
        )
}
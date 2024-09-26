package com.vc.vcposprintservice.di

import com.vc.vcposprintservice.data.repository.AuthRepositoryImpl
import com.vc.vcposprintservice.data.repository.FileRepositoryImpl
import com.vc.vcposprintservice.data.repository.PrinterRepositoryImpl
import com.vc.vcposprintservice.domain.repository.AuthRepository
import com.vc.vcposprintservice.domain.repository.FileRepository
import com.vc.vcposprintservice.domain.repository.PrinterRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindFileRepositoryImpl_toFileRepository(fileRepositoryImpl: FileRepositoryImpl): FileRepository

    @Binds
    @Singleton
    fun bindAuthRepositoryImpl_toAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    fun bindPrinterRepositoryImpl_toPrinterRepository(printerRepositoryImpl: PrinterRepositoryImpl): PrinterRepository

}

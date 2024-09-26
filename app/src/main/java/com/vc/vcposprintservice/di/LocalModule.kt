package com.vc.vcposprintservice.di

import android.content.Context
import androidx.room.Room
import com.vc.vcposprintservice.data.local.AuthDao
import com.vc.vcposprintservice.data.local.PrintServiceDatabase
import com.vc.vcposprintservice.data.local.PrinterDao
import com.vc.vcposprintservice.domain.repository.AuthRepository
import com.vc.vcposprintservice.domain.repository.PrinterRepository
import com.vc.vcposprintservice.domain.usecases.auth.AuthUseCases
import com.vc.vcposprintservice.domain.usecases.auth.GetAuth
import com.vc.vcposprintservice.domain.usecases.auth.SaveAuth
import com.vc.vcposprintservice.domain.usecases.printer.GetPrinter
import com.vc.vcposprintservice.domain.usecases.printer.PrinterUseCases
import com.vc.vcposprintservice.domain.usecases.printer.SavePrinter
import com.vc.vcposprintservice.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PrintServiceDatabase =
        Room.databaseBuilder(
            context,
            PrintServiceDatabase::class.java,
            Constants.VCPOS_PRINT_SERVICE_DATABASE
        )
            .build()

    @Provides
    @Singleton
    fun provideAuthDao(database: PrintServiceDatabase): AuthDao =
        database.getAuthDao()

    @Provides
    @Singleton
    fun providePrinterDao(database: PrintServiceDatabase): PrinterDao =
        database.getPrinterDao()

    @Provides
    @Singleton
    fun provideAuthUseCases(repository: AuthRepository) =
        AuthUseCases(
            saveAuth = SaveAuth(repository),
            getAuth = GetAuth(repository)
        )

    @Provides
    @Singleton
    fun providePrinterUseCases(repository: PrinterRepository) =
        PrinterUseCases(
            savePrinter = SavePrinter(repository),
            getPrinter = GetPrinter(repository)
        )
}
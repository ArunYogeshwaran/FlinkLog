package com.ayogeshwaran.workoutlogger

import android.app.Application
import androidx.appfunctions.service.AppFunctionConfiguration
import com.ayogeshwaran.workoutlogger.appfunctions.WorkoutAppFunctions
import com.ayogeshwaran.workoutlogger.di.AppContainer

class WorkoutLoggerApplication : Application(), AppFunctionConfiguration.Provider {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    override val appFunctionConfiguration: AppFunctionConfiguration by lazy {
        val workoutAppFunctions = WorkoutAppFunctions(
            repository = container.repository
        )
        AppFunctionConfiguration.Builder()
            .addEnclosingClassFactory(WorkoutAppFunctions::class.java) { workoutAppFunctions }
            .build()
    }
}


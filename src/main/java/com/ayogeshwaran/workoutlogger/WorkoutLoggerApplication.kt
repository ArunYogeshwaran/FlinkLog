package com.ayogeshwaran.workoutlogger

import android.app.Application
import com.ayogeshwaran.workoutlogger.di.AppContainer

class WorkoutLoggerApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}


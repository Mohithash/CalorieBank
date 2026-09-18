package com.mohithash.caloriebank

import android.app.Application
import androidx.room.Room
import com.mohithash.caloriebank.ai.AiClient
import com.mohithash.caloriebank.data.AppDb
import com.mohithash.caloriebank.data.Prefs
import com.mohithash.caloriebank.data.Repository

class CalorieBankApp : Application() {
    lateinit var repo: Repository
    val ai = AiClient()

    override fun onCreate() {
        super.onCreate()
        val db = Room.databaseBuilder(this, AppDb::class.java, "caloriebank.db").build()
        repo = Repository(db, Prefs(this))
    }
}

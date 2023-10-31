package com.helpfull.goalsList

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.room.Room
import com.helpfull.goalsList.models.databases.GoalDatabase
import com.helpfull.goalsList.repositories.GoalRepository
import com.helpfull.goalsList.views.add.AddViewModel
import com.helpfull.goalsList.views.edit.EditViewModel
import com.helpfull.goalsList.views.main.MainViewModel
import com.helpfull.goalsList.views.submitDelete.SubmitDeleteViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**The main and single activity of this application.*/
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Default activity initialization.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // If koin context is null, then start the koin application.
        if(GlobalContext.getOrNull() == null) {
            startKoin {
                modules(
                    module {
                        // Add view models to koin.
                        viewModelOf(::AddViewModel)
                        viewModelOf(::MainViewModel)
                        viewModelOf(::SubmitDeleteViewModel)
                        viewModelOf(::EditViewModel)
                        // Add database to koin.
                        single {
                            Room.databaseBuilder(applicationContext, GoalDatabase::class.java,
                                "goal_database").build()
                        }
                        // Add goal repository to koin.
                        single {
                            GoalRepository()
                        }
                    }
                )
            }
        }
    }
}
package com.helpfull.goalsList.views.submitDelete

import androidx.lifecycle.ViewModel
import com.helpfull.goalsList.repositories.GoalRepository
import com.helpfull.goalsList.views.main.MainFragment
import org.koin.java.KoinJavaComponent.inject

/**
 * View model for [MainFragment]. It can retrieve goals from repository and delete them.
 * by priorities. Delete all goals can only [SubmitDeleteViewModel].
 */
class SubmitDeleteViewModel: ViewModel() {
    /** Singleton object of [GoalRepository]. In this viewModel, it is used to retrieving and
     * deleting goals*/
    private val repository: GoalRepository by inject(GoalRepository::class.java)

    /** Invokes [GoalRepository.delete] of [repository]. */
    fun delete(priority: Int) = repository.delete(priority)
    /** Invokes [GoalRepository.deleteAll] of [repository]. */
    fun deleteAll() = repository.deleteAll()
}
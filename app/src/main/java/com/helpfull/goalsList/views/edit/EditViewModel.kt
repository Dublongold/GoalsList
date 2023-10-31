package com.helpfull.goalsList.views.edit

import androidx.lifecycle.ViewModel
import com.helpfull.goalsList.models.Goal
import com.helpfull.goalsList.repositories.GoalRepository
import com.helpfull.goalsList.views.main.MainFragment
import com.helpfull.goalsList.views.submitDelete.SubmitDeleteViewModel
import org.koin.java.KoinJavaComponent.inject

/**
 * View model for [EditFragment]. It can only edit goals.
 */
class EditViewModel: ViewModel() {
    /** Singleton object of [GoalRepository]. In this viewModel, it is used to editing goals. */
    private val repository: GoalRepository by inject(GoalRepository::class.java)

    /** Invokes [GoalRepository.edit] of [repository]. */
    fun edit(oldPriority: Int, newGoal: Goal) = repository.edit(oldPriority, newGoal)
}
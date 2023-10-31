package com.helpfull.goalsList.views.add

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.helpfull.goalsList.R
import com.helpfull.goalsList.models.Goal
import com.helpfull.goalsList.views.editor.EditorFragment
import org.koin.android.ext.android.inject

/**
 * A fragment where we can add new goal.
 */
class AddFragment: EditorFragment() {
    private val viewModel: AddViewModel by inject()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add, container, false)
    }

    override fun actionButtonOnClickListener() {
        // Defines new goal priority and text.
        val priority = goalPriority.text.toString().toInt()
        val text = goalText.text.toString()
        // Adds new goal.
        viewModel.add(Goal(priority, text))
        // Show the user a message indicating that the operation was completed and pop back stack.
        view?.handler?.post {
            Toast.makeText(
                context,
                "New goal added successful!",
                Toast.LENGTH_SHORT
            ).show()
            findNavController().popBackStack()
        }
    }
}
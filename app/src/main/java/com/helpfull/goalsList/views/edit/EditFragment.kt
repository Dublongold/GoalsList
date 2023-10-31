package com.helpfull.goalsList.views.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.helpfull.goalsList.R
import com.helpfull.goalsList.models.Goal
import com.helpfull.goalsList.views.editor.EditorFragment
import com.helpfull.goalsList.views.submitDelete.SubmitDeleteFragment
import org.koin.android.ext.android.inject

/**
 * A fragment where we can edit or delete selected goal.
 * It should be opened with arguments GOAL_TEXT and GOAL_PRIORITY.
 */
class EditFragment: EditorFragment() {
    private val viewModel: EditViewModel by inject()
    /** Goal's priority, which will be changed in the future, if arguments aren't null. */
    private var priority: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Get goal's text and goal's priority from arguments.
        val goalText: String? = arguments?.getString(GOAL_TEXT)
        val goalPriority: Int? = arguments?.getInt(GOAL_PRIORITY)
        // If goal's priority variable isn't null, assign it to priority's variable.
        if(goalPriority != null) {
            priority = goalPriority
        }
        // Run the following code inside a view to avoid repeating "view.".
        view.run {
            // Sets goal's text inside EditText.
            findViewById<EditText>(R.id.goalTextField).setText(goalText)
            // Sets goal's priority inside EditText only if goal's priority isn't null.
            goalPriority?.let {
                findViewById<EditText>(R.id.priorityTextField).setText(it.toString())
            }
            // Sets on click listener for a delete button.
            // It just open SubmitDeleteFragment with OPEN_TYPE OPEN_TYPE_SINGLE.
            findViewById<ImageButton>(R.id.deleteGoalButton).setOnClickListener {
                findNavController().navigate(
                    R.id.openSubmitFragment,
                    bundleOf(
                        SubmitDeleteFragment.PRIORITY to goalPriority,
                        SubmitDeleteFragment.OPEN_TYPE to SubmitDeleteFragment.OPEN_TYPE_SINGLE
                        )
                )
            }
        }
    }

    override fun actionButtonOnClickListener() {
        // Edits goal.
        viewModel.edit(
            priority,
            Goal(goalPriority.text.toString().toInt(),goalText.text.toString())
        )
        // Show the user a message indicating that the operation was completed.
        view?.handler?.post {
            Toast.makeText(
                context,
                "Goal was edited!",
                Toast.LENGTH_SHORT
            ).show()
            findNavController().popBackStack()
        }
    }
    companion object {
        /**Name of the argument that contains goal's text.*/
        const val GOAL_TEXT = "text"
        /**Name of the argument that contains goal's priority.*/
        const val GOAL_PRIORITY = "priority"
    }
}
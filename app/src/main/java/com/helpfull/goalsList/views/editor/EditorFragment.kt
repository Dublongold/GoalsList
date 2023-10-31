package com.helpfull.goalsList.views.editor

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.helpfull.goalsList.R
import com.helpfull.goalsList.customViews.ErrorMessageView
import com.helpfull.goalsList.views.add.AddFragment
import com.helpfull.goalsList.views.edit.EditFragment
/**
 * Abstract class, which contains common methods and field to [AddFragment] and [EditFragment].
 */
abstract class EditorFragment: Fragment() {
    /** Contains the goal's text. */
    protected lateinit var goalText: EditText
    /** Contains the goal's priority. */
    protected lateinit var goalPriority: EditText
    /** Contains the goal's text error message that appeared after click on editor button. */
    private lateinit var textErrorMessage: ErrorMessageView
    /** Contains the goal's priority error message that appeared after click on editor button. */
    private lateinit var priorityErrorMessage: ErrorMessageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Run next code inside view to make calling findViewById method easier.
        view.run {
            // Set on click listener of go back button, which will call popBackStack method of
            // navigation controller.
            findViewById<ImageButton>(R.id.goBackButton).setOnClickListener {
                findNavController().popBackStack()
            }
            // Assign fields of editor.
            goalText = findViewById(R.id.goalTextField)
            goalPriority = findViewById(R.id.priorityTextField)
            textErrorMessage = findViewById(R.id.textErrorMessage)
            priorityErrorMessage = findViewById(R.id.priorityErrorMessage)
            // Will set error message to empty while we change content of goal's text.
            goalText.doOnTextChanged { _, _, _, _ ->
                textErrorMessage.setErrorMessage(null)
            }
            // Will set error message to empty while we change content of goal's priority.
            goalPriority.doOnTextChanged { _, _, _, _ ->
                priorityErrorMessage.setErrorMessage(null)
            }
            // Action, which will invoked after click on editor button.
            findViewById<TextView>(R.id.editorActionTextButton).setOnClickListener {
                // Variable to check for any errors.
                var error = false
                // If goal's text is empty, show for user corresponding error message.
                if(goalText.text.toString().isEmpty()){
                    textErrorMessage.setErrorMessage("Write goal text!")
                    error = true
                }
                // If goal's priority is empty, show for user corresponding error message.
                if(goalPriority.text.toString().isEmpty()) {
                    priorityErrorMessage.setErrorMessage("Write goal priority!")
                    error = true
                }
                // If there are no errors, run the following code.
                if(!error) {
                    // Set editor button disabled.
                    it.isEnabled = false
                    // Depending on whether instance is AddFragment or EditFragment,
                    // call actionButtonOnClickListener method inside coroutine.
                    // Here goal either goal will be added or goal will be edited.
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        actionButtonOnClickListener()
                    }
                    // If the editor isn't detached, run the following code.
                    if(!this@EditorFragment.isDetached) {
                        // Set the editor button enabled.
                        it.isEnabled = true
                    }
                }
            }
        }
    }

    /** The action that will performed after the editor button is clicked. */
    abstract fun actionButtonOnClickListener()
}
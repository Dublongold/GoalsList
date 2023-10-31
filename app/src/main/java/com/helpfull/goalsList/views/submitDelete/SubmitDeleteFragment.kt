package com.helpfull.goalsList.views.submitDelete

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.helpfull.goalsList.R
import org.koin.android.ext.android.inject

/**
 * The fragment in where user can confirm the deletion of one or more goals, depending on open type.
 */
class SubmitDeleteFragment: DialogFragment() {
    private val viewModel: SubmitDeleteViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Sets the dialog background color to transparent, as it will be white and look ugly.
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // Perform standard inflation.
        return inflater.inflate(R.layout.fragment_submit_delete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Run the following code inside a view to make working with the view easier.
        view.run {
            // If arguments isn't null, run the following code inside arguments.
            arguments?.run {
                // Get open type.
                val openType = getInt(OPEN_TYPE)
                // If open type is OPEN_TYPE_SINGLE, text will remain like:
                // "Do you really want to delete this goal?".
                // But if open type is OPEN_TYPE_ALL, we should change the text for better
                // understanding.
                if(openType == OPEN_TYPE_ALL) {
                    findViewById<TextView>(R.id.submitDeleteTitle).text =
                        getString(R.string.submit_delete_all_title)
                }
                // If user after all click button "Yes", run the following code.
                findViewById<TextView>(R.id.yesButton).setOnClickListener {
                    // Run the following code inside coroutine with dispatcher io, because we
                    // will work with Room.
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        // If necessary delete one goal, then get it priority and delete.
                        if(openType == OPEN_TYPE_SINGLE) {
                            val goalPriority = getInt(PRIORITY)
                            viewModel.delete(goalPriority)
                        }
                        // Otherwise, delete all goals.
                        else if(openType == OPEN_TYPE_ALL) {
                            viewModel.deleteAll()
                        }
                        // Either way, navigate to the menu.
                        handler.post {
                            findNavController().navigate(R.id.openMain)
                        }
                    }
                }
            // But if arguments is null, show corresponding message to the user.
            } ?: Toast.makeText(
                requireContext(),
                "Something wrong. Please, click \"No\" button.",
                Toast.LENGTH_SHORT).show()
            // If the user clicks the button "No",
            // than just close window by popping the back stack.
            findViewById<TextView>(R.id.noButton).setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    companion object {
        /**Name of the argument that contains goal's priority.*/
        const val PRIORITY = "priority"
        /**Name of the argument that contains fragment's open type.*/
        const val OPEN_TYPE = "open_type"
        /**Must be passed in arguments with open type. Needed when you want to delete a goal.*/
        const val OPEN_TYPE_SINGLE = 0
        /**Must be passed in arguments with open type. Needed when you want to delete all goals.*/
        const val OPEN_TYPE_ALL = 1
    }
}
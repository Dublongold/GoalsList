package com.helpfull.goalsList.views.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.ProgressBar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.helpfull.goalsList.R
import com.helpfull.goalsList.recyclerView.GoalRecyclerViewAdapter
import com.helpfull.goalsList.recyclerView.touchCallbacks.ItemMoveCallback
import com.helpfull.goalsList.views.submitDelete.SubmitDeleteFragment
import org.koin.android.ext.android.inject

/**
 * Main fragment, where is application menu of goals.
 */
class MainFragment: Fragment() {
    private val viewModel: MainViewModel by inject()
    /** The progress bar view that appears when retrieving goals from the goal's database. */
    private lateinit var progressBar: ProgressBar
    /** Goals recycler view adapter. It should be variable because the list of goals will be change
     * in the future */
    private lateinit var adapter: GoalRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Assign progress bar for:
        // 1) set visibility to gone after receiving goals.
        // 2) set visibility to visible while receiving goals.
        progressBar = view.findViewById(R.id.goalsProgressBar)
        // Set on click listener for add new goal button.
        // Once clicked, the navigation controller will open AddFragment.
        view.findViewById<ImageButton>(R.id.addNewGoalButton).setOnClickListener {
            findNavController().navigate(R.id.openAddFragment)
        }
        // Initialize pop up menu.
        initPopUpMenu()
        // Create a recyclerView variable to make assigning properties easier.
        val recyclerView = view.findViewById<RecyclerView>(R.id.goalsList)
        // Create adapter.
        adapter = createGoalsAdapter()
        // Assign adapter and layout manager of recyclerView.
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        // Run the following code in coroutine, because there will be work with Room.
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            // Get all goals.
            val goals = viewModel.getGoals().toMutableList()
            // Create item move callback for moving goals using drag and drop.
            val callback = ItemMoveCallback(adapter)
            // Create item touch helper and pass callback created above to it.
            val touchHelper = ItemTouchHelper(callback)
            // Update goals list and make progress bar visibility to gone.
            waitWhileHandlerIsNullAfterPost {
                touchHelper.attachToRecyclerView(recyclerView)
                adapter.updateList(goals)
                progressBar.visibility = View.GONE
            }
        }
    }

    /**
     * Initialize pop up menu. By default, it can delete all goals and align by priorities.
     */
    private fun initPopUpMenu() {
        // If view not null,
        view?.findViewById<ImageButton>(R.id.kebabMenu)?.let {
            // Create popup menu and inflate its.
            val popup = PopupMenu(context, it)
            popup.menuInflater.inflate(R.menu.main_three_dots_menu, popup.menu)
            // Set on menu item click listener.
            popup.setOnMenuItemClickListener { menu: MenuItem ->
                when (menu.itemId) {
                    R.id.alignByPriorities -> {
                        // Run the following code in coroutine, because there will be work with Room.
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            // Align goals by priorities and get result.
                            val goals = viewModel.alignByPriorities()
                            // Update adapter goals list with aligned goals.
                            waitWhileHandlerIsNullAfterPost {
                                adapter.updateList(goals)
                            }
                        }
                        true
                    }
                    R.id.deleteAll -> {
                        // Show window with confirmation to delete all goals.
                        findNavController().navigate(
                            R.id.openSubmitFragment,
                            bundleOf(
                                SubmitDeleteFragment.OPEN_TYPE to
                                        SubmitDeleteFragment.OPEN_TYPE_ALL
                            )
                        )
                        true
                    }

                    else -> false
                }
            }
            // Set on click listener, which will show pop up menu.
            it.setOnClickListener {
                popup.show()
            }
        }
    }

    /** Since view handler can be null,
     * this method will wait cyclically 100 seconds while it is null.
     * And when view handler is no longer null, post method of the handler will be called.
     * But if view handler is null after 10 seconds of calling this method,
     * the loop ends and post method of the handler won't be called. */
    private suspend fun waitWhileHandlerIsNullAfterPost(action: () -> Unit) {
        while(view?.handler == null) {
            delay(100)
        }
        view?.handler?.post(action)
    }
    /**Just like the method says, create goals adapter.*/
    private fun createGoalsAdapter(): GoalRecyclerViewAdapter {
        return GoalRecyclerViewAdapter(
            mutableListOf(),
            // Click callback will open edit fragment for a clicked goal.
            clickCallback =  {
                findNavController().navigate(
                    R.id.openEditFragment,
                    bundleOf("text" to it.text, "priority" to it.priority)
                )
            },
            // Save move callback will save changes after move goal using drag and drop.
            saveMoveCallback = { oldPriority, newGoal ->
                // Run the following code in coroutine, because there will be work with Room.
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    // Edit moved goal.
                    viewModel.editGoal(oldPriority, newGoal)
                    // Get all goals after edit goal.
                    val goals = viewModel.getGoals().toMutableList()
                    // Assign new goals list to adapter.
                    waitWhileHandlerIsNullAfterPost {
                        adapter.updateList(goals)
                    }
                }
            }
        )
    }

    companion object {
        /** Default log tag for this fragment. */
        const val LOG_TAG = "Main fragment"
    }
}
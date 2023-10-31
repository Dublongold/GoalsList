package com.helpfull.goalsList.customViews

import android.content.Context
import android.graphics.Color
import android.provider.CalendarContract.Colors
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Create red error message for the user. You can change error message color if necessary in the
 * future.
 */
class ErrorMessageView: androidx.appcompat.widget.AppCompatTextView {
    constructor(context: Context) : super(context) {
        additionalInitialization()
    }
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        additionalInitialization()
    }
    constructor(
        context: Context,
        attributeSet: AttributeSet,
        defStyleAttr: Int
    ) : super(context, attributeSet, defStyleAttr) {
        additionalInitialization()
    }

    /** Required for additional initialization for error message view. */
    private fun additionalInitialization() {
        setTextColor(Color.RED)
    }

    /** Changes error message color to [color].
     *
     * @param color Integer value of the new text color.
     */
    fun setErrorMessageColor(color: Int) {
        setTextColor(color)
    }

    /**
     * Sets error message text. If [text] is null, set text to "" and height to 0.
     * Otherwise, set text to [text] and height to [LinearLayout.LayoutParams.WRAP_CONTENT].
     *
     * @param text Error message text. Can be null, if there are no errors.
     */
    fun setErrorMessage(text: String?) {
        // Runs the following code if text is null.
        if(text == null) {
            // Just sets text to blank and the height to 0.
            this.text = ""
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0
            )
        }
        else {
            // Just sets text to passed as parameter text and the height to -2 (Wrap content).
            this.text = text
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }
}
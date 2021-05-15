package com.kunzisoft.keepass.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.kunzisoft.keepass.R

class EntryEditFieldView @JvmOverloads constructor(context: Context,
                                                   attrs: AttributeSet? = null,
                                                   defStyle: Int = 0)
    : RelativeLayout(context, attrs, defStyle) {

    private val labelViewId = ViewCompat.generateViewId()
    private val valueViewId = ViewCompat.generateViewId()
    private val actionImageButtonId = ViewCompat.generateViewId()

    private val labelView = TextInputLayout(context).apply {
        id = labelViewId
        layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT).also {
                    it.addRule(LEFT_OF, actionImageButtonId)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                it.addRule(START_OF, actionImageButtonId)
            }
        }
    }
    private val valueView = TextInputEditText(context).apply {
        id = valueViewId
        layoutParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT)
        inputType = EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            imeOptions = EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
            importantForAutofill = IMPORTANT_FOR_AUTOFILL_NO
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        maxLines = 1
    }
    private var actionImageButton = AppCompatImageButton(
            ContextThemeWrapper(context, R.style.KeepassDXStyle_ImageButton_Simple), null, 0).apply {
        id = actionImageButtonId
        layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT).also {
            it.topMargin = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    8f,
                    resources.displayMetrics
            ).toInt()
            it.addRule(ALIGN_PARENT_RIGHT)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                it.addRule(ALIGN_PARENT_END)
            }
        }
        visibility = View.GONE
        contentDescription = context.getString(R.string.menu_edit)
    }

    init {
        // Manually write view to avoid view id bugs
        labelView.addView(valueView)
        addView(labelView)
        addView(actionImageButton)
    }

    fun applyFontVisibility(fontInVisibility: Boolean) {
        if (fontInVisibility)
            valueView.applyFontVisibility()
    }

    fun getActionImageView(): View {
        return actionImageButton
    }

    var label: String
        get() {
            return labelView.hint?.toString() ?: ""
        }
        set(value) {
            labelView.hint = value
        }

    var value: String
        get() {
            return valueView.text?.toString() ?: ""
        }
        set(value) {
            valueView.setText(value)
        }

    fun setValue(value: String?, valueType: TextType) {
        when (valueType) {
            TextType.NORMAL -> {
                valueView.inputType = valueView.inputType or EditorInfo.TYPE_TEXT_VARIATION_NORMAL
                valueView.maxLines = 1
            }
            TextType.MULTI_LINE -> {
                valueView.inputType = valueView.inputType or EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE
                valueView.maxEms = 40
                valueView.maxLines = 40
            }
        }
        valueView.setText(value ?: "")
    }

    fun setProtection(protection: Boolean) {
        if (protection) {
            labelView.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
            valueView.inputType = valueView.inputType or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
        }
    }

    fun setOnActionClickListener(onActionClickListener: OnClickListener? = null,
                                 @DrawableRes actionImageId: Int? = null) {
        actionImageId?.let {
            actionImageButton.setImageDrawable(ContextCompat.getDrawable(context, it))
        }
        actionImageButton.setOnClickListener(onActionClickListener)
        actionImageButton.visibility = if (onActionClickListener == null) View.GONE else View.VISIBLE
    }

    enum class TextType {
        NORMAL, MULTI_LINE
    }
}
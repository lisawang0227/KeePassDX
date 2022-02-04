package com.kunzisoft.keepass.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.kunzisoft.keepass.R
import com.kunzisoft.keepass.database.search.SearchHelper
import com.kunzisoft.keepass.database.search.SearchParameters

class SearchFiltersView @JvmOverloads constructor(context: Context,
                                                  attrs: AttributeSet? = null,
                                                  defStyle: Int = 0)
    : LinearLayout(context, attrs, defStyle) {

    private var searchContainer: ViewGroup
    private var searchAdvanceFiltersContainer: ViewGroup? = null
    private var searchExpandButton: ImageView
    private var searchNumbers: TextView
    private var searchCurrentGroup: CompoundButton
    private var searchCaseSensitive: CompoundButton
    private var searchTitle: CompoundButton
    private var searchUsername: CompoundButton
    private var searchPassword: CompoundButton
    private var searchURL: CompoundButton
    private var searchExpires: CompoundButton
    private var searchNotes: CompoundButton
    private var searchOther: CompoundButton
    private var searchUUID: CompoundButton
    private var searchTag: CompoundButton
    private var searchRecycleBin: CompoundButton
    private var searchTemplate: CompoundButton

    var searchParameters = SearchParameters()
        get() {
            return field.apply {
                this.searchInCurrentGroup = searchCurrentGroup.isChecked
                this.caseSensitive = searchCaseSensitive.isChecked
                this.searchInTitles = searchTitle.isChecked
                this.searchInUsernames = searchUsername.isChecked
                this.searchInPasswords = searchPassword.isChecked
                this.searchInUrls = searchURL.isChecked
                this.excludeExpired = !(searchExpires.isChecked)
                this.searchInNotes = searchNotes.isChecked
                this.searchInOther = searchOther.isChecked
                this.searchInUUIDs = searchUUID.isChecked
                this.searchInTags = searchTag.isChecked
                this.searchInRecycleBin = searchRecycleBin.isChecked
                this.searchInTemplates = searchTemplate.isChecked
            }
        }
        set(value) {
            field = value
            val tempListener = mOnParametersChangeListener
            mOnParametersChangeListener = null
            searchCurrentGroup.isChecked = value.searchInCurrentGroup
            searchCaseSensitive.isChecked = value.caseSensitive
            searchTitle.isChecked = value.searchInTitles
            searchUsername.isChecked = value.searchInUsernames
            searchPassword.isChecked = value.searchInPasswords
            searchURL.isChecked = value.searchInUrls
            searchExpires.isChecked = !value.excludeExpired
            searchNotes.isChecked = value.searchInNotes
            searchOther.isChecked = value.searchInOther
            searchUUID.isChecked = value.searchInUUIDs
            searchTag.isChecked = value.searchInTags
            searchRecycleBin.isChecked = value.searchInRecycleBin
            searchTemplate.isChecked = value.searchInTemplates
            mOnParametersChangeListener = tempListener
        }

    var onParametersChangeListener: ((searchParameters: SearchParameters) -> Unit)? = null
    private var mOnParametersChangeListener: ((searchParameters: SearchParameters) -> Unit)? = {
        // To recalculate height
        if (searchAdvanceFiltersContainer?.visibility == View.VISIBLE) {
            searchAdvanceFiltersContainer?.expand(
                false,
                searchAdvanceFiltersContainer?.getFullHeight()
            )
        }
        onParametersChangeListener?.invoke(searchParameters)
    }

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        inflater?.inflate(R.layout.view_search_filters, this)

        searchContainer = findViewById(R.id.search_container)
        searchAdvanceFiltersContainer = findViewById(R.id.search_advance_filters)
        searchExpandButton = findViewById(R.id.search_expand)
        searchNumbers = findViewById(R.id.search_numbers)
        searchCurrentGroup = findViewById(R.id.search_chip_current_group)
        searchCaseSensitive = findViewById(R.id.search_chip_case_sensitive)
        searchTitle = findViewById(R.id.search_chip_title)
        searchUsername = findViewById(R.id.search_chip_username)
        searchPassword = findViewById(R.id.search_chip_password)
        searchURL = findViewById(R.id.search_chip_url)
        searchExpires = findViewById(R.id.search_chip_expires)
        searchNotes = findViewById(R.id.search_chip_note)
        searchUUID = findViewById(R.id.search_chip_uuid)
        searchOther = findViewById(R.id.search_chip_other)
        searchTag = findViewById(R.id.search_chip_tag)
        searchRecycleBin = findViewById(R.id.search_chip_recycle_bin)
        searchTemplate = findViewById(R.id.search_chip_template)

        // Expand menu with button
        searchExpandButton.setOnClickListener {
            val isVisible = searchAdvanceFiltersContainer?.visibility == View.VISIBLE
            if (isVisible)
                closeAdvancedFilters()
            else
                openAdvancedFilters()
        }

        searchCurrentGroup.setOnCheckedChangeListener { _, isChecked ->
            searchParameters.searchInCurrentGroup = isChecked
            mOnParametersChangeListener?.invoke(searchParameters)
        }
        searchCaseSensitive.setOnCheckedChangeListener { _, isChecked ->
            searchParameters.caseSensitive = isChecked
            mOnParametersChangeListener?.invoke(searchParameters)
        }
        searchTitle.setOnCheckedChangeListener { _, isChecked ->
            searchParameters.searchInTitles = isChecked
            mOnParametersChangeListener?.invoke(searchParameters)
        }
        searchUsername.setOnCheckedChangeListener { _, isChecked ->
            searchParameters.searchInUsernames = isChecked
            mOnParametersChangeListener?.invoke(searchParameters)
        }
        searchPassword.setOnCheckedChangeListener { _, isChecked ->
            searchParameters.searchInPasswords = isChecked
            mOnParametersChangeListener?.invoke(searchParameters)
        }
        searchURL.setOnCheckedChangeListener { _, isChecked ->
            searchParameters.searchInUrls = isChecked
            mOnParametersChangeListener?.invoke(searchParameters)
        }
        searchExpires.setOnCheckedChangeListener { _, isChecked ->
            searchParameters.excludeExpired = !isChecked
            mOnParametersChangeListener?.invoke(searchParameters)
        }
        searchNotes.setOnCheckedChangeListener { _, isChecked ->
            searchParameters.searchInNotes = isChecked
            mOnParametersChangeListener?.invoke(searchParameters)
        }
        searchUUID.setOnCheckedChangeListener { _, isChecked ->
            searchParameters.searchInUUIDs = isChecked
            mOnParametersChangeListener?.invoke(searchParameters)
        }
        searchOther.setOnCheckedChangeListener { _, isChecked ->
            searchParameters.searchInOther = isChecked
            mOnParametersChangeListener?.invoke(searchParameters)
        }
        searchTag.setOnCheckedChangeListener { _, isChecked ->
            searchParameters.searchInTags = isChecked
            mOnParametersChangeListener?.invoke(searchParameters)
        }
        searchRecycleBin.setOnCheckedChangeListener { _, isChecked ->
            searchParameters.searchInRecycleBin = isChecked
            mOnParametersChangeListener?.invoke(searchParameters)
        }
        searchTemplate.setOnCheckedChangeListener { _, isChecked ->
            searchParameters.searchInTemplates = isChecked
            mOnParametersChangeListener?.invoke(searchParameters)
        }
    }

    fun setNumbers(numbers: Int) {
        searchNumbers.text = SearchHelper.showNumberOfSearchResults(numbers)
    }

    fun setCurrentGroupText(text: String) {
        val maxChars = 12
        searchCurrentGroup.text = when {
            text.isEmpty() -> context.getString(R.string.current_group)
            text.length > maxChars -> text.substring(0, maxChars) + "…"
            else -> text
        }
    }

    fun enableOther(enable: Boolean) {
        searchOther.isEnabled = enable
    }

    fun enableTags(enable: Boolean) {
        searchTag.isEnabled = enable
    }

    fun enableTemplates(enable: Boolean) {
        searchTemplate.isEnabled = enable
    }

    fun closeAdvancedFilters() {
        searchAdvanceFiltersContainer?.collapse()
    }

    private fun openAdvancedFilters() {
        searchAdvanceFiltersContainer?.expand(true,
            searchAdvanceFiltersContainer?.getFullHeight()
        )
    }

    override fun setVisibility(visibility: Int) {
        when (visibility) {
            View.VISIBLE -> {
                searchAdvanceFiltersContainer?.visibility = View.GONE
                searchContainer.showByFading()
            }
            else -> {
                searchContainer.hideByFading()
                if (searchAdvanceFiltersContainer?.visibility == View.VISIBLE) {
                    searchAdvanceFiltersContainer?.visibility = View.INVISIBLE
                    searchAdvanceFiltersContainer?.collapse()
                }
            }
        }
    }
}
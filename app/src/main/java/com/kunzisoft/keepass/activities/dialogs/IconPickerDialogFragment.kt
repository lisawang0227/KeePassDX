/*
 * Copyright 2019 Jeremy Jamet / Kunzisoft.
 *
 * This file is part of KeePassDX.
 *
 *  KeePassDX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  KeePassDX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with KeePassDX.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.kunzisoft.keepass.activities.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.kunzisoft.keepass.R
import com.kunzisoft.keepass.adapters.IconPickerPagerAdapter
import com.kunzisoft.keepass.database.element.icon.IconImage


class IconPickerDialogFragment : DialogFragment() {

    private lateinit var iconPickerPagerAdapter: IconPickerPagerAdapter
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private var iconPickerListener: IconPickerListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            iconPickerListener = object : IconPickerListener {
                override fun iconPicked(icon: IconImage) {
                    (context as IconPickerListener).iconPicked(icon)
                    dismiss()
                }
            }
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(context.toString()
                    + " must implement " + IconPickerListener::class.java.name)
        }
    }

    override fun onDetach() {
        iconPickerListener = null
        super.onDetach()
    }

    /*
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let { activity ->
            val builder = AlertDialog.Builder(activity)

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            val root = activity.layoutInflater.inflate(R.layout.fragment_icon_picker, null)
            //viewPager = root.findViewById(R.id.icon_picker_pager)
            //tabLayout = root.findViewById(R.id.icon_picker_tabs)
            builder.setView(root)
            builder.setNegativeButton(android.R.string.cancel) { _, _ -> this@IconPickerDialogFragment.dialog?.cancel() }

            return builder.create()
        }
        return super.onCreateDialog(savedInstanceState)
    }
     */

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //val root = super.onCreateView(inflater, container, savedInstanceState)
        val root = layoutInflater.inflate(R.layout.fragment_icon_picker, container)
        viewPager = root.findViewById(R.id.icon_picker_pager)
        tabLayout = root.findViewById(R.id.icon_picker_tabs)
        iconPickerPagerAdapter = IconPickerPagerAdapter(childFragmentManager)
        iconPickerPagerAdapter.iconSelected = iconPickerListener
        viewPager.adapter = iconPickerPagerAdapter
        tabLayout.setupWithViewPager(viewPager)

        return root
    }

    interface IconPickerListener {
        fun iconPicked(icon: IconImage)
    }

    companion object {

        fun launch(activity: FragmentActivity) {
            // Create an instance of the dialog fragment and show it
            val dialog = IconPickerDialogFragment()
            dialog.show(activity.supportFragmentManager, "IconPickerDialogFragment")
        }
    }
}

package com.timetable.slava.timetableiate

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.ListAdapter
import android.widget.ListView

class TimetableFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.e("TimetableFragment", "Creating...")
        super.onCreate(savedInstanceState)
        pageNumber = arguments.let { it?.getInt(ARG_PAGE_NUMBER) }
        timetable = arguments.let { it?.getSerializable(ARG_TIMETABLE_LIST) as ArrayList<TimetableItem> }
        Log.e("TimetableFragment", "Created.")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.e("TimetableFragment", "Creating view...")
        val view = inflater.inflate(R.layout.timetable_list_view, container, false)

        listView = view.findViewById(R.id.list_view)
        listAdapter = TimetableListAdapter(inflater.context, timetable!!)//ArrayAdapter(inflater.context, R.layout.timatable_item_layout, timetable!!.toMutableList())

        listView!!.adapter = listAdapter
        Log.e("TimetableFragment", "View created.")
        return view
    }

    var listView: ListView? = null
    var listAdapter: TimetableListAdapter? = null
    var pageNumber: Int? = null

    // days in timetable
    var timetable: ArrayList<TimetableItem>? = null

    companion object {
        fun newInstance(page: Int): TimetableFragment {
            val fragment = TimetableFragment()
            val arguments = Bundle()
            arguments.putInt(ARG_PAGE_NUMBER, page)
            fragment.arguments = arguments

            return fragment
        }


        const val ARG_PAGE_NUMBER = "arg_page_number"
        const val ARG_TIMETABLE_LIST = "arg_tiometable_list"
    }
}
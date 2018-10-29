package com.timetable.slava.timetableiate

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView

class ChooseGroupDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(context, R.layout.choose_group_dialog, null)

        val items = arguments?.getStringArrayList(ARG_CHOOSE_GROUPS)
        val type = arguments?.getString(ARG_TYPE)
        val title = when (type) {
            MainActivity.ARG_ITEM_TYPE_GROUP -> resources.getText(R.string.choose_group)
            MainActivity.ARG_ITEM_TYPE_ROOM -> resources.getText(R.string.choose_room)
            MainActivity.ARG_ITEM_TYPE_TEACHER -> resources.getText(R.string.choose_teacher)
            else -> ""
        }

        val listView: ListView = view.findViewById(R.id.choose_group_dialog)
        val arrayAdapter = ArrayAdapter<String>(context!!, R.layout.choose_group_item)
        arrayAdapter.addAll(items!!)
        listView.adapter = arrayAdapter

        listView.setOnItemClickListener { _, _, position, _ ->
            lambda?.invoke(arrayAdapter.getItem(position)!!)
            dismiss()
        }


        return AlertDialog.Builder(context)
                .setView(view)
                .setTitle(title)
                .create()
    }

    var lambda: ((String) -> Unit)? = null

    companion object {
        const val ARG_CHOOSE_GROUPS = "_ChsGrps"
        const val ARG_TYPE = "_Type"

        fun newInstance(items: ArrayList<String>, type: String, lambda: (String) -> Unit): ChooseGroupDialog {
            val chooseGroupDialog = ChooseGroupDialog()
            chooseGroupDialog.lambda = lambda

            val bundle = Bundle()
            bundle.putString(ARG_TYPE, type)
            bundle.putStringArrayList(ARG_CHOOSE_GROUPS, items)
            chooseGroupDialog.arguments = bundle

            return chooseGroupDialog
        }
    }
}
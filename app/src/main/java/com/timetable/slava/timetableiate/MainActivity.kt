package com.timetable.slava.timetableiate

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val app = application as TimetableApp

        editText = findViewById(R.id.teInput)
        rbGroup = findViewById(R.id.rbGroup)
        rbRoom = findViewById(R.id.rbRoom)
        rbTeacher = findViewById(R.id.rbTeacher)
        btnShow = findViewById(R.id.btnShow)

        btnShow.setOnClickListener {

            val selectedType = getRadioGroupType()
            val enteredName = editText.text.toString()
            val findingUrls = app.findUrlPostfixes(selectedType, enteredName)

            when {
                findingUrls == null -> {}
                findingUrls.isEmpty() -> {
                    val msg = when (selectedType) {
                        ARG_ITEM_TYPE_ROOM -> resources.getString(R.string.cant_find_room)
                        ARG_ITEM_TYPE_TEACHER -> resources.getString(R.string.cant_find_teacher)
                        ARG_ITEM_TYPE_GROUP -> resources.getString(R.string.cant_find_group)
                        else -> ""
                    }
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                }

                findingUrls.size > 1 -> {
                    ChooseGroupDialog.newInstance(ArrayList(findingUrls.keys), selectedType) { findingName ->
                        startTimetableActivity(findingUrls[findingName]!!)
                    }.show(supportFragmentManager, "Dialog")
                    return@setOnClickListener
                }

                findingUrls.isNotEmpty() -> startTimetableActivity(
                        findingUrls.values.elementAt(0)
                )
            }
        }
    }

    private fun startTimetableActivity(urlPostfix: String) {
        val app = application as TimetableApp
        val url = GetTimetableTask.ARG_HTTP_START_PAGE + urlPostfix
        app.startNewTimetableActivity(url)
        btnShow.isClickable = false
    }

    private fun getRadioGroupType(): String {
        return when {
            rbGroup.isChecked -> ARG_ITEM_TYPE_GROUP
            rbRoom.isChecked -> ARG_ITEM_TYPE_ROOM
            rbTeacher.isChecked -> ARG_ITEM_TYPE_TEACHER
            else -> ""
        }
    }

    override fun onStart() {
        super.onStart()
        btnShow.isClickable = true
    }


    private lateinit var editText: EditText
    private lateinit var rbGroup: RadioButton
    private lateinit var rbRoom: RadioButton
    private lateinit var rbTeacher: RadioButton
    private lateinit var btnShow: Button


    companion object {
        const val ARG_ITEM_TYPE_GROUP = "group"
        const val ARG_ITEM_TYPE_ROOM = "room"
        const val ARG_ITEM_TYPE_TEACHER = "teacher"
    }
}

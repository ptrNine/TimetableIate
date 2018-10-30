package com.timetable.slava.timetableiate

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.*
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val app = application as TimetableApp

        editText = findViewById(R.id.teInput)
        radioGroup = findViewById(R.id.radioGroup)
        rbGroup = findViewById(R.id.rbGroup)
        rbRoom = findViewById(R.id.rbRoom)
        rbTeacher = findViewById(R.id.rbTeacher)
        btnShow = findViewById(R.id.btnShow)

        initAutoCompleteTextAdapter()

        radioGroup.setOnCheckedChangeListener { _, _ -> initAutoCompleteTextAdapter() }
        editText.setOnItemClickListener { _, _, _, _ -> btnShow.performClick() }

        editText.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE) {
                btnShow.performClick()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

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

    private fun initAutoCompleteTextAdapter() {
        val app = application as TimetableApp
        val type = getRadioGroupType()

        val handler = Handler(applicationContext.mainLooper)
        val runnable = Runnable {
            while(!app.isUrlPostfixesLoaded) {}
            Log.e("WAIT", "A SECOND")
            val urlPostfixNames = app.getUrlPostfixesNames(type)
            if (urlPostfixNames != null)
                editText.setAdapter(ArrayAdapter(applicationContext, R.layout.choose_group_item, urlPostfixNames))
            else
                Log.e("((((", "it's null(")

            Log.e("finish","finish init")
        }
        handler.postAtTime(runnable, 10000)}

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


    private lateinit var editText: AutoCompleteTextView
    private lateinit var radioGroup: RadioGroup
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

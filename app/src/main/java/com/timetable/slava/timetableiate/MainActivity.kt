package com.timetable.slava.timetableiate

import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    init {
        GetTimetableAsyncTask().execute(GetTimetableAsyncTask.ARG_GET_JSON_ALL_TIMETABLE_URLS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText = findViewById(R.id.teInput)
        rbGroup = findViewById(R.id.rbGroup)
        rbRoom = findViewById(R.id.rbRoom)
        rbTeacher = findViewById(R.id.rbTeacher)
        btnShow = findViewById(R.id.btnShow)

        btnShow!!.setOnClickListener {
            val type = getRequestType()
            val name = editText!!.text.toString()

            //val task = GetTimetableAsyncTask()
            //val jsonGroups = task.execute(GetTimetableAsyncTask.ARG_GET_JSON_TIMETABLE_URLS, name, type).get()[0]
            //val groups = Gson().fromJson(jsonGroups, Array<ResponseJsonGroup>::class.java)

            val finding = timetableUrls[type]!!.filterKeys { entry ->  entry.contains(name, true)}
            val urlPrefix = if (finding.values.isNotEmpty()) finding.values.elementAt(0) else null

            if (urlPrefix != null) {
                val htmlPage = GetTimetableAsyncTask().execute(GetTimetableAsyncTask.ARG_GET_TIMETABLE_HTML_PAGE, urlPrefix).get()[0]
                val jsonTimetable = parseTimetable(htmlPage)

                it.isClickable = false

                val intent = Intent(this, TimetableActivity::class.java)
                intent.putExtra("JSON_TIMETABLE", jsonTimetable)
                startActivity(intent)
            }

        }
    }

    fun getRequestType(): String {
        return when {
            rbGroup!!.isChecked -> GetTimetableAsyncTask.ARG_REQUEST_TYPE_GROUP
            rbRoom!!.isChecked -> GetTimetableAsyncTask.ARG_REQUEST_TYPE_ROOM
            rbTeacher!!.isChecked -> GetTimetableAsyncTask.ARG_REQUEST_TYPE_TEACHER
            else -> ""
        }
    }

    override fun onStart() {
        super.onStart()
        btnShow!!.isClickable = true
    }


    var editText: EditText? = null
    var rbGroup: RadioButton? = null
    var rbRoom: RadioButton? = null
    var rbTeacher: RadioButton? = null
    var btnShow: Button? = null

    private external fun parseTimetable(htmlPage: String): String



    companion object {
        init {
            System.loadLibrary("native-lib")
        }

        val timetableUrls = HashMap<String, HashMap<String, String>>()
    }
}

package com.timetable.slava.timetableiate

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getTimetableUrls()

        editText = findViewById(R.id.teInput)
        rbGroup = findViewById(R.id.rbGroup)
        rbRoom = findViewById(R.id.rbRoom)
        rbTeacher = findViewById(R.id.rbTeacher)
        btnShow = findViewById(R.id.btnShow)

        btnShow!!.setOnClickListener {
            if (timetableUrls.isNotEmpty()) {
                val type = getRequestType()
                val name = editText!!.text.toString()

                val finding = timetableUrls[type]!!.filterKeys { entry -> entry.startsWith(name, true) }
                val urlPrefix = if (finding.values.isNotEmpty()) finding.values.elementAt(0) else null

                if (urlPrefix != null) {
                    // async timetable load
                    val loadTimetableTask = AsyncLambda(WeakReference(applicationContext)) { cntxt ->
                        val htmlPage = GetTimetableTask(WeakReference(cntxt)).run(GetTimetableTask.ARG_GET_TIMETABLE_HTML_PAGE, urlPrefix)
                        if (htmlPage.containsKey(null)) {
                            val json = parseTimetable(htmlPage[null]!!)
                            return@AsyncLambda Gson().fromJson(json, ParsedTimetable::class.java)
                        } else {
                            return@AsyncLambda null
                        }
                    }
                    loadTimetableTask.execute()

                    it.isClickable = false

                    val app = application as TimetableApp
                    app.suprBundle.put(AsyncLambda.ARG_ASYNC_TIMETABLE_LOADER, loadTimetableTask)
                    val intent = Intent(this, TimetableActivity::class.java)
                    //intent.putExtra(AsyncLambda.ARG_ASYNC_TIMETABLE_LOADER, Bundle())
                    startActivity(intent)
                }
            } else {
                Toast.makeText(applicationContext, R.string.unnable_to_load_urls, Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun getTimetableUrls() {
        val file = AsyncFile(applicationContext, AsyncFile.ARG_FILENAME_TIMETABLE_URLS)

        if (file.exists()) {
            AsyncLambda(WeakReference(applicationContext)) {
                val fileName = it.resources.getString(R.string.filename_timetable_urls)
                val readedFile = ReadFileTask(fileName, WeakReference(it)).run()
                timetableUrls = Gson().fromJson(readedFile, timetableUrls::class.java)
            }.execute()
        } else {
            AsyncLambda(WeakReference(applicationContext)) {
                val res = GetTimetableTask(WeakReference(it)).run(GetTimetableTask.ARG_GET_JSON_ALL_TIMETABLE_URLS)
                timetableUrls[ARG_REQUEST_TYPE_GROUP] = HashMap()
                timetableUrls[ARG_REQUEST_TYPE_TEACHER] = HashMap()
                timetableUrls[ARG_REQUEST_TYPE_ROOM] = HashMap()

                val gson = Gson()
                for (type in res) {
                    val parsed = gson.fromJson(type.value, Array<ResponseJsonGroup>::class.java)
                    for (entity in parsed)
                        timetableUrls[type.key]!![entity.name] = type.key + "/" + entity.id
                }

                val fileName = it.resources.getString(R.string.filename_timetable_urls)
                WriteFileTask(it, fileName, timetableUrls).run()
            }.execute()
        }
    }

    private fun getRequestType(): String {
        return when {
            rbGroup!!.isChecked -> ARG_REQUEST_TYPE_GROUP
            rbRoom!!.isChecked -> ARG_REQUEST_TYPE_ROOM
            rbTeacher!!.isChecked -> ARG_REQUEST_TYPE_TEACHER
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
    var timetableUrls = HashMap<String, HashMap<String, String>>()

    private external fun parseTimetable(htmlPage: String): String

    companion object {
        const val ARG_REQUEST_TYPE_GROUP = "group"
        const val ARG_REQUEST_TYPE_ROOM = "room"
        const val ARG_REQUEST_TYPE_TEACHER = "teacher"

        init {
            System.loadLibrary("native-lib")
        }
    }
}

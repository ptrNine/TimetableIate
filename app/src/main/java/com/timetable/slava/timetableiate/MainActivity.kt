package com.timetable.slava.timetableiate

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import com.google.gson.Gson
import java.io.File
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getTimetableUrlsAsync()

        editText = findViewById(R.id.teInput)
        rbGroup = findViewById(R.id.rbGroup)
        rbRoom = findViewById(R.id.rbRoom)
        rbTeacher = findViewById(R.id.rbTeacher)
        btnShow = findViewById(R.id.btnShow)

        btnShow.setOnClickListener {
            if (timetableUrls.isEmpty()) {
                Toast.makeText(applicationContext, R.string.unnable_to_load_urls, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedType = getRadioGroupType()
            val enteredName = editText.text.toString()

            val findingUrls = timetableUrls[selectedType]!!.filterKeys {
                entry -> entry.startsWith(enteredName, true)
            }

            when {
                findingUrls.size > 1 -> {
                    ChooseGroupDialog.newInstance(ArrayList(findingUrls.keys), selectedType) {
                        findStr -> startTimetableActivity(findingUrls[findStr]!!)
                    }.show(supportFragmentManager, "Dialog")
                    return@setOnClickListener
                }

                findingUrls.isNotEmpty() -> startTimetableActivity(findingUrls.values.elementAt(0))

                else -> {
                    val msg = when (selectedType) {
                        ARG_ITEM_TYPE_ROOM -> resources.getString(R.string.cant_find_room)
                        ARG_ITEM_TYPE_TEACHER -> resources.getString(R.string.cant_find_teacher)
                        ARG_ITEM_TYPE_GROUP -> resources.getString(R.string.cant_find_group)
                        else -> ""
                    }
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startTimetableActivity(urlPrefix: String) {
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

        btnShow.isClickable = false

        val app = application as TimetableApp
        app.suprBundle.put(AsyncLambda.ARG_ASYNC_TIMETABLE_LOADER, loadTimetableTask)
        val intent = Intent(this, TimetableActivity::class.java)
        //intent.putExtra(AsyncLambda.ARG_ASYNC_TIMETABLE_LOADER, Bundle())
        startActivity(intent)
    }

    private fun getTimetableUrlsAsync() {
        val fileName = resources.getString(R.string.filename_timetable_urls)
        val file = File(applicationContext.filesDir, fileName)

        if (file.exists()) {
            AsyncLambda(WeakReference(applicationContext)) {
                val readedFile = ReadFileTask(WeakReference(it), fileName).run()
                timetableUrls = Gson().fromJson(readedFile, timetableUrls::class.java)
            }.execute()
        } else {
            AsyncLambda(WeakReference(applicationContext)) {
                val res = GetTimetableTask(WeakReference(it)).run(GetTimetableTask.ARG_GET_JSON_ALL_TIMETABLE_URLS)
                timetableUrls[ARG_ITEM_TYPE_GROUP] = HashMap()
                timetableUrls[ARG_ITEM_TYPE_TEACHER] = HashMap()
                timetableUrls[ARG_ITEM_TYPE_ROOM] = HashMap()

                val gson = Gson()
                for (type in res) {
                    val parsed = gson.fromJson(type.value, Array<ResponseJsonGroup>::class.java)
                    for (entity in parsed)
                        timetableUrls[type.key]!![entity.name] = type.key + "/" + entity.id
                }

                WriteFileTask(WeakReference(it), fileName, timetableUrls).run()
            }.execute()
        }
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
    private var timetableUrls = HashMap<String, HashMap<String, String>>()

    private external fun parseTimetable(htmlPage: String): String

    companion object {
        const val ARG_ITEM_TYPE_GROUP = "group"
        const val ARG_ITEM_TYPE_ROOM = "room"
        const val ARG_ITEM_TYPE_TEACHER = "teacher"

        init {
            System.loadLibrary("native-lib")
        }
    }
}

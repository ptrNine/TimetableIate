package com.timetable.slava.timetableiate

import android.app.Application
import android.content.Intent
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.ref.WeakReference
import kotlin.collections.HashMap

class TimetableApp : Application() {

    override fun onCreate() {
        super.onCreate()

        getTimetableUrlsAsync()
    }

    private fun getTimetableUrlsAsync() {
        val fileName = resources.getString(R.string.filename_timetable_urls)
        val file = File(applicationContext.filesDir, fileName)

        if (file.exists()) {
            AsyncLambda(WeakReference(applicationContext)) {
                val readFile = ReadFileTask(WeakReference(it), fileName).run()
                timetableUrls = Gson().fromJson(readFile,
                        object: TypeToken<HashMap<String, HashMap<String, String>>>(){}.type)
                return@AsyncLambda
            }.execute()
        } else {
            AsyncLambda(WeakReference(applicationContext)) {
                val res = GetTimetableTask(WeakReference(it)).run(GetTimetableTask.ARG_GET_JSON_ALL_TIMETABLE_URLS)
                val gson = Gson()
                for (type in res) {
                    val parsed = gson.fromJson(type.value, Array<ResponseJsonGroup>::class.java)
                    if (!timetableUrls.containsKey(type.key))
                        timetableUrls[type.key!!] = HashMap()
                    for (entity in parsed)
                        timetableUrls[type.key]!![entity.name] = type.key + "/" + entity.id
                }
                if (timetableUrls.isNotEmpty())
                    WriteFileTask(WeakReference(it), fileName, gson.toJson(timetableUrls).toByteArray()).run()
                return@AsyncLambda
            }.execute()
        }
    }

    fun startNewTimetableActivity(name: String, timetableUrl: String) {
        val loadTimetableTask = AsyncLambda(WeakReference(applicationContext)) { cntxt ->
            val htmlPage = GetTimetableTask(WeakReference(cntxt)).run(
                    GetTimetableTask.ARG_GET_TIMETABLE_HTML_PAGE, timetableUrl)

            if (htmlPage.containsKey(null)) {
                val json = parseTimetable(htmlPage[null]!!)
                return@AsyncLambda Gson().fromJson(json, ParsedTimetable::class.java)
            } else {
                return@AsyncLambda null
            }
        }
        loadTimetableTask.execute()

        suprBundle.put(AsyncLambda.ARG_ASYNC_TIMETABLE_LOADER, loadTimetableTask)

        val intent = Intent(this, TimetableActivity::class.java)
        startActivity(intent)
    }

    fun findUrlPostfixes(type: String, name: String): Map<String, String>? {
        if (timetableUrls.isEmpty()) {
            Toast.makeText(applicationContext, R.string.unnable_to_load_urls, Toast.LENGTH_SHORT).show()
            return null
        }
        return timetableUrls[type]?.filterKeys {
            entry -> entry.startsWith(name, true)
        } ?: HashMap()
    }

    private var timetableUrls = HashMap<String, HashMap<String, String>>()



    class UnsafeSuperBundle {
        private val map = HashMap<String, Any>()

        fun <T: Any> put(key: String, value: T) {
            map[key] = value
        }

        fun <T: Any> take(key: String): T? {
            return map[key] as T
        }
    }

    val suprBundle = UnsafeSuperBundle()

    // Jni
    private external fun parseTimetable(htmlPage: String): String

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

}
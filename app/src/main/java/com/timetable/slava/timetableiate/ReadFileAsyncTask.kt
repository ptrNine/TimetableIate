package com.timetable.slava.timetableiate

import android.content.Context
import android.os.AsyncTask
import java.io.File
import java.lang.ref.WeakReference

class ReadFileTask(
        private val filePath: String,
        private val applicationContext: WeakReference<Context>
) {
    fun run(): String {
        var resultData = ""

        val context = applicationContext.get()
        if (context != null) {
            val file = File(context.filesDir, filePath)

            if (file.exists()) {
                val openFile = context.openFileInput(filePath).bufferedReader()
                resultData = openFile.readText()
            }
        }
        return resultData
    }
}

class ReadFileAsyncTask(
        private val filePath: String,
        private val applicationContext: WeakReference<Context>
) : AsyncTask<Void, Void, String>() {

    override fun doInBackground(vararg params: Void?): String {
        return ReadFileTask(filePath, applicationContext).run()
    }
}
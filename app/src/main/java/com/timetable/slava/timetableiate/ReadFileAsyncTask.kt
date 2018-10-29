package com.timetable.slava.timetableiate

import android.content.Context
import android.os.AsyncTask
import java.io.File
import java.lang.ref.WeakReference

class ReadFileTask(
        private val applicationContext: WeakReference<Context>,
        private val filePath: String
) {
    fun run(): String {
        var resultData = ""

        val context = applicationContext.get()
        if (context != null) {
            val file = File(context.filesDir, filePath)

            if (file.exists()) {
                val inputFile = context.openFileInput(filePath).bufferedReader()
                resultData = inputFile.readText()
                inputFile.close()
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
        return ReadFileTask(applicationContext, filePath).run()
    }
}
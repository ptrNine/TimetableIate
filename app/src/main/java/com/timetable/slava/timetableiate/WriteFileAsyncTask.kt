package com.timetable.slava.timetableiate

import android.content.Context
import android.os.AsyncTask
import com.google.gson.Gson
import java.lang.ref.WeakReference


class WriteFileTask<T>(
        private val applicationContext: WeakReference<Context>,
        private val filePath: String,
        private val obj: T
) {

    fun run() {
        val context = applicationContext.get()
        if (context != null) {
            val file = context.openFileOutput(filePath, Context.MODE_PRIVATE)
            file.write(Gson().toJson(obj).toByteArray())
        }
    }
}


class WriteFileAsyncTask<T>(
        private val filePath: String,
        private val applicationContext: WeakReference<Context>,
        private val obj: T
) : AsyncTask<Void, Void, Void?>() {

    override fun doInBackground(vararg params: Void?): Void? {
        WriteFileTask(applicationContext, filePath, obj).run()
        return null
    }
}
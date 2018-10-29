package com.timetable.slava.timetableiate

import android.content.Context
import android.os.AsyncTask
import com.google.gson.Gson
import java.lang.ref.WeakReference


class WriteFileTask(
        private val applicationContext: WeakReference<Context>,
        private val filePath: String,
        private val byteArray: ByteArray
) {

    fun run() {
        val context = applicationContext.get()
        if (context != null) {
            val file = context.openFileOutput(filePath, Context.MODE_PRIVATE)
            file.write(byteArray)
            file.close()
        }
    }
}


class WriteFileAsyncTask(
        private val filePath: String,
        private val applicationContext: WeakReference<Context>,
        private val byteArray: ByteArray
) : AsyncTask<Void, Void, Void?>() {

    override fun doInBackground(vararg params: Void?): Void? {
        WriteFileTask(applicationContext, filePath, byteArray).run()
        return null
    }
}
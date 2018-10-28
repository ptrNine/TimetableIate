package com.timetable.slava.timetableiate

import android.content.Context
import android.os.AsyncTask
import java.io.File
import java.lang.ref.WeakReference

class AsyncFile(
        private val context: Context,
        private val fileName: String
) {

    fun <T: Any> write(obj: T) {
        writeTask = WriteFileAsyncTask(fileName, WeakReference(context), obj)
        writeTask!!.execute()
    }

    fun <T: Any> read(obj: T) {
        readTask = JsonAssignmentAsyncTask(obj) { ReadFileTask(fileName, WeakReference(context)).run() }
        readTask!!.execute()
    }

    fun isReadCompleted() = if (readTask != null) readTask!!.status == AsyncTask.Status.FINISHED else false
    fun isWriteCompleted() = if (writeTask != null) writeTask!!.status == AsyncTask.Status.FINISHED else false

    fun waitRead() {
        while(!isReadCompleted()) {}
    }

    fun waitWrite() {
        while (!isWriteCompleted()) {}
    }

    fun exists() = File(context.filesDir, fileName).exists()

    enum class ActionTypes {
        NO_ACTION,
        ARG_ALL_TIMETABLE_URLS
    }

    private var readTask: AsyncTask<Void, Void, Void?>? = null
    private var writeTask: AsyncTask<Void, Void, Void?>? = null

    companion object {
        const val ARG_FILENAME_TIMETABLE_URLS = "timetable_urls.dat"
    }
}
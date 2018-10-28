package com.timetable.slava.timetableiate

import android.content.Context
import android.os.AsyncTask
import java.lang.ref.WeakReference

class AsyncLambda<T: Any?>(
        private val appContext: WeakReference<Context>,
        private val func: (Context) -> T
) : AsyncTask<Void, Void, T>()
{
    override fun doInBackground(vararg params: Void?): T? {
        val context = appContext.get()
        return if (context != null) func(context)
        else null
    }

    override fun onPostExecute(res: T) {
        result = res
        isComplete = true
        super.onPostExecute(result)
    }

    var result: T? = null
    var isComplete: Boolean = false

    companion object {
        const val ARG_ASYNC_TIMETABLE_LOADER = "__async_timetable_loader"
    }
}
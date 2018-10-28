package com.timetable.slava.timetableiate

import android.os.AsyncTask
import com.google.gson.Gson
import java.lang.reflect.Type

class JsonAssignmentAsyncTask<L : Any>(
        var leftVar: L,
        val rightVal: () -> String
) : AsyncTask<Void, Void, Void?>() {
    override fun doInBackground(vararg params: Void?): Void? {
        leftVar = Gson().fromJson(rightVal(), leftVar::class.java)
        return null
    }
}

// gson can't recognize type in runtime
class JsonComplexMapAssignmentAsyncTask<M: Any, L: Any>(
        private var leftVar: HashMap<String, L>,
        private val rightVal: () -> Map<String?, String>,
        private val middleToLeft: (String, M) -> L,
        private val type: Type
) : AsyncTask<Void, Void, Void?>() {

    override fun doInBackground(vararg params: Void?): Void? {
        val map = rightVal()
        val gson = Gson()

        for (it in map) {
            if (it.key != null) {
                val middle: M = gson.fromJson(it.value, type)
                leftVar[it.key!!] = middleToLeft(it.key!!, middle)
            }
        }
        return null
    }
}


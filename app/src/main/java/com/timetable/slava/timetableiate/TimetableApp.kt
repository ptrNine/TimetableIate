package com.timetable.slava.timetableiate

import android.app.Application
import java.util.*

class TimetableApp : Application() {
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
}
package com.timetable.slava.timetableiate

class TimetableObj(
        val type: Int,
        val name: String,
        val items: LinkedHashMap<String, List<TimetableItem>>
)
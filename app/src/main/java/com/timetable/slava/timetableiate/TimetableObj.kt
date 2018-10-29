package com.timetable.slava.timetableiate

class TimetableItem(
        val topClickableText: Map<String, String>,
        val bottomClickableText: Map<String, String>,
        val lesson: String,
        val lesson_time: String,
        val group_name: String,
        val lesson_type: String,
        val circle: Int,
        val isLast: Boolean
)

class TimetableObj(
        val type: Int,
        val name: String,
        val items: LinkedHashMap<String, List<TimetableItem>>
)
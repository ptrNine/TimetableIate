package com.timetable.slava.timetableiate

class ParsedTimetable (
    val name: String,
    val data: Array<ParsedDay>
) {

    class ParsedParamPair(
        val reference: String,
        val name: String
    )

    class ParsedLesson(
        val lesson_name: String,
        val lesson_type: String,
        val lesson_time: String,
        val data_circle: String,
        val parameter1: Array<ParsedParamPair>,
        val parameter2: Array<ParsedParamPair>
    )

    class ParsedDay(
        val day_name: String,
        val data: Array<ParsedLesson>
    )
}
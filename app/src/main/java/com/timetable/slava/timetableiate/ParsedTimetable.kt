package com.timetable.slava.timetableiate

class ParsedTimetable(
        val name: String,
        val data: Array<ParsedDay>
) {

    class ParsedParamPair (
            val reference: String,
            val name: String
    )

    class ParsedLesson(
            val name: String,
            val type: String,
            val time: String,
            val parity: String,
            val parameter1: Array<ParsedParamPair>,
            val parameter2: Array<ParsedParamPair>
    )

    class ParsedDay(
            val name: String,
            val data: Array<ParsedLesson>
    )
}
package com.timetable.slava.timetableiate

import com.google.gson.annotations.SerializedName

class ResponseJsonGroup (
    val id: Int = 0,
    @SerializedName(value = "name", alternate = ["fio", "room_num"])
    val name: String = "",
    val course: Int = 0
)
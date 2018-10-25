package com.timetable.slava.timetableiate

import android.os.AsyncTask
import com.google.gson.Gson
import khttp.structures.cookie.CookieJar


class GetTimetableAsyncTask : AsyncTask<String, Void, ArrayList<String>>() {


    override fun doInBackground(vararg params: String?): ArrayList<String> {
        val returnArray = ArrayList<String>()

        if (cookies == null) {
            getRequest(ARG_HTTP_START_PAGE)
        }

        when {
            params[0] == ARG_GET_JSON_TIMETABLE_URLS -> // params[1] - name, params[2] - type
                returnArray.add(postRequestWithFormData(ARG_HTTP_START_PAGE, params[1], params[2]))
            params[0] == ARG_GET_JSON_ALL_TIMETABLE_URLS -> {
                returnArray.add(postRequestWithFormData(ARG_HTTP_START_PAGE, "", ARG_REQUEST_TYPE_GROUP))
                returnArray.add(postRequestWithFormData(ARG_HTTP_START_PAGE, "", ARG_REQUEST_TYPE_ROOM))
                returnArray.add(postRequestWithFormData(ARG_HTTP_START_PAGE, "", ARG_REQUEST_TYPE_TEACHER))
                taskType = 1
            }
            params[0] == ARG_GET_TIMETABLE_HTML_PAGE -> // params[1] - url postfix
                returnArray.add(getRequest(ARG_HTTP_START_PAGE + params[1]))
        }

        return returnArray
    }

    private fun getRequest(url: String): String {
        val response = khttp.get(
                url = url,
                cookies = cookies
        )

        csrfToken = Pair(ARG_FORM_CSRF_TOKEN, getCsrfToken(response.text))
        cookies = response.cookies

        return response.text
    }

    private fun postRequestWithFormData(url: String, name: String?, type: String?): String {
        val response = khttp.post(
                url = url,
                headers = mapOf(csrfToken!!),
                data = mapOf("searchName" to name, "searchType" to type),
                cookies = cookies
        )

        cookies = response.cookies

        return response.text
    }

    override fun onPostExecute(result: ArrayList<String>) {

        if (taskType == 1) {
            val gson = Gson()
            val groups = gson.fromJson(result[0], Array<ResponseJsonGroup>::class.java)
            val rooms = gson.fromJson(result[1], Array<ResponseJsonGroup>::class.java)
            val teachers = gson.fromJson(result[2], Array<ResponseJsonGroup>::class.java)

            MainActivity.timetableUrls.clear()

            MainActivity.timetableUrls[ARG_REQUEST_TYPE_GROUP] = HashMap()
            MainActivity.timetableUrls[ARG_REQUEST_TYPE_ROOM] = HashMap()
            MainActivity.timetableUrls[ARG_REQUEST_TYPE_TEACHER] = HashMap()

            groups.forEach { it -> MainActivity.timetableUrls[ARG_REQUEST_TYPE_GROUP]!![it.name] =
                    ARG_REQUEST_TYPE_GROUP + "/" + it.id.toString() }
            rooms.forEach { it -> MainActivity.timetableUrls[ARG_REQUEST_TYPE_ROOM]!![it.name] =
                    ARG_REQUEST_TYPE_ROOM + "/" + it.id.toString() }
            teachers.forEach { it -> MainActivity.timetableUrls[ARG_REQUEST_TYPE_TEACHER]!![it.name] =
                    ARG_REQUEST_TYPE_TEACHER + "/" + it.id.toString() }
        }

        super.onPostExecute(result)
    }

    private external fun getCsrfToken(htmlFile: String): String


    // 1 - get all timetables
    var taskType = 0

    companion object {
        const val ARG_GET_JSON_TIMETABLE_URLS: String = "__get_json_timetable_urls"
        const val ARG_GET_TIMETABLE_HTML_PAGE: String = "__get_timetable_html_page"
        const val ARG_GET_JSON_ALL_TIMETABLE_URLS: String = "__get_json_all_timetable_urls"

        const val ARG_REQUEST_TYPE_GROUP = "group"
        const val ARG_REQUEST_TYPE_ROOM = "room"
        const val ARG_REQUEST_TYPE_TEACHER = "teacher"

        const val ARG_HTTP_START_PAGE = "http://timetable.iate.obninsk.ru/"
        const val ARG_FORM_CSRF_TOKEN: String = "x-csrf-token"
        private var cookies: CookieJar? = null
        private var csrfToken: Pair<String, String>? = null
    }
}
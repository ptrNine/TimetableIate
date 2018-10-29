package com.timetable.slava.timetableiate

import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.timetable.slava.timetableiate.MainActivity.Companion.ARG_ITEM_TYPE_GROUP
import com.timetable.slava.timetableiate.MainActivity.Companion.ARG_ITEM_TYPE_ROOM
import com.timetable.slava.timetableiate.MainActivity.Companion.ARG_ITEM_TYPE_TEACHER
import khttp.structures.cookie.CookieJar
import java.lang.Exception
import java.lang.ref.WeakReference

class GetTimetableTask(private val appContext: WeakReference<Context>) {
    fun run(vararg params: String?): HashMap<String?, String> {
        val returnMap = HashMap<String?, String>()
        try {

            if (cookies == null) {
                getRequest(ARG_HTTP_START_PAGE)
            }

            when {
                params[0] == ARG_GET_JSON_TIMETABLE_URLS -> // params[1] - name, params[2] - type
                    returnMap[null] = (postRequestWithFormData(ARG_HTTP_START_PAGE, params[1], params[2]))
                params[0] == ARG_GET_JSON_ALL_TIMETABLE_URLS -> {
                    returnMap[ARG_ITEM_TYPE_GROUP] = postRequestWithFormData(ARG_HTTP_START_PAGE, "", ARG_ITEM_TYPE_GROUP)
                    returnMap[ARG_ITEM_TYPE_ROOM] = postRequestWithFormData(ARG_HTTP_START_PAGE, "", ARG_ITEM_TYPE_ROOM)
                    returnMap[ARG_ITEM_TYPE_TEACHER] = postRequestWithFormData(ARG_HTTP_START_PAGE, "", ARG_ITEM_TYPE_TEACHER)
                }
                params[0] == ARG_GET_TIMETABLE_HTML_PAGE -> // params[1] - url postfix
                    returnMap[null] = getRequest(ARG_HTTP_START_PAGE + params[1])
            }
        } catch (e: Exception) {
            Log.e("GetTimetableTask", e.toString() + ":" + e.message.toString())
            val context = appContext.get()
            if (context != null) {
                val handler = Handler(context.mainLooper)
                val runnable = Runnable { Toast.makeText(context, "Can't connect to $ARG_HTTP_START_PAGE", Toast.LENGTH_LONG).show() }
                handler.post(runnable)
            }
        }

        return returnMap
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

    private external fun getCsrfToken(htmlFile: String): String

    companion object {
        const val ARG_GET_JSON_TIMETABLE_URLS: String = "__get_json_timetable_urls"
        const val ARG_GET_TIMETABLE_HTML_PAGE: String = "__get_timetable_html_page"
        const val ARG_GET_JSON_ALL_TIMETABLE_URLS: String = "__get_json_all_timetable_urls"


        const val ARG_HTTP_START_PAGE = "http://timetable.iate.obninsk.ru/"
        const val ARG_FORM_CSRF_TOKEN: String = "x-csrf-token"
        private var cookies: CookieJar? = null
        private var csrfToken: Pair<String, String>? = null
    }
}

class GetTimetableAsyncTask(
        private val appContext: WeakReference<Context>
) : AsyncTask<String, Void, HashMap<String?, String>>() {

    override fun doInBackground(vararg params: String?): HashMap<String?, String> {
        return GetTimetableTask(appContext).run(*params)
    }

    companion object {
        const val ARG_EXCEPTION_WAS = "__EXCEPTION"
    }
}
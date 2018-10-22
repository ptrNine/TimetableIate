package com.timetable.slava.timetableiate

import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    class GetTimetableAsyncTask() : AsyncTask<String, Void, HashMap<String, String>>() {
        private var requestFormCsrfToken: Pair<String, String>? = null
        private val requestCookie = HashMap<String, String>()

        override fun doInBackground(vararg params: String?): HashMap<String, String> {
            val responseHeaders = HashMap<String, String>()

            if (requestFormCsrfToken == null || requestCookie.isEmpty()) {
                val response = khttp.get(ARG_HTTP_START_PAGE)

                // write csrf token and cookies
                requestFormCsrfToken = Pair(ARG_FORM_CSRF_TOKEN, getCsrfToken(response.text))
                requestCookie[ARG_COOKIE_SSS] = response.cookies[ARG_COOKIE_SSS] ?: ""
                requestCookie[ARG_COOKIE_SESSION] = response.cookies[ARG_COOKIE_SESSION] ?: ""
                requestCookie[ARG_COOKIE_XSRF_TOKEN] = response.cookies[ARG_COOKIE_XSRF_TOKEN] ?: ""
            }

            if (params[0] == ARG_GET_JSON_TIMETABLES_DATA) {
                // post request
                // params[1] - name, params[2] - type

                val response = khttp.post(
                        url = ARG_HTTP_START_PAGE,
                        headers = mapOf(requestFormCsrfToken!!),
                        data = mapOf("searchName" to params[1], "searchType" to params[2]),
                        cookies = requestCookie
                )

                // save cookies
                responseHeaders.putAll(response.cookies)

                // save json array and last form csrf token
                responseHeaders[ARG_JSON_TIMETABLES_DATA] = response.jsonArray.toString()
                responseHeaders[ARG_FORM_CSRF_TOKEN] = requestFormCsrfToken!!.second
            }
            else if (params[0] == ARG_GET_TIMETABLE_HTML_PAGE) {
                // get request
                // params[1] - url postfix

                val response = khttp.get(
                        url = ARG_HTTP_START_PAGE + "/" + params[1],
                        cookies = requestCookie
                )
                // save html page
                responseHeaders[ARG_HTML_TIMETABLE] = response.text
                // save cookies
                responseHeaders.putAll(response.cookies)
                responseHeaders[ARG_FORM_CSRF_TOKEN] = getCsrfToken(response.text)
            }
            return responseHeaders
        }

        override fun onPreExecute() {
            val lastFormCsrfToken = lastResponseHeaders[ARG_FORM_CSRF_TOKEN]
            if (lastFormCsrfToken != null)
                requestFormCsrfToken = Pair("x-csrf-token", lastFormCsrfToken)


            val lastCookieSSS = lastResponseHeaders[ARG_COOKIE_SSS]
            val lastCookieSession = lastResponseHeaders[ARG_COOKIE_SESSION]
            val lastCookieXsrf = lastResponseHeaders[ARG_COOKIE_XSRF_TOKEN]

            if (lastCookieSSS != null)
                requestCookie[ARG_COOKIE_SSS] = lastCookieSSS
            if (lastCookieSession != null)
                requestCookie[ARG_COOKIE_SESSION] = lastCookieSession
            if (lastCookieXsrf != null)
                requestCookie[ARG_COOKIE_XSRF_TOKEN] = lastCookieXsrf
        }

        override fun onPostExecute(responseHeaders: HashMap<String, String>?) {
            super.onPostExecute(responseHeaders)
            if (responseHeaders != null)
                lastResponseHeaders = responseHeaders
        }

        private external fun getCsrfToken(htmlFile: String): String
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText = findViewById(R.id.teInput)
        rbGroup = findViewById(R.id.rbGroup)
        rbRoom = findViewById(R.id.rbRoom)
        rbTeacher = findViewById(R.id.rbTeacher)
        btnShow = findViewById(R.id.btnShow)

        btnShow!!.setOnClickListener {
            var type = ""

            if (rbGroup!!.isChecked)
                type = "group"
            else if (rbRoom!!.isChecked)
                type = "room"
            else if (rbTeacher!!.isChecked)
                type = "teacher"

            val name = editText!!.text.toString()

            val task = GetTimetableAsyncTask()
            val resp = task.execute(ARG_GET_JSON_TIMETABLES_DATA, name, type).get()

            val groups = Gson().fromJson(resp[ARG_JSON_TIMETABLES_DATA], Array<ResponseJsonGroup>::class.java)

            if (groups != null) {
                val task2 = GetTimetableAsyncTask()
                val resp2 = task2.execute(ARG_GET_TIMETABLE_HTML_PAGE, type + "/" + groups[0].id).get()

                val json = parseTimetable(resp2[ARG_HTML_TIMETABLE]!!)
                it.isClickable = false

                val intent = Intent(this, TimetableActivity::class.java)
                intent.putExtra("JSON_TIMETABLE", json)
                startActivity(intent)
            }

        }

    }

    override fun onStart() {
        super.onStart()
        btnShow!!.isClickable = true
    }


    var editText: EditText? = null
    var rbGroup: RadioButton? = null
    var rbRoom: RadioButton? = null
    var rbTeacher: RadioButton? = null
    var btnShow: Button? = null

    private external fun parseTimetable(htmlPage: String): String

    companion object {
        init {
            System.loadLibrary("native-lib")
        }

        var lastResponseHeaders: HashMap<String, String> = HashMap()

        const val ARG_FORM_CSRF_TOKEN: String = "x-csrf-token"
        const val ARG_HTML_TIMETABLE: String = "__timetable_html"
        const val ARG_JSON_TIMETABLES_DATA: String = "__json_data"
        const val ARG_GET_JSON_TIMETABLES_DATA: String = "__get_json_timetables_data"
        const val ARG_GET_TIMETABLE_HTML_PAGE: String = "__get_timetable_html_page"
        const val ARG_HTTP_START_PAGE = "http://timetable.iate.obninsk.ru"
        const val ARG_COOKIE_SSS = "SSS"
        const val ARG_COOKIE_SESSION = "iate_niyau_mifi_session"
        const val ARG_COOKIE_XSRF_TOKEN = "XSRF-TOKEN"
    }
}

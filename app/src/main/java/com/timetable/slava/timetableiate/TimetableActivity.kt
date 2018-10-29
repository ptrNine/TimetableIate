package com.timetable.slava.timetableiate

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import java.lang.ref.WeakReference

class TimetableActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.e("TimetableActivity", "Create new activity")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)

        val app = application as TimetableApp
        val processToken = intent.getStringExtra(null)!!
        val task: AsyncLambda<ParsedTimetable?>? = app.suprBundle.take(processToken)

        Log.e("TimetableActivity", "Creating Async Task...")
        AsyncLambda<Void?>(WeakReference(this)) {
            Log.e("TimetableActivity", "Run Async Task...")
            if (task != null) {
                while (!task.isComplete) {}
                Log.e("TimetableActivity", "AsyncTask: get timetable result...")
                val timetable = task.result
                Log.e("TimetableActivity", "AsyncTask: get timetable result: DONE")

                if (timetable != null) {
                    Log.e("TimetableActivity", "Handler: creating task in ui thread...")
                    val handler = Handler(it.mainLooper)
                    val runnable = Runnable {
                        Log.e("TimetableActivity", "Runnable: running...")
                        setTimetableData(timetable)
                        Log.e("TimetableActivity", "Runnable: DONE!")
                    }
                    handler.post(runnable)
                    Log.e("TimetableActivity", "Handler: task in ui thread created!")
                }
            }
            return@AsyncLambda null
        }.execute()
        Log.e("TimetableActivity", "Async Task Created")

        viewPager = findViewById(R.id.view_pager)
        pagerAdapter = TimetableFragmentPagerAdapter(supportFragmentManager)//, timetableObj)
        viewPager!!.adapter = pagerAdapter
        Log.e("TimetableActivity", "Activity created")
    }

    override fun onCreateView(name: String?, context: Context?, attrs: AttributeSet?): View? {
        return super.onCreateView(name, context, attrs)

    }

    class TimetableFragmentPagerAdapter(
            fm: FragmentManager,
            private var timetableObj: TimetableObj? = null
    ) : FragmentPagerAdapter(fm) {

        private var pageTitles: List<String>? = null //= timetableObj.items.keys.toList()

        override fun getItem(p0: Int): Fragment {
            val fragment = TimetableFragment.newInstance(p0)
            val args = Bundle()
            val items = timetableObj!!.items.values.toTypedArray()[p0]
            args.putSerializable(TimetableFragment.ARG_TIMETABLE_LIST, ArrayList<TimetableItem>(items))
            fragment.arguments = args

            return fragment
        }

        override fun getCount() = pageCount

        override fun getPageTitle(position: Int): CharSequence? {
            return pageTitles?.get(position)
        }

        fun setData(timetable: TimetableObj) {
            timetableObj = timetable
            pageCount = timetable.items.size
            pageTitles = ArrayList(timetable.items.keys)
            notifyDataSetChanged()
        }

        var pageCount = 0
    }

    fun setTimetableData(parsedTimetable: ParsedTimetable) {
        title = parsedTimetable.name
        val app = application as TimetableApp

        val linkedHashMap = LinkedHashMap<String, List<TimetableItem>>()
        for (parsedDay in parsedTimetable.data) {
            val day = ArrayList<TimetableItem>()
            var switcher = true

            var lessonNumber = 0
            val firstLessonTime = parsedDay.data[0].time

            if (firstLessonTime.length >= 13) {
                val endTime = firstLessonTime.substring(8, 13)

                val endH = endTime.substring(0, 2).toInt()
                val endM = endTime.substring(3, 5).toInt()
                val end = endH * 100 + endM

                lessonNumber = when (end) {
                    in 0..1100 -> 1
                    in 1100..1300 -> 2
                    in 1300..1520 -> 3
                    in 1520..1700 -> 4
                    in 1700..1830 -> 5
                    in 1830..2020 -> 6
                    in 2020..2150 -> 7
                    else -> 0
                }
            }

            // TODO: mark link as unclickable
            day.add(TimetableItem(
                    mapOf("" to ""),
                    mapOf("" to ""),
                    "К " + lessonNumber.toString() + " паре",
                    "",
                    parsedTimetable.name,
                    "",
                    6,
                    switcher))

            for (parsedLesson in parsedDay.data) {
                val param1 = HashMap<String, String>()
                val param2 = HashMap<String, String>()
                parsedLesson.parameter1.forEach { it -> param1[it.name] = it.reference; app.addUrl(it.name, it.reference) }
                parsedLesson.parameter2.forEach { it -> param2[it.name] = it.reference; app.addUrl(it.name, it.reference)  }

                if (parsedLesson.time.length > 1)
                    switcher = !switcher

                var circle = 0
                when (parsedLesson.parity) {
                    " up-circle " -> circle = 1
                    " down-circle " -> circle = 2
                }

                if (param1.contains("Кучерявый С.И.")) circle += 3

                day.add(TimetableItem(
                        param1,
                        param2,
                        parsedLesson.name,
                        parsedLesson.time,
                        parsedTimetable.name,
                        parsedLesson.type,
                        circle,
                        switcher
                ))
            }
            linkedHashMap[parsedDay.name] = day
        }
        page_cout = parsedTimetable.data.size

        val timetableObj = TimetableObj(0, "HUY", linkedHashMap)

        pagerAdapter!!.setData(timetableObj)
    }

    var viewPager: ViewPager? = null
    var pagerAdapter: TimetableFragmentPagerAdapter? = null

    var page_cout = 0
}

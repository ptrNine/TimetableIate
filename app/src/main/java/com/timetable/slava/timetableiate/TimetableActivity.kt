package com.timetable.slava.timetableiate

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import com.google.gson.Gson

class TimetableActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)

        val json = intent.getStringExtra("JSON_TIMETABLE")
        val parsedTimetable = Gson().fromJson(json, ParsedTimetable::class.java)

        title = parsedTimetable.name

        val linkedHashMap = LinkedHashMap<String, List<TimetableItem>>()
        for (parsedDay in parsedTimetable.data) {
            val day = ArrayList<TimetableItem>()
            var switcher = true

            var lessonNumber = 0
            val firstLessonTime = parsedDay.data[0].lesson_time

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

            day.add(TimetableItem(
                    "",
                    "К " + lessonNumber.toString() + " паре",
                    "",
                    "",
                    parsedTimetable.name,
                    "",
                    6,
                    switcher))

            for (parsedLesson in parsedDay.data) {
                var param1name = ""
                for (str in parsedLesson.parameter1)
                    param1name += str.name

                var param2name = ""
                for (str in parsedLesson.parameter2)
                    param2name += str.name

                if (parsedLesson.lesson_time.length > 1)
                    switcher = !switcher

                var circle = 0
                when (parsedLesson.data_circle) {
                    " up-circle " -> circle = 1
                    " down-circle " -> circle = 2
                }

                if (param1name == "Кучерявый С.И.")
                    circle += 3

                day.add(TimetableItem(
                        param1name,
                        parsedLesson.lesson_name,
                        param2name,
                        parsedLesson.lesson_time,
                        parsedTimetable.name,
                        parsedLesson.lesson_type,
                        circle,
                        switcher
                ))
            }
            linkedHashMap[parsedDay.day_name] = day
        }
        page_cout = parsedTimetable.data.size

        val timetableObj = TimetableObj(0, "HUY", linkedHashMap)

        viewPager = findViewById(R.id.view_pager)
        pagerAdapter = TimetableFragmentPagerAdapter(supportFragmentManager, timetableObj)
        viewPager!!.adapter = pagerAdapter



    }

    class TimetableFragmentPagerAdapter(
            fm: FragmentManager,
            private val timetableObj: TimetableObj
    ) : FragmentPagerAdapter(fm) {

        private var pageTitles = timetableObj.items.keys.toList()

        override fun getItem(p0: Int): Fragment {
            val fragment = TimetableFragment.newInstance(p0)
            val args = Bundle()
            val items = timetableObj.items.values.toTypedArray()[p0]
            args.putSerializable(TimetableFragment.ARG_TIMETABLE_LIST, ArrayList<TimetableItem>(items))
            fragment.arguments = args

            return fragment
        }

        override fun getCount(): Int {
            return page_cout;
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return pageTitles[position]
        }
    }



    var viewPager: ViewPager? = null
    var pagerAdapter: PagerAdapter? = null

    //private var timetableListView: ListView? = null
    //private var listAdapter: ArrayAdapter<ArrayList<TimetableItem>>? = null

    companion object {
        var page_cout = 0
    }
}

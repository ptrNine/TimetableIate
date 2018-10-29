package com.timetable.slava.timetableiate

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

class TimetableListAdapter(
        private val context: Context,
        private val timetableList: ArrayList<TimetableItem>,
        private val inflater: LayoutInflater = LayoutInflater.from(context)
) : BaseAdapter() {

    private class ViewHolder(val view: View) {
        val time: TextView = view.findViewById(R.id.textView13)
        val lessonName: TextView = view.findViewById(R.id.textView14)
        val lessonType: ImageView = view.findViewById(R.id.imageView)
        val textViewLt: TextView = view.findViewById(R.id.textViewLt)
        val layout: ConstraintLayout = view.findViewById(R.id.constraintL)
        val linearLayout1: LinearLayout = view.findViewById(R.id.linearLayout)
        val linearLayout2: LinearLayout = view.findViewById(R.id.linearLayout2)

        //val paramsMap = HashMap<String, String>()

        fun addClickableTextViews(linearLayout: LinearLayout, values: Map<String, String>) {
            val app = view.context.applicationContext as TimetableApp
            linearLayout.removeAllViewsInLayout()

            val outValue = TypedValue()
            view.context.theme.resolveAttribute(R.attr.selectableItemBackground, outValue, true)

            for (name in values.keys) {
                val textView = TextView(view.context)
                textView.text = name
                textView.setTypeface(textView.typeface, Typeface.ITALIC)
                @Suppress("DEPRECATION")
                textView.setTextColor(view.resources.getColor(R.color.colorPrimaryDark))
                textView.setBackgroundResource(outValue.resourceId)

                textView.setOnClickListener {
                    app.startNewTimetableActivity(app.getUrl(name)!!)
                }
                linearLayout.addView(textView)
            }
        }

        // TODO: при прокрутке меняется цвет фона строчек в таблице
        fun bind(item: TimetableItem) {
            addClickableTextViews(linearLayout1, item.topClickableText)
            addClickableTextViews(linearLayout2, item.bottomClickableText)

            time.text = item.lesson_time
            textViewLt.text = item.lesson_type
            lessonName.text = item.lesson

            when (item.circle) {
                1 -> lessonType.setImageResource(R.drawable.odd)
                2 -> lessonType.setImageResource(R.drawable.even)
                3 -> lessonType.setImageResource(R.drawable.bevery)
                4 -> lessonType.setImageResource(R.drawable.bodd)
                5 -> lessonType.setImageResource(R.drawable.beven)
                6 -> {
                    lessonType.visibility = View.INVISIBLE
                    layout.setBackgroundColor(Color.argb(255, 210, 210, 210))
                    lessonName.textSize = 16.0f
                }
            }

            if (item.isLast)
                layout.setBackgroundColor(Color.argb(255, 220, 220, 220))
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View?
        val viewHolder: ViewHolder?

        Log.e("position", position.toString())
        if (convertView == null) {
            Log.e("Info", "Creating new View...")
            view = inflater.inflate(R.layout.timatable_item_layout, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
            Log.e("Info", "new View created.")
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
            Log.e("Info", "Load ViewHolder")
        }

        viewHolder.bind(timetableList[position])
        return view!!
    }
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getItem(position: Int): Any = timetableList[position]
    override fun getCount() = timetableList.size
}
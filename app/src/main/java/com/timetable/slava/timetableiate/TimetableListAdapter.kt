package com.timetable.slava.timetableiate

import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.w3c.dom.Text

class TimetableListAdapter(
        private val context: Context,
        private val timetableList: ArrayList<TimetableItem>,
        private val inflater: LayoutInflater = LayoutInflater.from(context)
) : BaseAdapter() {

    companion object {
        private class ViewHolder(val view: View) {
            val textView1: TextView = view.findViewById(R.id.textView12)
            val textView2: TextView = view.findViewById(R.id.textView15)
            val time: TextView = view.findViewById(R.id.textView13)
            val lessonName: TextView = view.findViewById(R.id.textView14)
            val lessonType: ImageView = view.findViewById(R.id.imageView)
            val textViewLt: TextView = view.findViewById(R.id.textViewLt)
            val layout: ConstraintLayout = view.findViewById(R.id.constraintL)

            //val paramsMap = HashMap<String, String>()

            fun setClickableText(textView: TextView, layoutId: Int, values: Map<String, String>) {
                if (values.isNotEmpty()) {
                    val linearLayout: LinearLayout = view.findViewById(layoutId)
                    linearLayout.removeAllViewsInLayout()
                    linearLayout.addView(textView)

                    val names = ArrayList(values.keys)

                    textView.text = names[0]
                    //paramsMap.putAll(values)

                    if (values.size > 1) {
                        names.drop(0)


                        for (name in names) {
                            val textView = TextView(view.context)
                            textView.text = name
                            linearLayout.addView(textView)
                        }
                    }
                }
            }

            // TODO: создавать мапу ссылок на расписания в другом, общем классе
            fun bind(item: TimetableItem) {
                setClickableText(textView1, R.id.linearLayout, item.topClickableText)
                setClickableText(textView2, R.id.linearLayout2, item.bottomClickableText)

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
    }

    fun setItems(items: Collection<TimetableItem>) {
        timetableList.addAll(items)
        notifyDataSetChanged()
    }

    fun clearItems() {
        timetableList.clear()
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View?
        val viewHolder: ViewHolder?

        Log.e("position", position.toString())
        if (convertView == null) {
            view = inflater.inflate(R.layout.timatable_item_layout, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
            Log.e("Info", "Create new View")
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
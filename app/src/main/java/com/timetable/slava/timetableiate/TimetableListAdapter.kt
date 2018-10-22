package com.timetable.slava.timetableiate

import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.util.zip.Inflater

class TimetableListAdapter(
        private val context: Context,
        private val timetableList: ArrayList<TimetableItem>,
        private val inflater: LayoutInflater = LayoutInflater.from(context)
) : BaseAdapter() {

    companion object {
        private class ViewHolder(view: View) {
            val textView1 = view.findViewById<TextView>(R.id.textView12)
            val textView2 = view.findViewById<TextView>(R.id.textView15)
            val time = view.findViewById<TextView>(R.id.textView13)
            val lessonName = view.findViewById<TextView>(R.id.textView14)
            val lessonType = view.findViewById<ImageView>(R.id.imageView)
            val textViewLt = view.findViewById<TextView>(R.id.textViewLt)
            val layout = view.findViewById<ConstraintLayout>(R.id.constraintL)

            fun bind(item: TimetableItem) {
                textView1.text = item.prepod
                textView2.text = item.room_where
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

        if (convertView == null) {
            view = inflater.inflate(R.layout.timatable_item_layout, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        viewHolder.bind(timetableList[position])
        return view!!
    }
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getItem(position: Int): Any = timetableList[position]
    override fun getCount() = timetableList.size
}
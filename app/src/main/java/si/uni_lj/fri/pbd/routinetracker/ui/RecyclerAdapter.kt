package si.uni_lj.fri.pbd.routinetracker

import android.view.LayoutInflater
import android.view.ViewGroup
import android.database.Cursor
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import si.uni_lj.fri.pbd.routinetracker.databinding.CardLayoutBinding
import si.uni_lj.fri.pbd.routinetracker.ui.DatabaseHelper

// implemented viewBinding with RecyclerAdapter using this: https://stackoverflow.com/questions/60423596/how-to-use-viewbinding-in-a-recyclerview-adapter
// implemented onClickEvents in RecyclerView using this: https://www.quora.com/How-do-I-implement-onClick-event-in-activity-class-in-Recyclerview-in-android

class RecyclerAdapter(private var cursor: Cursor?, private val listener: OnItemClickListener, private val listener2: OnItemLongClickListener) : RecyclerView.Adapter<RecyclerAdapter.CardViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(routineId: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(routineId: Int)
    }

    inner class CardViewHolder(val binding: CardLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        // check if it would be better to perform onClick here, as in tutorial
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.CardViewHolder {
        val binding = CardLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun getItemCount(): Int {
        if (cursor != null) {
            return cursor!!.count
        } else {
            return 0
        }
    }

    // accessing data from cursor for a specific position
    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {

        if (cursor == null) {
            return
        }

        // move cursor to correct positon and get needed data
        if (!cursor!!.moveToPosition(position)) {
            return
        }
        val routine_id = cursor!!.getInt(cursor!!.getColumnIndexOrThrow(DatabaseHelper._ID))
        val routine_name = cursor!!.getString(cursor!!.getColumnIndexOrThrow(DatabaseHelper.ROUTINE_NAME))
        val routine_type = cursor!!.getString(cursor!!.getColumnIndexOrThrow(DatabaseHelper.ROUTINE_TYPE))
        val start_h = cursor!!.getInt(cursor!!.getColumnIndexOrThrow(DatabaseHelper.START_H))
        val start_m = cursor!!.getInt(cursor!!.getColumnIndexOrThrow(DatabaseHelper.START_M))
        val end_h = cursor!!.getInt(cursor!!.getColumnIndexOrThrow(DatabaseHelper.END_H))
        val end_m = cursor!!.getInt(cursor!!.getColumnIndexOrThrow(DatabaseHelper.END_M))
        val routine_days = cursor!!.getString(cursor!!.getColumnIndexOrThrow(DatabaseHelper.ROUTINE_DAYS))
        val notif = cursor!!.getInt(cursor!!.getColumnIndexOrThrow(DatabaseHelper.ROUTINE_NOTIF))

        // time formatting
        val start = String.format("%02d:%02d", start_h, start_m)
        val end = String.format("%02d:%02d", end_h, end_m)

        // bind
        holder.binding.routineName.text = routine_name
        holder.binding.routineType.text = routine_type
        holder.binding.routineTime.text = "$start - $end"
        holder.binding.routineDays.text = routine_days

        // place the onclick last so i have the routine id -> check if its better to call in CardViewHolder (but then i have to get id there so idk)
        holder.itemView.setOnClickListener {
            listener.onItemClick(routine_id)
        }

        holder.itemView.setOnLongClickListener {
            listener2.onItemLongClick(routine_id)
            true
        }
    }
}

package si.uni_lj.fri.pbd.routinetracker

import android.view.LayoutInflater
import android.view.ViewGroup
import android.database.Cursor
import androidx.recyclerview.widget.RecyclerView
import si.uni_lj.fri.pbd.routinetracker.data.entity.Routine
import si.uni_lj.fri.pbd.routinetracker.databinding.CardLayoutBinding
import si.uni_lj.fri.pbd.routinetracker.viewmodel.RoutineAndCompletion

// viewBinding with RecyclerAdapter: https://stackoverflow.com/questions/60423596/how-to-use-viewbinding-in-a-recyclerview-adapter
// onClickEvents in RecyclerView: https://www.quora.com/How-do-I-implement-onClick-event-in-activity-class-in-Recyclerview-in-android

class RecyclerAdapter(private var routines: List<RoutineAndCompletion>, private val listener: OnItemClickListener, private val listener2: OnItemLongClickListener) : RecyclerView.Adapter<RecyclerAdapter.CardViewHolder>() {

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
        return routines.size
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {

        val bundle = routines[position]
        val oneRoutine = bundle.routine

        // time formatting
        val start = String.format("%02d:%02d", oneRoutine.startH, oneRoutine.startM)
        val end = String.format("%02d:%02d", oneRoutine.endH, oneRoutine.endM)

        // bind (set card info)
        holder.binding.routineName.text = oneRoutine.name
        holder.binding.routineType.text = oneRoutine.type
        holder.binding.routineTime.text = "$start - $end"
        holder.binding.routineDays.text = oneRoutine.days

        // send completion info to the card
        if (bundle.completed == null) {
            holder.binding.routineCompleted.text = ""
        } else if (bundle.completed == true) {
            holder.binding.routineCompleted.text = "Completed"
        } else {
            holder.binding.routineCompleted.text = "Missed"
        }

        // place the onclick last so i have the routine id -> check if its better to call in CardViewHolder (but then i have to get id there so idk)
        holder.itemView.setOnClickListener {
            listener.onItemClick(oneRoutine.id)
        }

        holder.itemView.setOnLongClickListener {
            listener2.onItemLongClick(oneRoutine.id)
            true
        }
    }

    // update the list when we get new routines from livedata
    // see labs 7 for implementation logic
    fun setRoutineList(routines2: List<RoutineAndCompletion>) {
        routines = routines2
        notifyDataSetChanged()
    }

}

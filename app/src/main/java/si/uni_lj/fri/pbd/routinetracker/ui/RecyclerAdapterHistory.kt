package si.uni_lj.fri.pbd.routinetracker

import android.view.LayoutInflater
import android.view.ViewGroup
import android.database.Cursor
import androidx.recyclerview.widget.RecyclerView
import si.uni_lj.fri.pbd.routinetracker.data.entity.Routine
import si.uni_lj.fri.pbd.routinetracker.data.entity.RoutineExecution
import si.uni_lj.fri.pbd.routinetracker.databinding.CardLayoutBinding
import si.uni_lj.fri.pbd.routinetracker.databinding.ExecutionHistoryBinding
import si.uni_lj.fri.pbd.routinetracker.viewmodel.RoutineAndCompletion

// Logic here is the same as in rec. adapter1 only that its less complicated

class RecyclerAdapterHistory(private var routines: List<RoutineExecution>) : RecyclerView.Adapter<RecyclerAdapterHistory.CardViewHolder>() {

    inner class CardViewHolder(val binding: ExecutionHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapterHistory.CardViewHolder {
        val binding = ExecutionHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return routines.size
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {

        val ex = routines[position]
        holder.binding.date.text = ex.date

        if (ex.completed) {
            holder.binding.status.text = "Completed"
        } else {
            holder.binding.status.text = "Not completed"
        }
    }

    fun setExList(routines2: List<RoutineExecution>) {
        routines = routines2
        notifyDataSetChanged()
    }
}

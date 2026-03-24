package si.uni_lj.fri.pbd.routinetracker.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import si.uni_lj.fri.pbd.routinetracker.R
import si.uni_lj.fri.pbd.routinetracker.RecyclerAdapter
import si.uni_lj.fri.pbd.routinetracker.databinding.FragmentRoutineListBinding

class RoutineListFragment : Fragment() {

    private lateinit var binding: FragmentRoutineListBinding
    private var recyclerView : RecyclerView? = null
    private var layoutManager : RecyclerView.LayoutManager? = null
    private var adapter : RecyclerAdapter? = null
    private var databaseHelper : DatabaseHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRoutineListBinding.inflate(inflater, container, false)
        return binding.root

        //val view = inflater.inflate(R.layout.fragment_routine_list, container, false)
        //recyclerView = view.findViewById(R.id.recycler_view)
        //layoutManager = LinearLayoutManager(view.context)

        //recyclerView?.layoutManager = layoutManager
        //adapter = RecyclerAdapter()
        //recyclerView?.adapter = adapter

        //return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // db
        databaseHelper = DatabaseHelper(view.context)

        // recyclerview
        binding.recyclerView.layoutManager = LinearLayoutManager(view.context)
        adapter = RecyclerAdapter(null)
        binding.recyclerView.adapter = adapter

        loadRoutines()

        // setup floating action button
        binding.addRoutine.setOnClickListener {
            findNavController().navigate(R.id.action_routineListFragment_to_addEditRoutineFragment)
        }

        //longPressDelete()
    }

    private fun loadRoutines() {
        val cursor = databaseHelper?.readAllRoutines()
        adapter?.swapCursor(cursor)
    }

    private fun longPressDelete() {
    }

    override fun onResume() {
        super.onResume()
        loadRoutines()
    }

    override fun onDestroy() {
        super.onDestroy()
        databaseHelper = null
    }

}
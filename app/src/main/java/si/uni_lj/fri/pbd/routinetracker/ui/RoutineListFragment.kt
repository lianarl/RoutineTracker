package si.uni_lj.fri.pbd.routinetracker.ui

import android.app.AlertDialog
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

class RoutineListFragment : Fragment(), RecyclerAdapter.OnItemClickListener, RecyclerAdapter.OnItemLongClickListener {

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

        // database setup
        databaseHelper = DatabaseHelper(requireContext())

        // recyclerview setup
        binding.recyclerView.layoutManager = LinearLayoutManager(binding.root.context)
        loadRoutines()

        //adapter = RecyclerAdapter(null, this, this)
        //binding.recyclerView.adapter = adapter

        // button for adding routines
        binding.addRoutine.setOnClickListener {

            // got to addEdit
            findNavController().navigate(R.id.action_routineListFragment_to_addEditRoutineFragment)
        }

        return binding.root
    }

    // handling clicking the card for details
    override fun onItemClick(routineId: Int) {
        // put id in a bundle and pass it to details fragment
        val passId = Bundle()
        passId.putInt("routineId", routineId)
        findNavController().navigate(R.id.action_routineListFragment_to_routineDetailsFragment, passId)
    }

    // alertDialog: https://developer.android.com/develop/ui/views/components/dialogs
    override fun onItemLongClick(routineId: Int) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage("Do you want to delete this routine?")
            .setPositiveButton("Yes") { dialog, id ->
                databaseHelper?.deleteRoutine(routineId)
                loadRoutines()
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun loadRoutines() {
        val cursor = databaseHelper?.readAllRoutines()
        adapter = RecyclerAdapter(cursor, this, this)
        binding.recyclerView.adapter = adapter
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
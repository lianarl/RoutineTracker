package si.uni_lj.fri.pbd.routinetracker.ui

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import si.uni_lj.fri.pbd.routinetracker.R
import si.uni_lj.fri.pbd.routinetracker.RecyclerAdapter
import si.uni_lj.fri.pbd.routinetracker.databinding.FragmentRoutineListBinding
import si.uni_lj.fri.pbd.routinetracker.repository.RoutineRepository
import si.uni_lj.fri.pbd.routinetracker.util.RoutineEvaluationWorker
import si.uni_lj.fri.pbd.routinetracker.viewmodel.RoutineListViewModel
import si.uni_lj.fri.pbd.routinetracker.viewmodel.RoutineListViewModelFactory
import kotlin.collections.emptyList

// check lecture example for Worker + slides
// for periodic request check https://stackoverflow.com/questions/50363541/schedule-a-work-on-a-specific-time-with-workmanager

class RoutineListFragment : Fragment(), RecyclerAdapter.OnItemClickListener, RecyclerAdapter.OnItemLongClickListener {
    private lateinit var binding: FragmentRoutineListBinding
    private var adapter : RecyclerAdapter? = null

    // viewModel instantiation -> it stores the data put in the xml field and enables to fetch data
    private lateinit var viewModel: RoutineListViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRoutineListBinding.inflate(inflater, container, false)

        // viewModel setup
        val repository = RoutineRepository.getInstance(requireContext())
        val factory = RoutineListViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[RoutineListViewModel::class.java]

        // change the RecyclerView and Adapter setup to this from labs, because it does it better
        recyclerSetup()
        // observer setup (also from labs implementation)
        observerSetup()

        // button for adding routines
        binding.addRoutine.setOnClickListener {
            // got to addEdit
            findNavController().navigate(R.id.action_routineListFragment_to_addEditRoutineFragment)
        }

        val workManager = WorkManager.getInstance(requireContext())

        // button for running a worker
        binding.runWorker.setOnClickListener {
            // one time worker
            val request = OneTimeWorkRequest.Builder(RoutineEvaluationWorker::class.java).build()

            // enqueue request like in example
            workManager.enqueue(request)
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

    // long click functionality -> aler    // long click functionality -> alertDialog: https://developer.android.com/develop/ui/views/components/dialogstDialog: https://developer.android.com/develop/ui/views/components/dialogs
    override fun onItemLongClick(routineId: Int) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage("Do you want to delete this routine?")
            .setPositiveButton("Yes") { dialog, id ->
                viewModel.deleteRoutine(routineId)
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun recyclerSetup() {
        adapter = RecyclerAdapter(emptyList(), this, this)
        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    // setup one observer per LiveData
    private fun observerSetup() {
        viewModel.allRoutines
            .observe(viewLifecycleOwner) { routines ->
                viewModel.getCompletionInfo(routines)
            }

        viewModel.routinesWithCompletion
            .observe(viewLifecycleOwner) { routinesAndComp ->
                adapter?.setRoutineList(routinesAndComp)
            }
    }
}
package si.uni_lj.fri.pbd.routinetracker.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import si.uni_lj.fri.pbd.routinetracker.R
import si.uni_lj.fri.pbd.routinetracker.RecyclerAdapter
import si.uni_lj.fri.pbd.routinetracker.RecyclerAdapterHistory
import si.uni_lj.fri.pbd.routinetracker.data.entity.Routine
import si.uni_lj.fri.pbd.routinetracker.databinding.FragmentRoutineDetailsBinding
import si.uni_lj.fri.pbd.routinetracker.repository.RoutineRepository
import si.uni_lj.fri.pbd.routinetracker.viewmodel.RoutineDetailViewModel
import si.uni_lj.fri.pbd.routinetracker.viewmodel.RoutineDetailViewModelFactory
import si.uni_lj.fri.pbd.routinetracker.viewmodel.RoutineListViewModel
import si.uni_lj.fri.pbd.routinetracker.viewmodel.RoutineListViewModelFactory

// this is just a simplification of the AddEditRoutine (+ some small adjustments to the xml) -> So just the edit part where we populate the fields
// + the edit and delete ofcourse

class RoutineDetailsFragment : Fragment() {
    private lateinit var binding: FragmentRoutineDetailsBinding
    private lateinit var viewModel: RoutineDetailViewModel

    private var adapter: RecyclerAdapterHistory? = null

    // times placeholder (i fill them in later)
    private var startH = 0
    private var startM = 0
    private var endH = 0
    private var endM = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRoutineDetailsBinding.inflate(inflater, container, false)

        // viewModel setup
        val repository = RoutineRepository.getInstance(requireContext())
        val factory = RoutineDetailViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[RoutineDetailViewModel::class.java]

        val id = arguments?.getInt("routineId")

        // setup for new recycler
        recyclerSetup(id)
        observerSetup(id)

        // handle edit and delete
        binding.detailsEdit.setOnClickListener {
            var passId = Bundle()
            passId.putInt("routineId", id!!)

            // go to edit and pass id via bundle
            findNavController().navigate(R.id.action_routineDetailsFragment_to_addEditRoutineFragment, passId)
        }

        binding.detailsDelete.setOnClickListener {
            // delete the routine
            if (id != null) { // ugly but okay
                viewModel.deleteRoutine(id)
            }
            findNavController().navigateUp() // go home
        }

        return binding.root
    }

    // now for MVVM i fill the UI from the actual Routine instead of id
    private fun fillIn(routine: Routine) {

        binding.detailsName.setText(routine.name)

        // fill time
        startH = routine.startH
        startM = routine.startM
        endH = routine.endH
        endM = routine.endM
        binding.detailsTimeStart.setText("%02d:%02d".format(startH, startM))
        binding.detailsTimeEnd.setText("%02d:%02d".format(endH, endM))

        // fill days
        var routine_days = " "
        if (!routine.days!!.isEmpty()) {
            routine_days = routine.days!!
        }

        // build the string so i can just put it as one textview
        var days = ""
        if (routine_days.contains("Mon")) {
            days = days + "Mon "
        }
        if (routine_days.contains("Tue")) {
            days = days + "Tue "
        }
        if (routine_days.contains("Wed")) {
            days = days + "Wed "
        }
        if (routine_days.contains("Thu")) {
            days = days + "Thu "
        }
        if (routine_days.contains("Fri")) {
            days = days + "Fri "
        }
        if (routine_days.contains("Sat")) {
            days = days + "Sat "
        }
        if (routine_days.contains("Sun")) {
            days = days + "Sun"
        }
        binding.detailsDays.setText(days)

        binding.detailsType.setText(routine.type)

        // notif status
        var notif = routine.notif
        if (notif == 1) {
            val y = "Yes" // ugly but its an easy way to stop the error
            binding.detailsNotif.setText(y)
        } else {
            val n = "No"
            binding.detailsNotif.setText(n)
        }
    }

    private fun observerSetup(id: Int?) {
        if (id != null) {
            viewModel.getRoutineById(id).observe(viewLifecycleOwner) { routine ->
                if (routine != null) {  // a check so it doesnt force me to use !!
                    fillIn(routine)
                }
            }
        }
    }

    // template from the first recycler + observer setup
    private fun recyclerSetup(id: Int?) {
        adapter = RecyclerAdapterHistory(emptyList())
        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        if (id != null) {
            viewModel.getExes(id).observe(viewLifecycleOwner) { exs ->
                adapter?.setExList(exs)
            }
        }
    }
}
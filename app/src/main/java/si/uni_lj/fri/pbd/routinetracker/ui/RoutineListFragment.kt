package si.uni_lj.fri.pbd.routinetracker.ui

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.work.OneTimeWorkRequest
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import si.uni_lj.fri.pbd.routinetracker.R
import kotlin.collections.emptyList
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.work.WorkManager
import si.uni_lj.fri.pbd.routinetracker.repository.RoutineRepository
import si.uni_lj.fri.pbd.routinetracker.ui.theme.JetpackComposeTheme
import si.uni_lj.fri.pbd.routinetracker.util.RoutineEvaluationWorker
import si.uni_lj.fri.pbd.routinetracker.viewmodel.RoutineAndCompletion
import si.uni_lj.fri.pbd.routinetracker.viewmodel.RoutineListViewModel
import si.uni_lj.fri.pbd.routinetracker.viewmodel.RoutineListViewModelFactory
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// check lecture example for Worker + slides
// for periodic request check https://stackoverflow.com/questions/50363541/schedule-a-work-on-a-specific-time-with-workmanager

// compose implementation:
// - https://bugfender.com/blog/jetpack-compose-fragments/
// - https://developer.android.com/codelabs/jetpack-compose-migration#0

class RoutineListFragment : Fragment() {

    // VM init. -> Stores the data put in the xml field and enables to fetch data (for more see lecture 12)
    private lateinit var viewModel: RoutineListViewModel

    private var routinesList = mutableStateOf<List<RoutineAndCompletion>>(emptyList())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // viewModel setup
        val repository = RoutineRepository.getInstance(requireContext())
        val factory = RoutineListViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[RoutineListViewModel::class.java]

        // observer setup (from labs implementation) -> data goes to mutableStateOf now that adapter is no longer a thing (see routinesList init.)
        observerSetup()

        // instead of returning binding.root
        return ComposeView(requireContext()).apply {
            setContent {
                JetpackComposeTheme() { RoutineList(routinesList.value) }
            }
        }
    }

    // setup one observer per LiveData
    // setting routinesList.value so it triggers Compose
    private fun observerSetup() {
        viewModel.allRoutines
            .observe(viewLifecycleOwner) { routines ->
                viewModel.getCompletionInfo(routines)
            }

        viewModel.routinesWithCompletion
            .observe(viewLifecycleOwner) { routinesAndComp ->
                routinesList.value = routinesAndComp
            }
    }

    // handling clicking the card for details
    private fun onItemClick(routineId: Int) {
        // put id in a bundle and pass it to details fragment
        val passId = Bundle()
        passId.putInt("routineId", routineId)
        findNavController().navigate(R.id.action_routineListFragment_to_routineDetailsFragment, passId)
    }

    // long click functionality -> alertDialog: https://developer.android.com/develop/ui/views/components/dialogstDialog
    // https://developer.android.com/develop/ui/views/components/dialogs
    private fun onItemLongClick(routineId: Int) {
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

    // composable for the card of a routine
    @Composable
    fun RoutineCard(routineAndComp: RoutineAndCompletion) {
        val routine = routineAndComp.routine
        val start = String.format("%02d:%02d", routine.startH, routine.startM)
        val end = String.format("%02d:%02d", routine.endH, routine.endM)


        // combinedClickable handles tap AND long press
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(onClick = { onItemClick(routine.id) }, onLongClick = { onItemLongClick(routine.id) })
            ,shape = RoundedCornerShape(12.dp)
            ,colors = CardDefaults.cardColors(containerColor = Color(0xFF3B9BFF))) {

            // routine info
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = routine.name ?: "", fontWeight = FontWeight.Bold, color = Color(0xFFFFFFFF), fontSize = 20.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "${routine.type ?: ""}  $start - $end")
                Text(text = routine.days ?: "")

                if (routineAndComp.completed == true) {
                    Text(text = "Completed", color = Color(0xFF90EE90))
                } else if (routineAndComp.completed == false){
                    Text(text = "Missed", color = Color(0xFF90EE90))
                }
            }
        }
    }

    // composable for the list of routines
    @Composable
    fun RoutineList(routines: List<RoutineAndCompletion>) {

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(100.dp))

            // title
            Text(text = "Routines", fontSize = 30.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 6.dp))

            // list
            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(routines) { routineAndComp ->
                    RoutineCard(routineAndComp)
                }
            }

            // buttons
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 42.dp), horizontalArrangement = Arrangement.Center) {
                FloatingActionButton(onClick = {
                    val request = OneTimeWorkRequest.Builder(RoutineEvaluationWorker::class.java).build()
                    val workManager = WorkManager.getInstance(requireContext())
                    workManager.enqueue(request)
                }, containerColor = Color(0xFF3B9BFF), contentColor = Color(0xFFFFFFFF)) {
                    Icon(painter = painterResource(R.drawable.baseline_autorenew_24), contentDescription = "Run Worker")
                }

                Spacer(modifier = Modifier.padding(25.dp))

                FloatingActionButton(onClick = {
                    findNavController().navigate(R.id.action_routineListFragment_to_addEditRoutineFragment)
                }, containerColor = Color(0xFF3B9BFF), contentColor = Color(0xFFFFFFFF)) {
                    Icon(painter = painterResource(R.drawable.round_add_24), contentDescription = "Add Routine")
                }
            }
        }
    }
}
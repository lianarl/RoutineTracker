package si.uni_lj.fri.pbd.routinetracker.ui

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import kotlinx.coroutines.launch
import si.uni_lj.fri.pbd.routinetracker.data.entity.Routine
import si.uni_lj.fri.pbd.routinetracker.databinding.FragmentAddEditRoutineBinding
import si.uni_lj.fri.pbd.routinetracker.repository.RoutineRepository
import si.uni_lj.fri.pbd.routinetracker.viewmodel.RoutineDetailViewModel
import si.uni_lj.fri.pbd.routinetracker.viewmodel.RoutineDetailViewModelFactory

class AddEditRoutineFragment : Fragment() {
    private lateinit var binding: FragmentAddEditRoutineBinding
    private lateinit var viewModel: RoutineDetailViewModel // will use the same one for details and addedit (as they are quite similar)

    // times declarations (i fill them in later)
    private var startH = 0
    private var startM = 0
    private var endH = 0
    private var endM = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddEditRoutineBinding.inflate(inflater, container, false)

        // viewModel setup
        val repository = RoutineRepository.getInstance(requireContext())
        val factory = RoutineDetailViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[RoutineDetailViewModel::class.java]

        // ill just do it the same way it was done in the labs
        listenerSetup()
        enableChooseType()

        // check if we edit or add
        val id = arguments?.getInt("routineId")
        observerSetup(id)

        return binding.root
    }

    // setup of the spinner
    // src: https://developer.android.com/develop/ui/views/components/spinner
    private fun enableChooseType() {
        val types = arrayOf("Study", "Exercise", "Socialize")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.addeditType.adapter = adapter
    }

    private fun listenerSetup() {
        binding.addeditSave.setOnClickListener {
            saveRoutine()
        }
    }

    private fun saveRoutine() {
        val routineName = binding.addeditName.text.toString()

        if (routineName.isEmpty()) {
            Toast.makeText(requireContext(), "A routine must have a name!", Toast.LENGTH_SHORT).show()
            return
        }

        val routineType = binding.addeditType.selectedItem.toString()

        // time handling
        val start = binding.addeditStart.text.toString().split(":")
        val end = binding.addeditEnd.text.toString().split(":")

        // make sure the format is correct -> so :
        if (start.size != 2 || end.size != 2) {
            Toast.makeText(requireContext(), "Im sorry, but you have to enter in format HH:MM", Toast.LENGTH_SHORT).show()
            return
        }

        startH = start[0].toInt()
        startM = start[1].toInt()
        endH = end[0].toInt()
        endM = end[1].toInt()

        // handle illogical hours and minutes
        if (startH > 23 || startM > 59 || endH > 23 || endM > 59) {
            Toast.makeText(requireContext(), "Time set is not possible!", Toast.LENGTH_SHORT).show()
            return
        }

         // check whether the end is after start
        val startTime = startH * 60 + startM
        val endTime = endH * 60 + endM
        if (endTime <= startTime) {
            Toast.makeText(requireContext(), "Time range is not possible!", Toast.LENGTH_SHORT).show()
            return
        }

        // days handling
        var routineDays = ""
        if (binding.cbMon.isChecked) {
            routineDays = routineDays + "Mon,"
        }
        if (binding.cbTue.isChecked) {
            routineDays = routineDays + "Tue,"
        }
        if (binding.cbWed.isChecked) {
            routineDays = routineDays + "Wed,"
        }
        if (binding.cbThu.isChecked) {
            routineDays = routineDays + "Thu,"
        }
        if (binding.cbFri.isChecked) {
            routineDays = routineDays + "Fri,"
        }
        if (binding.cbSat.isChecked) {
            routineDays = routineDays + "Sat,"
        }
        if (binding.cbSun.isChecked) {
            routineDays = routineDays + "Sun,"
        }
        if (routineDays.endsWith(",")) {
            routineDays = routineDays.dropLast(1)
        }

        // check if at least one day was selected
        if (routineDays.isEmpty()) {
            Toast.makeText(requireContext(), "You forgot to select a day!", Toast.LENGTH_SHORT).show()
            return
        }

        // notif handling
        var notif = 0
        if (binding.addeditNotif.isChecked) {
            notif = 1
        }

        // build the routine because now we need the object for insertRoutine
        val routine = Routine()
        routine.name = routineName
        routine.type = routineType
        routine.startH = startH
        routine.startM = startM
        routine.endH = endH
        routine.endM = endM
        routine.days = routineDays
        routine.notif = notif

        // check if the routine exists and appropriately handle
        val id = arguments?.getInt("routineId")


// from the lectures couroutine example i use lifecycleScope
        lifecycleScope.launch {
            if (id != null) {
                // here i update (by id)
                routine.id = id
                viewModel.updateRoutine(routine)
                if (notif == 1) {
                    alarm(id, routineName, startH, startM)
                }
            } else {
                // here i create a new routine
                val new = viewModel.insertRoutine(routine)
                if (notif == 1) {
                    alarm(new.toInt(), routineName, startH, startM)
                }
            }
            findNavController().navigateUp() // go home
        }
    }

    // function that fills the info of already created routines when editing them
    private fun fillIn(routine: Routine) {

        binding.addeditName.setText(routine.name)

        // find position of the type and set the spinner to correct position (of that type)
        val type = routine.type
        val adapter = binding.addeditType.adapter as ArrayAdapter<String> // shortcut so i dont have to loop through the types
        val position = adapter.getPosition(type)
        binding.addeditType.setSelection(position)

        // fill time
        startH = routine.startH
        startM = routine.startM
        endH = routine.endH
        endM = routine.endM

        binding.addeditStart.setText("%02d:%02d".format(startH, startM))
        binding.addeditEnd.setText("%02d:%02d".format(endH, endM))

        // fill days
        var routine_days = ""
        if (!routine.days.isNullOrBlank()) {
            routine_days = routine.days!!
        }

        if (routine_days.contains("Mon")) {
            binding.cbMon.isChecked = true
        }
        if (routine_days.contains("Tue")) {
            binding.cbTue.isChecked = true
        }
        if (routine_days.contains("Wed")) {
            binding.cbWed.isChecked = true
        }
        if (routine_days.contains("Thu")) {
            binding.cbThu.isChecked = true
        }
        if (routine_days.contains("Fri")) {
            binding.cbFri.isChecked = true
        }
        if (routine_days.contains("Sat")) {
            binding.cbSat.isChecked = true
        }
        if (routine_days.contains("Sun")) {
            binding.cbSun.isChecked = true
        }

        // fill notification
        val notif = routine.notif
        if (notif == 1) {
            binding.addeditNotif.isChecked = true
        } else {
            binding.addeditNotif.isChecked = false
        }
    }

    // function for notification scheduling
    // src: https://developer.android.com/reference/android/app/AlarmManager, https://mubaraknative.medium.com/creating-a-alarm-using-alarmmanager-in-android-e27a4283d39f
    @SuppressLint("ScheduleExactAlarm")
    private fun alarm(id: Int, name: String, h: Int, m: Int) {

        val advance1 = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val advance2 = advance1.getString("advanceTime_key", "1")

        val intent = Intent(requireContext(), MyBroadcastReceiver::class.java)
        intent.putExtra("routineId", id)
        intent.putExtra("routineName", name)

        val resultPendingIntent = PendingIntent.getBroadcast(
            requireContext(), id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // calendar setup
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, h)
        calendar.set(java.util.Calendar.MINUTE, m)
        calendar.set(java.util.Calendar.SECOND, 0)

        // subract advance time so we notify early enough
        val advance3 = advance2?.toInt()
        calendar.add(java.util.Calendar.MINUTE, -advance3!!)

        val alarmManager = requireContext().getSystemService(
            android.content.Context.ALARM_SERVICE
        ) as? AlarmManager

        alarmManager?.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, resultPendingIntent)
    }

    private fun observerSetup(id: Int?) {
        if (id != null) {
            viewModel.getRoutineById(id)
                .observe(viewLifecycleOwner) { routine ->
                    if (routine != null) { // a check so it doesnt force me to use !!
                        fillIn(routine)
                    }
                }
        }
    }
}
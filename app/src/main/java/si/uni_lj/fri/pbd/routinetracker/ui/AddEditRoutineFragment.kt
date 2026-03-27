package si.uni_lj.fri.pbd.routinetracker.ui

import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import si.uni_lj.fri.pbd.routinetracker.R
import si.uni_lj.fri.pbd.routinetracker.databinding.FragmentAddEditRoutineBinding

class AddEditRoutineFragment : Fragment() {
    private lateinit var binding: FragmentAddEditRoutineBinding
    private var databaseHelper: DatabaseHelper? = null

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
        binding = FragmentAddEditRoutineBinding.inflate(inflater, container, false)

        databaseHelper = DatabaseHelper(requireContext())

        enableChooseType()

        // check if the routine exists, if not -> fill in the existing routine details
        val id = arguments?.getInt("routineId")
        if (id != null) {
            fillIn(id)
        }

        // enable the save button
        binding.addeditSave.setOnClickListener {
            saveRoutine()
            // go back home
            findNavController().navigateUp()
        }
        return binding.root
    }

    // check this
    private fun enableChooseType() {
        val types = arrayOf("Study", "Exercise", "Socialize")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.addeditType.adapter = adapter
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

        // check if it exists and appropriately handle (use the bundle)
        val id = arguments?.getInt("routineId")
        if (id != null) {
            databaseHelper?.updateRoutine(id, routineName, routineType, startH, startM, endH, endM, routineDays, notif)
        } else {
            databaseHelper?.createRoutine(routineName, routineType, startH, startM, endH, endM, routineDays, notif)
        }
    }

    private fun fillIn(id: Int) {
        val cursor = databaseHelper?.readOneRoutine(id)
        if (cursor == null || !cursor.moveToFirst()) {
            cursor?.close()
            return
        }

        val routine_name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ROUTINE_NAME))
        binding.addeditName.setText(routine_name)

        // find position of the type and set the spinner to correct position (of that type)
        val type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ROUTINE_TYPE))
        val adapter = binding.addeditType.adapter as ArrayAdapter<String> // shortcut so i dont have to loop through the types
        val position = adapter.getPosition(type)
        binding.addeditType.setSelection(position)

        // fill time
        startH = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.START_H))
        startM = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.START_M))
        endH = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.END_H))
        endM = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.END_M))

        binding.addeditStart.setText("%02d:%02d".format(startH, startM))
        binding.addeditEnd.setText("%02d:%02d".format(endH, endM))

        // fill days
        val routine_days = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ROUTINE_DAYS))

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

        // fil notif
        val notif = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ROUTINE_NOTIF))
        if (notif == 1) {
            binding.addeditNotif.isChecked = true
        } else {
            binding.addeditNotif.isChecked = false
        }

        cursor.close()
    }
}
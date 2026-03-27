package si.uni_lj.fri.pbd.routinetracker.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import si.uni_lj.fri.pbd.routinetracker.R
import si.uni_lj.fri.pbd.routinetracker.databinding.FragmentAddEditRoutineBinding
import si.uni_lj.fri.pbd.routinetracker.databinding.FragmentRoutineDetailsBinding
import si.uni_lj.fri.pbd.routinetracker.databinding.FragmentRoutineListBinding

// this is just a simplification of the AddEditRoutine (+ some small adjustments to the xml) -> So just the edit part where we populate the fields
// + the edit and delete ofcourse

class RoutineDetailsFragment : Fragment() {
    private lateinit var binding: FragmentRoutineDetailsBinding
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

        binding = FragmentRoutineDetailsBinding.inflate(inflater, container, false)

        databaseHelper = DatabaseHelper(requireContext())

        val id = arguments?.getInt("routineId")
        if (id != null) {
            fillIn(id)
        }

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
                databaseHelper?.deleteRoutine(id)
            }
            findNavController().navigateUp() // go home
        }

        return binding.root
    }

    private fun fillIn(id: Int) {
        val cursor = databaseHelper?.readOneRoutine(id)
        if (cursor == null || !cursor.moveToFirst()) {
            cursor?.close()
            return
        }

        val routine_name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ROUTINE_NAME))
        binding.detailsName.setText(routine_name)

        // find position of the type and set the spinner to correct position (of that type)

        val routine_type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ROUTINE_TYPE))
        binding.detailsType.setText(routine_type)

        // fill time
        startH = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.START_H))
        startM = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.START_M))
        endH = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.END_H))
        endM = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.END_M))

        binding.detailsTimeStart.setText("%02d:%02d".format(startH, startM))
        binding.detailsTimeEnd.setText("%02d:%02d".format(endH, endM))

        // fill days
        val routine_days = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ROUTINE_DAYS))

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

        // fil notif
        val notif = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ROUTINE_NOTIF))
        if (notif == 1) {
            val y = "Yes" // ugly but its an easy wat to stop the error
            binding.detailsNotif.setText(y)
        } else {
            val n = "No"
            binding.detailsNotif.setText(n)
        }
        cursor.close()
    }
}
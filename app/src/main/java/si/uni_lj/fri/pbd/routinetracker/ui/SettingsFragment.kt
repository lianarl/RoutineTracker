package si.uni_lj.fri.pbd.routinetracker.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import si.uni_lj.fri.pbd.routinetracker.R
import si.uni_lj.fri.pbd.routinetracker.repository.RoutineRepository
import si.uni_lj.fri.pbd.routinetracker.viewmodel.RoutineListViewModel
import si.uni_lj.fri.pbd.routinetracker.viewmodel.RoutineListViewModelFactory

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var viewModel: RoutineListViewModel

    override fun onCreatePreferences(savedInstanceState: Bundle?,
                                     rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        //findPreference<SwitchPreferenceCompat>("notifications_key")
        //    ?.onPreferenceChangeListener {_, newValue ->
        //        Log.d("Preferences", "Notifications enabled: $newValue")
        //        true // returns true if the event is handled
        //    }

        // viewModel setup
        val repository = RoutineRepository.getInstance(requireContext())
        val factory = RoutineListViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[RoutineListViewModel::class.java]

        // reset app functionallity
        findPreference<Preference>("reset")
            ?.setOnPreferenceClickListener {
                Log.d("Preferences", "Reseting the app")

                // delete routines with vm
                viewModel.deleteAllRoutines()

                // reset the shared preferences (https://stackoverflow.com/questions/38063326/reset-settings-preferences)
                val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
                val editor: SharedPreferences.Editor = preferences.edit()
                editor.clear()
                editor.apply()

                true // return true if clicked
            }

    }
}
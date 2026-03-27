package si.uni_lj.fri.pbd.routinetracker.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import si.uni_lj.fri.pbd.routinetracker.R
import java.util.prefs.Preferences

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?,
                                     rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        //findPreference<SwitchPreferenceCompat>("notifications_key")
        //    ?.onPreferenceChangeListener {_, newValue ->
        //        Log.d("Preferences", "Notifications enabled: $newValue")
        //        true // returns true if the event is handled
        //    }

        // reset app functionallity
        findPreference<Preference>("reset")
            ?.setOnPreferenceClickListener {
                Log.d("Preferences", "Reseting the app")

                val db = DatabaseHelper(requireContext())
                db.writableDatabase.delete(DatabaseHelper.TABLE_ROUTINES, null, null)

                // reset the shared preferences (https://stackoverflow.com/questions/38063326/reset-settings-preferences)
                val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
                val editor: SharedPreferences.Editor = preferences.edit()
                editor.clear()
                editor.apply()

                true // return true if clicked
            }

    }
}
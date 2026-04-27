package si.uni_lj.fri.pbd.routinetracker.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import si.uni_lj.fri.pbd.routinetracker.R
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.findNavController
import si.uni_lj.fri.pbd.routinetracker.databinding.ActivityMainBinding
import android.Manifest
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import si.uni_lj.fri.pbd.routinetracker.util.RoutineEvaluationWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    var drawerLayout: DrawerLayout? = null
    var toggle: ActionBarDrawerToggle? = null

    companion object {
        const val TAG = "MainActivity"
        const val CHANNELID = "si.uni_lj.fri.pbd.routinetracker.ui"
        const val NOTIFICATIONID = 101
        const val KEY_TEXT_REPLY = "key_text_reply" // for replies, wont need currently
        const val NOTIF_REQUEST_CODE = 42
    }

    private var notificationManager: NotificationManagerCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // drawer layout setup
        drawerLayout = binding.activityMain
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.Open, R.string.Close)

        drawerLayout?.addDrawerListener(toggle!!)
        toggle?.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // tell nav drawer where to go when a certain item is pressed
        binding.nv.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.routines -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.routineListFragment) // go to my routines
                    //Toast.makeText(this, "My Routines", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.settings -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.settingsFragment) // go to settings
                    //Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        // notification setup
        notificationManager = NotificationManagerCompat.from(this)
        // this creates our channel -> Now we can send a notif
        createChannel(CHANNELID, "Routine Notifications", "Upcoming routine")
        //handleIntent() // it is called here because a call for intent will create a new main activity, so we can handle intent that was fired from this notif
        //sendNotification(binding.root)
        makeRequest()
        notificationClicked()

        // schedule the worker every hour and enque
        val workManager = WorkManager.getInstance(this)
        val requestPeriodic = PeriodicWorkRequest.Builder(RoutineEvaluationWorker::class.java, 1, TimeUnit.HOURS).build()
        workManager.enqueueUniquePeriodicWork("RoutineWorkerPeriodic", ExistingPeriodicWorkPolicy.KEEP, requestPeriodic)
    }

    // create notification channel, if SDK>=26 L8M40 (Lecture 8, minute 40)
    private fun createChannel(id: String, name: String, desc: String) {
        // create notif channel if sdk >= 26
        if (Build.VERSION.SDK_INT>=26) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(id, name, importance)
            with(channel) {
                description = desc
                lightColor = Color.RED
                enableLights(true)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    // functions for permissions
    private fun makeRequest() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIF_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission was denied")
        } else {
            Log.d(TAG, "Permission was granted")
        }
    }

    // if the user clicked the notification, the app ones at details
    private fun notificationClicked() {
        val id = intent?.getIntExtra("routineId", -1)
        if (id != -1) {
            val send = Bundle()
            send.putInt("routineId", id!!)
            findNavController(R.id.nav_host_fragment).navigate(R.id.routineDetailsFragment, send)
        }
    }

    // function for drawer toggle
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle?.onOptionsItemSelected(item)!!)
            return true
        return super.onOptionsItemSelected(item)
    }

    // note time when app goes to foreground
    override fun onResume() {
        super.onResume()
        val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor: SharedPreferences.Editor = preferences.edit()
        val time = System.currentTimeMillis()
        editor.putLong("lastForegroundTime", time)
        editor.apply()
    }

    // note time when app goes to background
    override fun onPause() {
        super.onPause()
        val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor: SharedPreferences.Editor = preferences.edit()
        val time = System.currentTimeMillis()
        editor.putLong("lastBackgroundTime", time)
        editor.apply()
    }
}
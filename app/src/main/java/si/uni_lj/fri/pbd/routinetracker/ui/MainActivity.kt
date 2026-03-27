package si.uni_lj.fri.pbd.routinetracker.ui

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import si.uni_lj.fri.pbd.routinetracker.R
import android.widget.Toast
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.google.android.material.navigation.NavigationView
import si.uni_lj.fri.pbd.routinetracker.databinding.ActivityMainBinding
import android.Manifest

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
        createChannel(CHANNELID, "DiretReply News", "Example News Channel")
        //handleIntent() // it is called here because a call for intent will create a new main activity, so we can handle intent that was fired from this notif
        sendNotification(binding.root)
    }

    // TODO: create notification channel, if SDK>=26 L8M40
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

    // TODO: handle notification (with action) sending

    @SuppressLint("MissingPermission")
    fun handleSending() {

        //val resultIntent = Intent(this, MainActivity::class.java)

        //val resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        //val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).setLabel("Enter your reply here").build()

        //val replyAction = NotificationCompat.Action.Builder(android.R.drawable.ic_dialog_info, "Reply", resultPendingIntent).addRemoteInput(remoteInput).build()

        val newMessageNotification = NotificationCompat.Builder(this, CHANNELID)
            .setColor(Color.YELLOW)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("My notification")
            .setContentText("This allows your feedback")
            //.addAction(replyAction)
            .build() // this creates the notif

        // send notif
        notificationManager?.notify(NOTIFICATIONID, newMessageNotification)
    }

    fun sendNotification(view: View){

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Permission to post notification denied")

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.POST_NOTIFICATIONS)){

                val builder = AlertDialog.Builder(this)
                with(builder){
                    //setMessage("If you don't enable this, you won't receive up-to-date info from this app!")
                    //setTitle("Permission I really need")
                    setPositiveButton("OK") {
                        p0, p1->
                        makeRequest()
                    }
                }

                val dialog = builder.create()
                dialog.show()

            } else {
                makeRequest()
            }

        } else {
            handleSending()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIF_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission was denied")
        } else {
            Log.d(TAG, "Permission was granted")
            handleSending()
        }
    }

    /*
    // TODO: handle the notif intent and update UI
    @SuppressLint("MissingPermission")
    private fun handleIntent() {
        // query the intent that started this activity
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        if (remoteInput != null) {
            val inputString = remoteInput.getCharSequence(KEY_TEXT_REPLY).toString()
            binding.textView.text = inputString // we show it in the text view (creat the xml)
        }

        // update the notification
        val replyIntent = Intent(this, MainActivity::class.java)
        val replyPendingIntent = PendingIntent.getActivity(this, 0, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // recreate a new notif
        val repliedNotification = NotificationCompat.Builder(this, CHANNELID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentText("Reply received")
            .setContentIntent(replyPendingIntent)
            .build()
        notificationManager?.notify(NOTIFICATIONID, repliedNotification)
    }
     */

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle?.onOptionsItemSelected(item)!!)
            return true
        return super.onOptionsItemSelected(item)
    }
}
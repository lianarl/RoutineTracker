package si.uni_lj.fri.pbd.routinetracker.ui

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import java.util.Calendar
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import kotlinx.coroutines.runBlocking
import si.uni_lj.fri.pbd.routinetracker.repository.RoutineRepository
import si.uni_lj.fri.pbd.routinetracker.viewmodel.RoutineListViewModel
import si.uni_lj.fri.pbd.routinetracker.viewmodel.RoutineListViewModelFactory

// https://mubaraknative.medium.com/creating-a-alarm-using-alarmmanager-in-android-e27a4283d39f
// https://developer.android.com/develop/background-work/background-tasks/broadcasts
// https://developer.android.com/develop/ui/views/notifications/build-notification
// https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/run-blocking.html

class MyBroadcastReceiver : BroadcastReceiver () {

    private lateinit var viewModel: RoutineListViewModel

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {

        // check if we can send notif
        val state1 = PreferenceManager.getDefaultSharedPreferences(context!!)
        val state2 = state1.getBoolean("notifications_key", false)
        if (state2 == false) {
            return
        }

         // get routine name and id from intent
        val name = intent?.getStringExtra("routineName")
        val id = intent?.getIntExtra("routineId", 0)

        // get routine from repository
        val repository = RoutineRepository.getInstance(context)
        val routine = runBlocking { repository.getRoutine(id!!) } // need runBlocking because getRoutine is suspend, but onReceive cant wait

        val days = routine?.days
        val calendar = Calendar.getInstance()
        val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val today = daysOfWeek[calendar.get(Calendar.DAY_OF_WEEK) - 1]
        if (!days!!.contains(today)) {
            return
        }

        // clicking notif opens routine details
        val resultIntent = Intent(context, MainActivity::class.java)
        resultIntent.putExtra("routineId", id)
        resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val resultPendingIntent = PendingIntent.getActivity(context, id!!, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // build the notif
        val newMessageNotification = NotificationCompat.Builder(context, MainActivity.CHANNELID)
            .setColor(Color.YELLOW)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Routine")
            .setContentText("Routine $name is going to start")
            .setContentIntent(resultPendingIntent) // listener for clicks
            .build() // this builds the notification

        // send notif
        NotificationManagerCompat.from(context).notify(id, newMessageNotification)
    }
}
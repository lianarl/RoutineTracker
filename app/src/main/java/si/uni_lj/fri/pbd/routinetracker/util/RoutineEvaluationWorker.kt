package si.uni_lj.fri.pbd.routinetracker.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import si.uni_lj.fri.pbd.routinetracker.data.entity.RoutineExecution
import si.uni_lj.fri.pbd.routinetracker.repository.RoutineRepository
import java.util.Calendar

class RoutineEvaluationWorker(appContext: Context, workerParams: WorkerParameters): CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {

        // connect to db via repository
        val repository = RoutineRepository.getInstance(applicationContext)

        // read foreground/background times from SharedPrefs
        val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val ftime = preferences.getLong("lastForegroundTime", 0L)
        val btime = preferences.getLong("lastBackgroundTime", 0L)

        // get today
        val calendar = Calendar.getInstance()
        val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val today = daysOfWeek[calendar.get(Calendar.DAY_OF_WEEK) - 1]
        val todaysDate = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault()).format((java.util.Date()))

        // get all routines
        val routines = repository.getAllRoutinesNonLive()

        // loop through routines (only those come in play that are set for today)
        for (r in routines) {
            var days = ""
            if (!r.days.isNullOrBlank()) {
                days = r.days!!
            }
            if (!days.contains(today)) {
                continue // skip if routines arent today
            }

            // is there an execution today already? -> If yes, skip
            if (repository.getExNonLive(r.id, todaysDate) != null) {
                continue
            }

            // get start and end time of a routine
            val start = Calendar.getInstance()
            start.set(Calendar.HOUR_OF_DAY, r.startH)
            start.set(Calendar.MINUTE, r.startM)
            val end = Calendar.getInstance()
            end.set(Calendar.HOUR_OF_DAY, r.endH)
            end.set(Calendar.MINUTE, r.endM)

            // put all the times in variables (times of the routine and of the app usage)
            val startMs = start.timeInMillis
            val endMs = end.timeInMillis
            val appStartMs = ftime
            var appEndMs = 0L
            val now = calendar.timeInMillis

            // check last time since app open
            if (btime > ftime) {
                appEndMs = btime
            } else { // app is open
                appEndMs = now
            }

            // check if the app was used during the routine (if the routine was completed)
            var completed = false
            if (appStartMs < endMs) {
                if (startMs < appEndMs) {
                    completed = true
                }
            }
            if (completed) {
                // add an execution
                val ex = RoutineExecution(routineId = r.id, date = todaysDate, completed = true)
                repository.insertEx(ex)
            } else if (now > endMs) {
                // routine has passed and we didnt open the app during it -> not completed
                val ex = RoutineExecution(routineId = r.id, date = todaysDate, completed = false)
                repository.insertEx(ex)
            }
        }

        // indicate whether the task finished successfully
        return Result.success()
    }
}
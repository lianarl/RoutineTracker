package si.uni_lj.fri.pbd.routinetracker.repository

import android.content.Context
import si.uni_lj.fri.pbd.routinetracker.data.dao.RoutineDao
import androidx.lifecycle.LiveData
import si.uni_lj.fri.pbd.routinetracker.data.RoutinesDatabase
import si.uni_lj.fri.pbd.routinetracker.data.entity.Routine
import si.uni_lj.fri.pbd.routinetracker.data.entity.RoutineExecution

// built on the labs 7 code

class RoutineRepository(private val routineDao: RoutineDao) {

    // A static reference to the database
    val allRoutines: LiveData<List<Routine>> = routineDao.getAllRoutines()

    suspend fun insertRoutine(newroutine: Routine): Long {
        // Run query to insert a routine on the executor
        return routineDao.insertRoutine(newroutine)
    }

    suspend fun deleteRoutine(id: Int) {
        routineDao.deleteRoutine(id)
    }

    suspend fun updateRoutine(routine: Routine) {
        routineDao.updateRoutine(routine)
    }

    suspend fun getRoutine(id: Int): Routine? {
        return routineDao.getRoutine(id)
    }

    fun getRoutineById(id: Int): LiveData<Routine?> {
        return routineDao.getRoutineById(id)
    }

    suspend fun deleteAllRoutines() {
        routineDao.deleteAllRoutines()
    }

    suspend fun insertEx(ex: RoutineExecution) {
        routineDao.insertEx(ex)
    }

    fun getExes(id: Int): LiveData<List<RoutineExecution>> {
        return routineDao.getExes(id)
    }

    suspend fun getLatestEx(id: Int): RoutineExecution? {
        return routineDao.getLatestEx(id)
    }

    suspend fun getAllRoutinesNonLive(): List<Routine> {
        return routineDao.getAllRoutinesNonLive()
    }

    suspend fun getExNonLive(id: Int, date: String): RoutineExecution? {
        return routineDao.getExNonLive(id, date)
    }

    companion object {
        @Volatile
        private var INSTANCE: RoutineRepository? = null

        fun getInstance(context: Context): RoutineRepository {
            return INSTANCE ?: synchronized(this) {
                val database = RoutinesDatabase.getDatabase(context)
                val instance = RoutineRepository(database.routineDao())
                INSTANCE = instance
                instance
            }
        }
    }
}
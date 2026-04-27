package si.uni_lj.fri.pbd.routinetracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import si.uni_lj.fri.pbd.routinetracker.data.entity.Routine
import si.uni_lj.fri.pbd.routinetracker.data.entity.RoutineExecution

@Dao
interface RoutineDao {
    @Insert
    suspend fun insertRoutine(routine: Routine): Long // need rowId return for the scheduling of the Alarm

    @Query("DELETE FROM routines WHERE routineId = :id") // using query to delete so I just need the ID not the whole object
    suspend fun deleteRoutine(id: Int)

    @Update
    suspend fun updateRoutine(routine: Routine)

    @Query("SELECT * FROM routines")
    fun getAllRoutines(): LiveData<List<Routine>>

    @Query("SELECT * FROM routines WHERE routineId = :id")
    suspend fun getRoutine(id: Int): Routine?

    @Query("SELECT * FROM routines WHERE routineId = :id")
    fun getRoutineById(id: Int): LiveData<Routine?>

    // need for the reset in settings
    @Query("DELETE FROM routines")
    suspend fun deleteAllRoutines()

    // methods for RoutineExecution

    // because routineDetails needs execution history -> all executions of a routine (history)
    @Query("SELECT * FROM routineExecution WHERE routineId = :id")
    fun getExes(id: Int): LiveData<List<RoutineExecution>>

    // because routineList needs an indicator whether the last routine was executed or not -> last execution of a routine (indicator)
    @Query("SELECT * FROM routineExecution WHERE routineId = :id ORDER BY date DESC")
    suspend fun getLatestEx(id:Int): RoutineExecution?

    // because routineExecution should be added to the DB
    @Insert
    suspend fun insertEx(execution: RoutineExecution)

    // non livedata versions of these functions so i can use them inside my worker
    @Query("SELECT * FROM routines")
    suspend fun getAllRoutinesNonLive(): List<Routine>
    @Query("SELECT * FROM routineExecution WHERE routineId = :id AND date = :date")
    suspend fun getExNonLive(id: Int, date: String): RoutineExecution?
}
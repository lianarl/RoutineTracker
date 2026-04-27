package si.uni_lj.fri.pbd.routinetracker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import si.uni_lj.fri.pbd.routinetracker.data.entity.Routine
import si.uni_lj.fri.pbd.routinetracker.repository.RoutineRepository

// bundled routine and its completion info for easier management
data class RoutineAndCompletion(
    val routine: Routine,
    val completed: Boolean?
)

class RoutineListViewModel(private val repository: RoutineRepository) : ViewModel() {

    // add LiveData field for allRoutines
    var allRoutines: LiveData<List<Routine>> = repository.allRoutines // all the routines that exists currently

    // adding encapsulated live data for checking completion of routines (check lab 7 for more implementation info)
    private val _routinesWithCompletion = MutableLiveData<List<RoutineAndCompletion>>()
    val routinesWithCompletion: LiveData<List<RoutineAndCompletion>> = _routinesWithCompletion

    // get list of routines paired with their completion info
    fun getCompletionInfo(routines: List<Routine>) {
        viewModelScope.launch {
            val m = routines.map { routine ->
                val completion = repository.getLatestEx(routine.id)
                RoutineAndCompletion(routine, completion?.completed)
            }
            _routinesWithCompletion.value = m
        }
    }

    // add functions for inserting, deleting, and updating a routine
    fun insertRoutine(routine: Routine) {
        viewModelScope.launch {
            repository.insertRoutine(routine)
        }
    }
    fun deleteRoutine(id: Int) {
        viewModelScope.launch {
            repository.deleteRoutine(id)
        }
    }
    fun updateRoutine(routine: Routine) {
        viewModelScope.launch {
            repository.updateRoutine(routine)
        }
    }

    fun deleteAllRoutines() {
        viewModelScope.launch {
            repository.deleteAllRoutines()
        }
    }
}
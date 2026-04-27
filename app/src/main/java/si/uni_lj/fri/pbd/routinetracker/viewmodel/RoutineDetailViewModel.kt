package si.uni_lj.fri.pbd.routinetracker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import si.uni_lj.fri.pbd.routinetracker.data.entity.Routine
import si.uni_lj.fri.pbd.routinetracker.data.entity.RoutineExecution
import si.uni_lj.fri.pbd.routinetracker.repository.RoutineRepository

// this one also handles the inserting of routines, so i dont have to make another file for the addedit -> i think this will work (TBD)

class RoutineDetailViewModel(private val repository: RoutineRepository): ViewModel() {
    // need to show one specific routine and its history

    fun getRoutineById(id: Int): LiveData<Routine?> {
        return repository.getRoutineById(id)
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

    // gets ex. history for one routine (fragment observes LiveData and puts it into new recycler)
    fun getExes(id: Int): LiveData<List<RoutineExecution>> {
        return repository.getExes(id)
    }

    // will also need ID to return to set alarm (because i will use the details VM for both routine detail and routine addedit)
    suspend fun insertRoutine(routine: Routine): Long {
        return repository.insertRoutine(routine)
    }
}
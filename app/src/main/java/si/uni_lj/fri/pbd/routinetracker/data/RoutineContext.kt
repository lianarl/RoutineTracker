package si.uni_lj.fri.pbd.routinetracker.data

import si.uni_lj.fri.pbd.routinetracker.data.entity.Routine
import si.uni_lj.fri.pbd.routinetracker.rest.WeatherData

data class RoutineContext(
    val weatherData: WeatherData,
    val light: Float,
    val routine: Routine?,
    val suggestion: String
)

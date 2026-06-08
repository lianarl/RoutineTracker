package si.uni_lj.fri.pbd.routinetracker.rest

// data class for weather info i get from API
data class WeatherData(
    val temperature: Double, // main.temp
    val conditions: String // weather[0].main
)
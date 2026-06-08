package si.uni_lj.fri.pbd.routinetracker.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.preference.PreferenceManager
import si.uni_lj.fri.pbd.routinetracker.data.RoutinesDatabase
import si.uni_lj.fri.pbd.routinetracker.data.dao.RoutineDao
import si.uni_lj.fri.pbd.routinetracker.data.entity.Routine
import si.uni_lj.fri.pbd.routinetracker.data.entity.RoutineExecution
import si.uni_lj.fri.pbd.routinetracker.rest.RestAPI
import si.uni_lj.fri.pbd.routinetracker.rest.RetrofitInstance
import si.uni_lj.fri.pbd.routinetracker.rest.WeatherData
import si.uni_lj.fri.pbd.routinetracker.data.RoutineContext
import si.uni_lj.fri.pbd.routinetracker.data.sensor.LightSensorReader

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

    suspend fun getRoutine(id: Long): Routine? {
        return routineDao.getRoutine(id)
    }

    fun getRoutineById(id: Long): LiveData<Routine?> {
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

    //              milestone 3
    // calls the api, gets json object and maps to weather data object

    private val api = RetrofitInstance.createService(RestAPI::class.java)
    suspend fun getWeather(location: String): WeatherData {
        // parse the json object
        val jsonObject = api.getWeather(location = location, key = "xxx")
        val temp = jsonObject.getAsJsonObject("main").get("temp").asDouble
        val cond = jsonObject.getAsJsonArray("weather").get(0).asJsonObject.get("main").asString

        return WeatherData(temperature = temp, conditions = cond)
    }

    // creates routine context object for routine recommendations
    // for case 1 its called from BroadcastReceiver
    // for case 2 its called from ViewModel
    suspend fun buildRoutineContext(routineId: Long, context: Context): RoutineContext {

        // get location from SharedPreferences
        val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val location = preferences.getString("location_key", "Ljubljana, Slovenia")

        // get info for the object
        val weather = getWeather(location!!)
        val light = LightSensorReader(context).readOnce()
        val routine: Routine? = getRoutine(routineId)

        // suggestion calc here
        var suggestion = ""
        if (routine?.type == "Study") {
            if (light > 500) {
                suggestion = "Consider adjusting the light for better focus"
            } else if (light < 50) {
                suggestion = "Consider adjusting the light for better focus"
            } else {
                suggestion = "Get ready for some productive studying"
            }
        } else if (routine?.type == "Exercise") {
            if (weather.temperature > 30) {
                suggestion = "Consider indoor exercise"
            } else if (weather.temperature < 5) {
                suggestion = "Consider indoor exercise"
            } else {
                suggestion = "Consider outdoor exercise"
            }
        } else if (routine?.type == "Socialise") {
            if (weather.conditions == "Rain" || weather.conditions == "Snow") {
                suggestion = "Consider attending an indoor event, for instance, going to a theatre"
            } else {
                suggestion = "Consider organising a picnic"
            }
        }
        return RoutineContext(weather, light, routine, suggestion)
    }
}

/*
{
  "coord": {
    "lon": 31.2497,
    "lat": 30.0626
  },
  "weather": [
    {
      "id": 800,
      "main": "Clear",
      "description": "clear sky",
      "icon": "01n"
    }
  ],
  "base": "stations",
  "main": {
    "temp": 29.42,
    "feels_like": 28.36,
    "temp_min": 29.12,
    "temp_max": 29.42,
    "pressure": 1012,
    "humidity": 32,
    "sea_level": 1012,
    "grnd_level": 1007
  },
  "visibility": 10000,
  "wind": {
    "speed": 4.63,
    "deg": 70
  },
  "clouds": {
    "all": 0
  },
  "dt": 1779131354,
  "sys": {
    "type": 1,
    "id": 2514,
    "country": "EG",
    "sunrise": 1779073201,
    "sunset": 1779122584
  },
  "timezone": 10800,
  "id": 360630,
  "name": "Cairo",
  "cod": 200
}
 */
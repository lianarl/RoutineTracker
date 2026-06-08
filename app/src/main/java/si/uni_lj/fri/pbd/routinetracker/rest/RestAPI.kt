package si.uni_lj.fri.pbd.routinetracker.rest

import com.google.gson.JsonObject
import retrofit2.http.GET
import retrofit2.http.Query

// example: https://api.openweathermap.org/data/2.5/weather?q=Cairo&appid=dba5430543806e2f33c12221d29883b2&units=metric
// build is: base + "data/..." + params
interface RestAPI {
    @GET("data/2.5/weather")
    // set units to metric so we get C
    suspend fun getWeather(@Query("q")location: String, @Query("appid")key: String, @Query("units")units:String = "metric"): JsonObject // returns a jsonObject because i can manually parse fields in repository
}
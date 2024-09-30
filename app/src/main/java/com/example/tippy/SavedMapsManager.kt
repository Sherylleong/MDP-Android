package com.example.tippy
import android.content.Context
import android.content.SharedPreferences
import androidx.constraintlayout.helper.widget.Grid
import org.json.JSONArray
import org.json.JSONObject

class SavedMapsManager (context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    private fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    private fun getString(key: String, defaultValue: String): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    fun loadCarPos() : GridCar {
        val savedCarString = getString("car","none")
        if (savedCarString == "none") {
            return GridCar(Coord(2,2), "N")
        }
        else {
            val jsonObject = JSONObject(savedCarString!!)
            val x = jsonObject.getInt("x")
            val y = jsonObject.getInt("y")
            val direction = jsonObject.getString("d")
            return GridCar(Coord(x,y), direction)
        }
    }

    fun saveCarPos(car: GridCar) {
        val jsonObject = JSONObject().apply {
            put("x", car.coord.x)
            put("y", car.coord.y)
            put("d", car.direction)
        }
        saveString("car", jsonObject.toString())


    }

    fun saveObstaclesList(obstaclesList: List<GridObstacle>) {
        val jsonObstaclesArray = JSONArray().apply {
            obstaclesList.forEach { item ->
                val jsonObject = JSONObject().apply {
                    put("x", item.coord.x)
                    put("y", item.coord.y)
                    put("d", item.direction ?: "none")
                    put("id", item.number)
                }
                put(jsonObject) // Add JSON object to the array
            }
        }
        // save to preferences
        println("rrr")
        println(jsonObstaclesArray.toString())
        saveString("obstacles", jsonObstaclesArray.toString())
    }

    fun loadObstaclesList() : List<GridObstacle>{
        val savedObstaclesListString = getString("obstacles","none")
        println("sss")
        println(savedObstaclesListString)
        if (savedObstaclesListString == "none") {
            return emptyList()
        }
        else {
            val jsonArray = JSONArray(savedObstaclesListString)
            val savedObstaclesList = mutableListOf<GridObstacle>()
            for (i in 0 until jsonArray.length()) {
                println(i)
                val jsonObject = jsonArray.getJSONObject(i)
                val x = jsonObject.getInt("x")
                val y = jsonObject.getInt("y")
                val number = jsonObject.getString("id")
                val direction = jsonObject.getString("d")
                savedObstaclesList.add(GridObstacle(Coord(x,y), number, direction))
            }
            return savedObstaclesList
        }
    }
}

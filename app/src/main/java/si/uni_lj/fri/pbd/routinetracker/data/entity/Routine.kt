package si.uni_lj.fri.pbd.routinetracker.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="routines")
class Routine {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="routineId")
    var id = 0

    @ColumnInfo(name="routineName")
    var name: String? = ""

    @ColumnInfo(name="routineType")
    var type: String? = ""

    @ColumnInfo(name="startH")
    var startH = 0

    @ColumnInfo(name="startM")
    var startM = 0

    @ColumnInfo(name="endH")
    var endH = 0

    @ColumnInfo(name="endM")
    var endM = 0

    @ColumnInfo(name="days")
    var days: String? = ""

    @ColumnInfo(name="notif")
    var notif = 0
}
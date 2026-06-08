package si.uni_lj.fri.pbd.routinetracker.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// adding foreign key to entity: https://www.youtube.com/watch?v=a7bHKh60bNY

@Entity(tableName="routineExecution", foreignKeys = [ForeignKey(entity = Routine::class, parentColumns = ["routineId"], childColumns = ["routineId"], onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE)])
data class RoutineExecution(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    var id: Long = 0,

    @ColumnInfo(name="routineId")
    var routineId: Int = 0,

    @ColumnInfo(name="date")
    var date: String = "",

    @ColumnInfo(name="completed")
    var completed: Boolean = false
)
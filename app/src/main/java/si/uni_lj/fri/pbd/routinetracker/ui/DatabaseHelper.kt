package si.uni_lj.fri.pbd.routinetracker.ui

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.database.Cursor

// Literature for db content: Lecture videos, lecture examples, https://developer.android.com/training/data-storage/sqlite#kotlin

class DatabaseHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    var dbHelper: DatabaseHelper? = null

    companion object {
        const val TAG = "DatabaseHelper"
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "routines_db"
        const val TABLE_ROUTINES = "routines"

        // basic data
        const val _ID = "_id"
        const val ROUTINE_NAME = "name"
        const val ROUTINE_TYPE = "type"

        // range data
        const val START_H = "start_h"
        const val START_M = "start_m"
        const val END_H = "end_h"
        const val END_M = "end_m"

        // other data
        const val ROUTINE_DAYS = "days"
        const val ROUTINE_NOTIF = "notif"

        val COLUMNS = arrayOf(_ID, ROUTINE_NAME, ROUTINE_TYPE, START_H, START_M, END_H, END_M, ROUTINE_DAYS, ROUTINE_NOTIF)
    }

    override fun onCreate(db: SQLiteDatabase?) {
        Log.d(TAG, "onCreate")

        val CREATE_ROUTINES_TABLE = ("CREATE TABLE" + TABLE_ROUTINES + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ROUTINE_NAME + " TEXT NOT NULL, " +
                ROUTINE_TYPE + " TEXT NOT NULL, " +
                START_H + " INTEGER, " +
                START_M + " INTEGER, " +
                END_H + " INTEGER, " +
                END_M + " INTEGER, " +
                ROUTINE_DAYS + " TEXT NOT NULL, " +
                ROUTINE_NOTIF + " INTEGER NOT NULL)")
        db?.execSQL(CREATE_ROUTINES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "onUpgrade")
        db?.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTINES)
        onCreate(db)
    }

    fun readAllRoutines(): Cursor? {
        return dbHelper?.readableDatabase?.query(DatabaseHelper.TABLE_ROUTINES, DatabaseHelper.COLUMNS, null, null, null, null, null)
    }

    fun readOneRoutine(id: Int): Cursor? {
        return dbHelper?.readableDatabase?.query(DatabaseHelper.TABLE_ROUTINES, DatabaseHelper.COLUMNS, _ID, null, null, null, null)
    }

    fun createRoutine(name: String, type: String, startH: Int, startM: Int, endH: Int, endM: Int, days: String, notif: Int): Long {
        val db = dbHelper?.writableDatabase
        val values = ContentValues().apply {
            put(ROUTINE_NAME, name)
            put(ROUTINE_TYPE, type)
            put(START_H, startH)
            put(START_M, startM)
            put(END_H, endH)
            put(END_M, endM)
            put(ROUTINE_DAYS, days)
            put(ROUTINE_NOTIF, notif)
        }
        val newRowId = db?.insert(TABLE_ROUTINES, null, values)
        return newRowId!! // why does !! fix??, i could also just remove the ? from helper and db -> Check this!
    }

    fun updateRoutine(id: Int, name: String, type: String, startH: Int, startM: Int, endH: Int, endM: Int, days: String, notif: Int): Int {
        val db = dbHelper?.writableDatabase
        val values = ContentValues().apply {
            put(ROUTINE_NAME, name)
            put(ROUTINE_TYPE, type)
            put(START_H, startH)
            put(START_M, startM)
            put(END_H, endH)
            put(END_M, endM)
            put(ROUTINE_DAYS, days)
            put(ROUTINE_NOTIF, notif)
        }
        val selection = "$_ID = ?"
        val selectionArgs = arrayOf(id.toString())
        val count = db?.update(
            TABLE_ROUTINES,
            values,
            selection,
            selectionArgs
        )
        return count!!
    }

    fun deleteRoutine(id: Int): Int {
        val db = dbHelper?.writableDatabase
        val selection = "$_ID = ?"
        val selectionArgs = arrayOf(id.toString())
        val deletedRows = db?.delete(TABLE_ROUTINES, selection, selectionArgs)
        return deletedRows!!
    }

}
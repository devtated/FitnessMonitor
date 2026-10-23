package com.example.fitnessmonitor.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ActivityRecord(
    val id: Long,
    val date: String,
    val steps: Int,
    val durationSeconds: Int
)

class FitnessDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "fitness_monitor.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_RECORDS = "activity_records"
        const val COLUMN_ID = "id"
        const val COLUMN_DATE = "date"
        const val COLUMN_STEPS = "steps"
        const val COLUMN_DURATION = "duration_seconds"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = ("CREATE TABLE $TABLE_RECORDS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_DATE TEXT UNIQUE, " +
                "$COLUMN_STEPS INTEGER, " +
                "$COLUMN_DURATION INTEGER)")
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RECORDS")
        onCreate(db)
    }

    fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun saveOrUpdateTodayRecord(steps: Int, durationSeconds: Int) {
        val db = this.writableDatabase
        val today = getTodayDate()

        val values = ContentValues().apply {
            put(COLUMN_DATE, today)
            put(COLUMN_STEPS, steps)
            put(COLUMN_DURATION, durationSeconds)
        }

        val cursor = db.query(
            TABLE_RECORDS,
            arrayOf(COLUMN_ID),
            "$COLUMN_DATE = ?",
            arrayOf(today),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
            cursor.close()
            db.update(
                TABLE_RECORDS,
                values,
                "$COLUMN_ID = ?",
                arrayOf(id.toString())
            )
        } else {
            cursor.close()
            db.insert(TABLE_RECORDS, null, values)
        }
    }

    fun getTodayRecord(): ActivityRecord? {
        val db = this.readableDatabase
        val today = getTodayDate()
        val cursor = db.query(
            TABLE_RECORDS,
            null,
            "$COLUMN_DATE = ?",
            arrayOf(today),
            null, null, null
        )

        var record: ActivityRecord? = null
        if (cursor.moveToFirst()) {
            record = ActivityRecord(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                steps = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STEPS)),
                durationSeconds = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION))
            )
        }
        cursor.close()
        return record
    }

    fun getAllRecords(): List<ActivityRecord> {
        val records = mutableListOf<ActivityRecord>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_RECORDS,
            null,
            null,
            null,
            null, null,
            "$COLUMN_DATE DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                val record = ActivityRecord(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    steps = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STEPS)),
                    durationSeconds = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION))
                )
                records.add(record)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return records
    }
}

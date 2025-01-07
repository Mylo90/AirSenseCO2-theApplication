package com.example.airsenseco2_theapp.data
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.airsenseco2_theapp.model.Measurement
import com.example.airsenseco2_theapp.model.TrainingSession

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "training.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE training_sessions (
                id INTEGER PRIMARY KEY,
                start_time INTEGER,
                duration INTEGER
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE measurements (
                session_id INTEGER,
                co2 REAL,
                temperature REAL,
                timestamp INTEGER,
                FOREIGN KEY (session_id) REFERENCES training_sessions(id)
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS training_sessions")
        db.execSQL("DROP TABLE IF EXISTS measurements")
        onCreate(db)
    }

    fun insertTrainingSession(session: TrainingSession) {
        val db = writableDatabase
        val sessionValues = ContentValues().apply {
            put("id", session.id)
            put("start_time", session.startTime)
            put("duration", session.duration)
        }
        db.insert("training_sessions", null, sessionValues)

        session.measurements.forEach { measurement ->
            val measurementValues = ContentValues().apply {
                put("session_id", session.id)
                put("co2", measurement.co2)
                put("temperature", measurement.temperature)
                put("timestamp", measurement.timestamp)
            }
            db.insert("measurements", null, measurementValues)
        }
        db.close()
    }

    fun getAllSessions(): List<TrainingSession> {
        val sessions = mutableListOf<TrainingSession>()
        val db = readableDatabase

        val cursor = db.rawQuery("SELECT * FROM training_sessions", null)
        while (cursor.moveToNext()) {
            val sessionId = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
            val startTime = cursor.getLong(cursor.getColumnIndexOrThrow("start_time"))
            val duration = cursor.getLong(cursor.getColumnIndexOrThrow("duration"))

            // Hämta alla mätningar kopplade till denna session
            val measurements = getMeasurementsForSession(sessionId)
            sessions.add(TrainingSession(sessionId, measurements, startTime, duration))
        }
        cursor.close()
        db.close()

        return sessions
    }

    private fun getMeasurementsForSession(sessionId: Long): List<Measurement> {
        val measurements = mutableListOf<Measurement>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM measurements WHERE session_id = ?", arrayOf(sessionId.toString()))

        while (cursor.moveToNext()) {
            val co2 = cursor.getFloat(cursor.getColumnIndexOrThrow("co2"))
            val temperature = cursor.getFloat(cursor.getColumnIndexOrThrow("temperature"))
            val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"))
            measurements.add(Measurement(co2, temperature, timestamp))
        }
        cursor.close()

        return measurements
    }
}
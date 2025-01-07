package com.example.airsenseco2_theapp.data

import android.content.Context
import android.util.Log
import com.example.airsenseco2_theapp.model.TrainingSession
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CsvHelper(private val context: Context) {

    fun writeTrainingSessionToCsv(session: TrainingSession) {
        val file = File(context.getExternalFilesDir(null), "training_sessions.csv")
        val timestampFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        try {
            val writer = FileWriter(file, true)

            writer.append("Pass ID: ${session.id}, Start: ${timestampFormat.format(session.startTime)}, Duration: ${session.duration / 1000}s\n")

            writer.append("Timestamp,CO2 (PPM),Temperature (°C)\n")

            session.measurements.forEach { measurement ->
                writer.append("${timestampFormat.format(measurement.timestamp)},${measurement.co2},${measurement.temperature}\n")
            }

            writer.flush()
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun writeToCsv(co2: Float, temp: Float) {
        val file = File(context.getExternalFilesDir(null), "training_sessions.csv")
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        try {
            val writer = FileWriter(file, true)

            writer.append("$timestamp,$co2,$temp\n")

            writer.flush()
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun exportToExcel(co2: Float, temp: Float) {
        val file = File(context.getExternalFilesDir(null), "export_sessions.csv")
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        try {
            val writer = FileWriter(file, true)

            if (file.length() == 0L) {
                writer.append("Timestamp,CO2 (PPM),Temperature (°C)\n")
            }

            writer.append("$timestamp,$co2,$temp\n")

            writer.flush()
            writer.close()
            Log.d("CSV Export", "Data exporterad till $file")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun readCsv(): List<String> {
        val file = File(context.getExternalFilesDir(null), "training_sessions.csv")
        val data = mutableListOf<String>()

        if (file.exists()) {
            file.forEachLine { line ->
                data.add(line)
            }
        }
        return data
    }
}

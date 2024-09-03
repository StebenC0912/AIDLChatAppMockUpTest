package com.example.clientapp.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private const val FULL_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm"
    private const val TIME_FORMAT = "HH:mm"
    private const val DATE_FORMAT = "yyyy-MM-dd"
    
    /**
     * Converts a timestamp in milliseconds (Long) to a formatted date-time string.
     * If the timestamp corresponds to the current date, only the time (HH:mm) is displayed.
     * Otherwise, the full date and time (yyyy-MM-dd HH:mm) is displayed.
     *
     * @param timestamp The timestamp to be formatted, in milliseconds.
     * @return A string representing the formatted date and time or just the time if the date is today.
     */
    fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        val currentDate = Date()
        
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        val fullDateTimeFormat = SimpleDateFormat(FULL_DATE_TIME_FORMAT, Locale.getDefault())
        val timeFormat = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
        
        return if (dateFormat.format(date) == dateFormat.format(currentDate)) {
            // If the date is today, return only the time
            timeFormat.format(date)
        } else {
            // Otherwise, return the full date and time
            fullDateTimeFormat.format(date)
        }
    }
    
    /**
     * Converts a formatted date-time string (yyyy-MM-dd HH:mm) back to a timestamp in milliseconds (Long).
     *
     * @param dateTimeString The formatted date-time string (yyyy-MM-dd HH:mm).
     * @return The corresponding timestamp in milliseconds.
     * @throws ParseException If the date-time string is not in the expected format.
     */
    fun parseTimestamp(dateTimeString: String): Long {
        return try {
            val dateFormat = SimpleDateFormat(FULL_DATE_TIME_FORMAT, Locale.getDefault())
            val date: Date =
                dateFormat.parse(dateTimeString) ?: throw ParseException("Invalid date format", 0)
            date.time
        } catch (e: ParseException) {
            e.printStackTrace()
            throw e
        }
    }
}

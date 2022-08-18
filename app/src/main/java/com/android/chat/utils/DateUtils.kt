package com.android.chat.utils

import android.content.Context
import com.android.chat.R
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun sinceFrom(
        context: Context,
        date: String,
        format: String
    ): String {
        val event = Calendar.getInstance()

        val sdf = SimpleDateFormat(format)
         sdf.timeZone = TimeZone.getTimeZone("UTC")
        val mDate = sdf.parse(date)

        event.time = mDate!!
        val time = String.format("%02d", event.get(Calendar.HOUR_OF_DAY)) + ":" + String.format(
            "%02d", event.get(
                Calendar.MINUTE
            )
        )
        val now = Calendar.getInstance()
        val secondsDiff = getNumberOfSecondsBetweenCalendars(now, event)
        val daysDiff = getNumberOfDaysBetweenCalendars(now, event)
        val minutesDiff = secondsDiff / 60
        return if (secondsDiff in 0..59) {
            context.getString(R.string.just_now)
        } else if (minutesDiff in 1..59) {
            if (minutesDiff==1){
                context.getString(R.string.ago)  + " une " + context.getString(R.string.minutes)
            }else{
                context.getString(R.string.ago) + " " + minutesDiff + " " + context.getString(R.string.minutes)
            }
        } else if (minutesDiff in 0..1440) {
            if ((minutesDiff / 60) ==1){
                context.getString(R.string.ago) +    " une " + context.getString(
                    R.string.hours
                )
            }else{
                context.getString(R.string.ago) + " " + (minutesDiff / 60) + " " + context.getString(
                    R.string.hours
                )
            }

        } else if (daysDiff == 1) {
            context.getString(R.string.yesterday) + " " + context.getString(R.string.a) + " " + time
        } else {
            val day = event.get(Calendar.DATE)
            val monthDate = SimpleDateFormat("MMM")
            val monthName = monthDate.format(event.time)
            return if (now.get(Calendar.YEAR) == event.get(Calendar.YEAR)) {
                String.format("%02d", day) + ", " + monthName.capitalizeFirstCharacter()+ " " + context.getString(R.string.a) + " " + time
            } else {
                String.format(
                    "%02d",
                    day
                ) + ", " + monthName.capitalizeFirstCharacter() + " " + event.get(Calendar.YEAR) + " " + context.getString(R.string.a) + " " + time
            }
        }
        // il y a 10 hours
        //10 hours Ago
        // 30 minutes ago
        // yesterday 10:22
        // 12, Avril 10:22
        // 12, Avril 2021 10:22
    }


    fun getNumberOfDaysBetweenCalendars(firstCal: Calendar, secondCal: Calendar): Int {
        firstCal.set(Calendar.HOUR_OF_DAY, 0)
        firstCal.set(Calendar.MINUTE, 0)
        firstCal.set(Calendar.SECOND, 0)
        firstCal.set(Calendar.MILLISECOND, 0)
        secondCal.set(Calendar.HOUR_OF_DAY, 0)
        secondCal.set(Calendar.MINUTE, 0)
        secondCal.set(Calendar.SECOND, 0)
        secondCal.set(Calendar.MILLISECOND, 0)
        val millionSeconds = firstCal.timeInMillis - secondCal.timeInMillis
        val dayCount = millionSeconds / (24 * 60 * 60 * 1000)
        return dayCount.toInt()
    }

    fun getNumberOfSecondsBetweenCalendars(firstCal: Calendar, secondCal: Calendar): Int {
        val millionSeconds = firstCal.timeInMillis - secondCal.timeInMillis
        val secondsCount = millionSeconds / (1000)
        return secondsCount.toInt()
    }
}
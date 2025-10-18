package com.ptit.btl_mobile.util

import androidx.room.TypeConverter
import java.util.Date

public object DateConverter {
    @TypeConverter
    public fun toDate(timestamp: Long): Date {
        return Date(timestamp)
    }

    @TypeConverter
    public fun fromDate(date: Date): Long {
        return date.time
    }
}
package com.example.matchmate.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromMatchStatus(status: MatchStatus): String = status.name

    @TypeConverter
    fun toMatchStatus(value: String): MatchStatus = MatchStatus.valueOf(value)
}

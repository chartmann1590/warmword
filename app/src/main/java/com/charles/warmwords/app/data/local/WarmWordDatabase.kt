package com.charles.warmwords.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.charles.warmwords.app.data.local.dao.ChatDao
import com.charles.warmwords.app.data.local.dao.CrisisResourceDao
import com.charles.warmwords.app.data.local.dao.JournalDao
import com.charles.warmwords.app.data.local.dao.MoodDao
import com.charles.warmwords.app.data.local.dao.SessionReminderDao
import com.charles.warmwords.app.data.local.dao.UserProfileDao
import com.charles.warmwords.app.data.local.entity.ChatMessage
import com.charles.warmwords.app.data.local.entity.CrisisResource
import com.charles.warmwords.app.data.local.entity.JournalEntry
import com.charles.warmwords.app.data.local.entity.MoodLog
import com.charles.warmwords.app.data.local.entity.SessionReminder
import com.charles.warmwords.app.data.local.entity.UserProfile

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
}

@Database(
    entities = [
        JournalEntry::class,
        MoodLog::class,
        ChatMessage::class,
        UserProfile::class,
        CrisisResource::class,
        SessionReminder::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WarmWordDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao
    abstract fun moodDao(): MoodDao
    abstract fun chatDao(): ChatDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun crisisResourceDao(): CrisisResourceDao
    abstract fun sessionReminderDao(): SessionReminderDao
}

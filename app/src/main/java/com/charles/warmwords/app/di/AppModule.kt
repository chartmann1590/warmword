package com.charles.warmwords.app.di

import android.content.Context
import androidx.room.Room
import androidx.work.Configuration
import com.charles.warmwords.app.data.local.WarmWordDatabase
import com.charles.warmwords.app.data.local.dao.ChatDao
import com.charles.warmwords.app.data.local.dao.CrisisResourceDao
import com.charles.warmwords.app.data.local.dao.JournalDao
import com.charles.warmwords.app.data.local.dao.MoodDao
import com.charles.warmwords.app.data.local.dao.SessionNoteDao
import com.charles.warmwords.app.data.local.dao.SessionReminderDao
import com.charles.warmwords.app.data.local.dao.UserProfileDao
import com.charles.warmwords.app.util.ModelConfig
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindJournalRepository(impl: com.charles.warmwords.app.data.repository.JournalRepositoryImpl): com.charles.warmwords.app.domain.repository.JournalRepository

    @Binds
    @Singleton
    abstract fun bindMoodRepository(impl: com.charles.warmwords.app.data.repository.MoodRepositoryImpl): com.charles.warmwords.app.domain.repository.MoodRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: com.charles.warmwords.app.data.repository.ChatRepositoryImpl): com.charles.warmwords.app.domain.repository.ChatRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(impl: com.charles.warmwords.app.data.repository.UserProfileRepositoryImpl): com.charles.warmwords.app.domain.repository.UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindSessionReminderRepository(impl: com.charles.warmwords.app.data.repository.SessionReminderRepositoryImpl): com.charles.warmwords.app.domain.repository.SessionReminderRepository

    @Binds
    @Singleton
    abstract fun bindSessionNoteRepository(impl: com.charles.warmwords.app.data.repository.SessionNoteRepositoryImpl): com.charles.warmwords.app.domain.repository.SessionNoteRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WarmWordDatabase {
        return Room.databaseBuilder(
            context,
            WarmWordDatabase::class.java,
            "warmword.db"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    fun provideJournalDao(db: WarmWordDatabase): JournalDao = db.journalDao()

    @Provides
    fun provideMoodDao(db: WarmWordDatabase): MoodDao = db.moodDao()

    @Provides
    fun provideChatDao(db: WarmWordDatabase): ChatDao = db.chatDao()

    @Provides
    fun provideUserProfileDao(db: WarmWordDatabase): UserProfileDao = db.userProfileDao()

    @Provides
    fun provideCrisisResourceDao(db: WarmWordDatabase): CrisisResourceDao = db.crisisResourceDao()

    @Provides
    fun provideSessionReminderDao(db: WarmWordDatabase): SessionReminderDao = db.sessionReminderDao()

    @Provides
    fun provideSessionNoteDao(db: WarmWordDatabase): SessionNoteDao = db.sessionNoteDao()

    @Provides
    @Singleton
    @Named("modelDownloadUrl")
    fun provideModelDownloadUrl(): String = ModelConfig.MODEL_DOWNLOAD_URL

    @Provides
    @Singleton
    @Named("modelSha256")
    fun provideModelSha256(): String = ModelConfig.MODEL_SHA256

    @Provides
    @Singleton
    @Named("modelFileName")
    fun provideModelFileName(): String = ModelConfig.MODEL_FILE_NAME

    @Provides
    @Singleton
    fun provideModelTotalBytes(): Long = ModelConfig.MODEL_TOTAL_BYTES

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): androidx.work.WorkManager =
        androidx.work.WorkManager.getInstance(context)
}

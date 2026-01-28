package by.dreb.tutorhelper.di

import android.content.Context
import androidx.room.Room
import by.dreb.tutorhelper.data.db.TutorHelperDatabase
import by.dreb.tutorhelper.data.db.dao.LessonDao
import by.dreb.tutorhelper.data.db.dao.PaymentDao
import by.dreb.tutorhelper.data.db.dao.StudentDao
import by.dreb.tutorhelper.data.preferences.SettingsDataStore
import by.dreb.tutorhelper.data.repository.LessonRepositoryImpl
import by.dreb.tutorhelper.data.repository.PaymentRepositoryImpl
import by.dreb.tutorhelper.data.repository.SettingsRepositoryImpl
import by.dreb.tutorhelper.data.repository.StudentRepositoryImpl
import by.dreb.tutorhelper.domain.repository.LessonRepository
import by.dreb.tutorhelper.domain.repository.PaymentRepository
import by.dreb.tutorhelper.domain.repository.SettingsRepository
import by.dreb.tutorhelper.domain.repository.StudentRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TutorHelperDatabase =
        Room.databaseBuilder(context, TutorHelperDatabase::class.java, "tutor_helper.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideStudentDao(database: TutorHelperDatabase): StudentDao = database.studentDao()

    @Provides
    fun provideLessonDao(database: TutorHelperDatabase): LessonDao = database.lessonDao()

    @Provides
    fun providePaymentDao(database: TutorHelperDatabase): PaymentDao = database.paymentDao()

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore =
        SettingsDataStore(context)

    @Provides
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindStudentRepository(impl: StudentRepositoryImpl): StudentRepository

    @Binds
    abstract fun bindLessonRepository(impl: LessonRepositoryImpl): LessonRepository

    @Binds
    abstract fun bindPaymentRepository(impl: PaymentRepositoryImpl): PaymentRepository

    @Binds
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}

@Qualifier
annotation class ApplicationScope

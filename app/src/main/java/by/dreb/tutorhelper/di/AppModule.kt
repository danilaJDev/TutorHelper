package by.dreb.tutorhelper.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import by.dreb.tutorhelper.data.db.TutorHelperDatabase
import by.dreb.tutorhelper.data.db.dao.LessonDao
import by.dreb.tutorhelper.data.db.dao.PaymentDao
import by.dreb.tutorhelper.data.db.dao.StudentDao
import by.dreb.tutorhelper.data.repository.LessonRepositoryImpl
import by.dreb.tutorhelper.data.repository.PaymentRepositoryImpl
import by.dreb.tutorhelper.data.repository.StudentRepositoryImpl
import by.dreb.tutorhelper.util.FinanceExporter
import by.dreb.tutorhelper.util.ReminderManager
import by.dreb.tutorhelper.domain.repository.LessonRepository
import by.dreb.tutorhelper.domain.repository.PaymentRepository
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
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE lessons ADD COLUMN isHidden INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE lessons ADD COLUMN isHomeworkSent INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE lessons ADD COLUMN isCompleted INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE students ADD COLUMN defaultPrice REAL NOT NULL DEFAULT 0.0")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE payments ADD COLUMN studentId INTEGER NOT NULL DEFAULT 0")
            db.execSQL("UPDATE payments SET studentId = (SELECT studentId FROM lessons WHERE lessons.id = payments.lessonId) WHERE EXISTS (SELECT 1 FROM lessons WHERE lessons.id = payments.lessonId)")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TutorHelperDatabase =
        Room.databaseBuilder(context, TutorHelperDatabase::class.java, "tutor_helper.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideStudentDao(database: TutorHelperDatabase): StudentDao = database.studentDao()

    @Provides
    fun provideLessonDao(database: TutorHelperDatabase): LessonDao = database.lessonDao()

    @Provides
    fun providePaymentDao(database: TutorHelperDatabase): PaymentDao = database.paymentDao()

    @Provides
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideReminderManager(@ApplicationContext context: Context): ReminderManager =
        ReminderManager(context)

    @Provides
    @Singleton
    fun provideFinanceExporter(@ApplicationContext context: Context): FinanceExporter =
        FinanceExporter(context)
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
}

@Qualifier
annotation class ApplicationScope

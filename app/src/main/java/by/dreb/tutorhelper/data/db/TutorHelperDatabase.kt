package by.dreb.tutorhelper.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import by.dreb.tutorhelper.data.db.dao.LessonDao
import by.dreb.tutorhelper.data.db.dao.PaymentDao
import by.dreb.tutorhelper.data.db.dao.StudentDao
import by.dreb.tutorhelper.data.db.entity.LessonEntity
import by.dreb.tutorhelper.data.db.entity.PaymentEntity
import by.dreb.tutorhelper.data.db.entity.StudentEntity

@Database(
    entities = [StudentEntity::class, LessonEntity::class, PaymentEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(TutorHelperTypeConverters::class)
abstract class TutorHelperDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun lessonDao(): LessonDao
    abstract fun paymentDao(): PaymentDao
}

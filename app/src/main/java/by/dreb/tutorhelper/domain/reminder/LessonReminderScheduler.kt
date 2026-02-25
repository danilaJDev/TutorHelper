package by.dreb.tutorhelper.domain.reminder

import by.dreb.tutorhelper.domain.model.Lesson

interface LessonReminderScheduler {
    fun scheduleOrCancel(lesson: Lesson, studentName: String)
    fun cancel(lessonId: Long)
}

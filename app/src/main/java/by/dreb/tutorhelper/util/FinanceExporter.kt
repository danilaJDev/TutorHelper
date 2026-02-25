package by.dreb.tutorhelper.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.domain.model.LessonDetails
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FinanceExporter(private val context: Context) {

    fun exportFinanceToCsv(lessons: List<LessonDetails>) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
        val fileName = context.getString(R.string.export_filename_finance, timestamp)
        val file = File(context.cacheDir, fileName)

        file.writeText(generateCsvContent(lessons))

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.export_title))
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, context.getString(R.string.action_export)))
    }

    private fun generateCsvContent(lessons: List<LessonDetails>): String {
        val sb = StringBuilder()
        // Header
        sb.append("Дата;Студент;Предмет;Цена;Статус;Оплачено;Метод оплаты;Заметка\n")

        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

        lessons.forEach { details ->
            val status = if (details.lesson.isCompleted) "Проведено" else "Запланировано"
            val paidStatus = if (details.payment != null) "Да" else "Нет"
            val paymentMethod = details.payment?.method ?: ""
            val note = details.lesson.note?.replace("\n", " ") ?: ""

            sb.append("${details.lesson.startTime.format(dateFormatter)};")
            sb.append("${details.student.name};")
            sb.append("${details.lesson.subject};")
            sb.append("${details.lesson.price};")
            sb.append("$status;")
            sb.append("$paidStatus;")
            sb.append("$paymentMethod;")
            sb.append("$note\n")
        }

        return sb.toString()
    }
}

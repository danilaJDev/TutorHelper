package by.dreb.tutorhelper.domain.usecase

import by.dreb.tutorhelper.domain.repository.StudentRepository
import javax.inject.Inject

class GetStudentsUseCase @Inject constructor(
    private val studentRepository: StudentRepository
) {
    operator fun invoke(isArchived: Boolean) = studentRepository.observeStudents(isArchived)
}

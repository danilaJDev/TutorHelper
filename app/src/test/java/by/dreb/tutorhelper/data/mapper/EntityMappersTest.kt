package by.dreb.tutorhelper.data.mapper

import by.dreb.tutorhelper.data.db.entity.StudentEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class EntityMappersTest {
    @Test
    fun student_entity_to_domain_mapping_is_correct() {
        val entity = StudentEntity(
            id = 1,
            name = "Test Student",
            phone = "123456",
            note = "Test Note",
            isArchived = false
        )
        val domain = entity.toDomain()

        assertEquals(entity.id, domain.id)
        assertEquals(entity.name, domain.name)
        assertEquals(entity.phone, domain.phone)
        assertEquals(entity.note, domain.note)
        assertEquals(entity.isArchived, domain.isArchived)
    }
}

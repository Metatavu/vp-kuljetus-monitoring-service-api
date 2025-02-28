package fi.metatavu.example.api.test.functional.tests

import fi.metatavu.example.client.models.*
import fi.metatavu.example.api.test.functional.TestBuilder
import fi.metatavu.example.api.test.functional.resources.LocalTestProfile
import fi.metatavu.example.api.test.functional.resources.MysqlTestResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests for Examples
 *
 * @author Jari Nykänen
 * @author Antti Leppä
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlTestResource::class)
)
@TestProfile(LocalTestProfile::class)
class ExamplesResourceTest {

    /**
     * Tests list Examples
     */
    @Test
    fun listExamples() {
        TestBuilder().use {
            val emptyList = it.manager.examples.listExamples(
                firstResult = null,
                maxResults = null
            )

            assertEquals(0, emptyList.size)

            it.manager.examples.createDefaultExample()
            it.manager.examples.createDefaultExample()

            val listWithTwoEntries = it.manager.examples.listExamples(
                firstResult = null,
                maxResults = null
            )

            assertEquals(2, listWithTwoEntries.size)
        }
    }

    /**
     * Tests creating Example
     */
    @Test
    fun createExample() {
        TestBuilder().use {
            val createdExample = it.manager.examples.createDefaultExample()
            assertNotNull(createdExample)
            assertNotNull(createdExample.name)
            assertNotNull(createdExample.amount)
        }
    }

    /**
     * Tests finding Example
     */
    @Test
    fun findExample() {
        TestBuilder().use {
            val createdExample = it.manager.examples.createDefaultExample()
            assertNotNull(createdExample)

            val foundExample = it.manager.examples.findExample(createdExample.id!!)
            assertNotNull(foundExample)
        }
    }

    /**
     * Tests updating Example
     */
    @Test
    fun updateExample() {
        TestBuilder().use {
            val createdExample = it.manager.examples.createDefaultExample()
            assertNotNull(createdExample)

            val exampleToUpdate = Example(
                name = "Updated name",
                amount = 200
            )


            val updatedExample = it.manager.examples.updateExample(createdExample.id!!, exampleToUpdate)
            assertEquals(updatedExample.name, exampleToUpdate.name)
            assertEquals(updatedExample.amount, exampleToUpdate.amount)
        }
    }

    /**
     * Tests deleting Example
     */
    @Test
    fun deleteExample() {
        TestBuilder().use {
            val createdExample = it.manager.examples.createDefaultExample()
            assertNotNull(createdExample)

            it.manager.examples.deleteExample(createdExample.id!!)

            val emptyListAfterDelete = it.manager.examples.listExamples(
                firstResult = null,
                maxResults = null
            )

            assertEquals(0, emptyListAfterDelete.size)
        }
    }
}
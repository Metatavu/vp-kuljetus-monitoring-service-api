package fi.metatavu.example.api.example

import fi.metatavu.example.api.persistence.dao.ExampleDAO
import fi.metatavu.example.api.persistence.model.Example
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller class for examples
 *
 * @author Jari Nykänen
 */
@ApplicationScoped
class ExamplesController {

    @Inject
    private lateinit var exampleDAO: ExampleDAO

    /**
     * Creates a new Example
     *
     * @param name name
     * @param amount amount
     * @param creatorId Creator Id
     * @return created counter frame
     */
    fun create (name: String, amount: Int, creatorId: UUID): Example {
        return exampleDAO.create(
            id = UUID.randomUUID(),
            name= name,
            amount = amount,
            creatorId = creatorId
        )
    }

    /**
     * Finds a example from the database
     *
     * @param exampleId example id to find
     * @return example or null if not found
     */
    fun findExample(exampleId: UUID): Example? {
        return exampleDAO.findById(exampleId)
    }

    /**
     * Updates example
     *
     * @param example example to update
     * @param name name
     * @param amount amount
     * @param modifierId modifierId
     * @return Updated Example
     */
    fun update(example: Example, name: String, amount: Int, modifierId: UUID): Example {
        val result = exampleDAO.updateName(example, name, modifierId)
        exampleDAO.updateAmount(result, amount, modifierId)

        return result
    }

    /**
     * List examples
     *
     * @param firstResult First result. Defaults to 0 (optional)
     * @param maxResults Max results. Defaults to 10 (optional)
     * @return list of languages
     */
    fun list(firstResult: Int?, maxResults: Int?): List<Example> {
        return exampleDAO.list(
            firstResult = firstResult,
            maxResults = maxResults
        )
    }

    /**
     * Deletes a example from the database
     *
     * @param example example to delete
     */
    fun deleteExample(example: Example) {
        return exampleDAO.delete(example)
    }
}
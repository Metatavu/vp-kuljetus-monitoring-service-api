package fi.metatavu.example.api.impl

import fi.metatavu.example.api.example.ExamplesController
import fi.metatavu.example.api.impl.translate.ExamplesTranslator
import fi.metatavu.example.model.Example
import fi.metatavu.example.spec.ExamplesApi
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

/**
 * Example API implementation
 *
 * @author Jari Nykänen
 * @author Antti Leppä
 */
@RequestScoped
@Transactional
class ExamplesApiImpl: ExamplesApi, AbstractApi()  {

    @Inject
    private lateinit var examplesController: ExamplesController

    @Inject
    private lateinit var examplesTranslator: ExamplesTranslator

    /* EXAMPLES */

    override suspend fun listExamples(firstResult: Int?, maxResults: Int?): Response {
        loggedUserId ?: return createUnauthorized(NO_VALID_USER_MESSAGE)
        val examples = examplesController.list(
            firstResult = firstResult,
            maxResults = maxResults
        )

        return createOk(examples.map(examplesTranslator::translate))
    }

    override suspend fun createExample(example: Example): Response {
        val userId = loggedUserId ?: return createUnauthorized(NO_VALID_USER_MESSAGE)
        val name = example.name
        val amount = example.amount

        val createdExample = examplesController.create(
            name = name,
            amount = amount,
            creatorId = userId
        )

        return createOk(examplesTranslator.translate(createdExample))
    }

    override suspend fun findExample(exampleId: UUID): Response {
        loggedUserId ?: return createUnauthorized(NO_VALID_USER_MESSAGE)
        val foundExample = examplesController.findExample(exampleId) ?: return createNotFound("Example with ID $exampleId could not be found")
        return createOk(examplesTranslator.translate(foundExample))

    }

    override suspend fun updateExample(exampleId: UUID, example: Example): Response {
        val userId = loggedUserId ?: return createUnauthorized(NO_VALID_USER_MESSAGE)
        val name = example.name
        val amount = example.amount

        val exampleToUpdate = examplesController.findExample(exampleId) ?: return createNotFound("Example with ID $exampleId could not be found")
        val updatedExample = examplesController.update(
            example = exampleToUpdate,
            name = name,
            amount = amount,
            modifierId = userId
        )

        return createOk(examplesTranslator.translate(updatedExample))
    }

    override suspend fun deleteExample(exampleId: UUID): Response {
        loggedUserId ?: return createUnauthorized(NO_VALID_USER_MESSAGE)
        val foundExample = examplesController.findExample(exampleId) ?: return createNotFound("Example with ID $exampleId could not be found")
        examplesController.deleteExample(foundExample)

        return createNoContent()
    }

    companion object {
        const val NO_VALID_USER_MESSAGE = "No valid user!"
    }

}

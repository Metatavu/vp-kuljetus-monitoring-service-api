package fi.metatavu.example.api.persistence.dao

import fi.metatavu.example.api.persistence.model.Example
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.criteria.Predicate

/**
 * DAO class for examples
 *
 * @author Jari Nykänen
 */
@ApplicationScoped
class ExampleDAO : AbstractDAO<Example>() {

    /**
     * Creates a new Example
     *
     * @param id id
     * @param name name
     * @param amount amount
     * @param creatorId creator id
     * @return created example
     */
    fun create(id: UUID, name: String, amount: Int, creatorId: UUID): Example {
        val result = Example()
        result.id = id
        result.name = name
        result.amount = amount
        result.creatorId = creatorId
        result.lastModifierId = creatorId
        return persist(result)
    }

    /**
     * Lists all examples with given filters
     *
     * @param firstResult First result. Defaults to 0 (optional)
     * @param maxResults Max results. Defaults to 10 (optional)
     * @return list of examples
     */
    fun list(firstResult: Int?, maxResults: Int?): List<Example> {
        val entityManager = getEntityManager()
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria = criteriaBuilder.createQuery(Example::class.java)
        val root = criteria.from(Example::class.java)

        criteria.select(root)
        val restrictions = ArrayList<Predicate>()

        criteria.where(criteriaBuilder.and(*restrictions.toTypedArray()))

        val query = entityManager.createQuery(criteria)
        if (firstResult != null) {
            query.firstResult = firstResult
        }

        if (maxResults != null) {
            query.maxResults = maxResults
        }

        return query.resultList
    }

    /**
     * Updates example title
     *
     * @param example example to update
     * @param name name
     * @param modifierId modifier id
     * @return updated example
     */
    fun updateName(example: Example, name: String, modifierId: UUID): Example {
        example.name = name
        example.lastModifierId = modifierId
        return persist(example)
    }

    /**
     * Updates example amount
     *
     * @param example example to update
     * @param amount amount
     * @param modifierId modifier id
     * @return updated example
     */
    fun updateAmount(example: Example, amount: Int, modifierId: UUID): Example {
        example.amount = amount
        example.lastModifierId = modifierId
        return persist(example)
    }

}
package fi.metatavu.example.api.impl.translate

/**
 * Abstract translator class
 *
 * @author Jari Nykänen
 */
abstract class AbstractTranslator<E, R> {

    abstract fun translate(entity: E): R

    /**
     * Translates list of entities
     *
     * @param entities list of entities to translate
     * @return List of translated entities
     */
    open fun translate(entities: List<E>): List<R> {
        return entities.mapNotNull(this::translate)
    }

}
package fi.metatavu.example.api.impl.translate

import fi.metatavu.example.api.persistence.model.Example
import javax.enterprise.context.ApplicationScoped

/**
 * Translator class for Examples
 */
@ApplicationScoped
class ExamplesTranslator: AbstractTranslator<Example, fi.metatavu.example.model.Example>() {

    override fun translate(entity: Example): fi.metatavu.example.model.Example {
        return fi.metatavu.example.model.Example(
            id = entity.id,
            name = entity.name!!,
            amount = entity.amount!!
        )
    }

}
package io.github.jan.einkaufszettel.common.cache

import io.github.jan.einkaufszettel.common.repositories.card.Card
import kotlinx.serialization.builtins.ListSerializer

class CardCacheManager : JsonCacheManager<Card> {

    override val fileName = "cards.json"
    override val serializer = ListSerializer(Card.serializer())

}
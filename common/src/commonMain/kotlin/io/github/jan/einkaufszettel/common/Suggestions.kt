package io.github.jan.einkaufszettel.common

object Suggestions {

    private val ALL = listOf(
        "Tabasco",
        "Gurke",
        "Kartoffel",
        "Käse",
        "Tomate",
        "Salatzeug",
        "Zwiebeln",
        "Paprika",
        "Tomatenzeug",
        "Süßigkeiten",
        "Chips",
        "Brotbelag",
        "Habanero",
        "Pepperoni",
        "Tiefkühlpizza",
        "Olivenöl",
        "Aceto Balsamico",
        "Ketchup",
        "Sonnenblumenöl",
        "Kokosmilch",
        "Rapsöl",
        "Eisbergsalat",
        "Knoblauch",
        "Milch",
        "Avocado",
        "Eistee",
        "Mayonese",
        "Sprudel",
        "Wasser für Jan",
        "Bananen",
        "Äpfel",
        "Mais",
        "Kidney Bohnen",
        "Miracel Whip",
        "Cornflakes",
        "Weißwürste",
        "Cola",
        "Kaffee",
        "Kakao",
        "Karotte",
        "Fusilli",
        "Penne",
        "Salzstängel",
        "Salzbrezeln",
        "Cola",
        "Zahnbürste",
        "Curry",
        "Weltmeisterbrötchen",
        "Schär Mix Brot",
        "Cetirizin",
        "Rasierwasser",
        "Lidl-Mehl-Glutenfrei"
    )

    fun getSuggestions(term: String): List<String> {
        return ALL.filter { it.lowercase().contains(term.lowercase()) }
    }

}

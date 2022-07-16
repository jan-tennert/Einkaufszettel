package io.github.jan.einkaufszettel.common.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NewScreen() {
    val darkMode = !MaterialTheme.colors.isLight
    LazyColumn {
        News.NEWS.forEach { (version, news) ->
            item {
                Text("Neu in der Version $version:", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
            item {
                news.forEach { new ->
                    Row(Modifier.padding(8.dp),verticalAlignment = Alignment.CenterVertically) {
                        Canvas(modifier = Modifier.padding(start = 8.dp,end = 8.dp).size(6.dp)){
                            drawCircle(if(darkMode) Color.White else Color.Black)
                        }
                        Text(text = new, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

object News {

    private val allNews = mutableListOf<Pair<Int, List<String>>>()
    val NEWS get() = allNews.reversed()

    val VERSION_10 = createNews(10, listOf(
        "Neues Feature: Change Log",
        "Neues Feature: Man sieht jetzt bei den Produkten das Durchstreich Datum und den, der das Produkt durchgestrichen hat",
        "Besseres Design bei den Produkten"
    ))

    val VERSION_11 = createNews(11, listOf(
        "Neues Feature: Items bearbeiten indem man länger auf ein Item drückt",
        "Neues Feature: Einstellungen",
        "Besseres Design",
        "Neue Caching Methode"
    ))

    val VERSION_12 = createNews(12, listOf(
        "Es wird nun im Hintergrund nach Updates gesucht",
    ))

    val VERSION_14 = createNews(14, listOf(
        "Es gibt nun einen neuen Tab \"Anhänge\"",
    ))

    val VERSION_15 = createNews(15, listOf(
        "Neues Feature: Progress Bar beim runterladen von einem Update",
        "Mehr Performance durch Code optimierung durch View Models"
    ))

    val VERSION_16 = createNews(16, listOf(
        "Neues Feature: Man kann jetzt die Items selber verschieben",
        "Versuch das Mobile Daten Problem zu umgehen",
    ))

    val VERSION_17 = createNews(17, listOf(
        "Der Changelog ist jetzt anders rum (:",
        "Fehler behoben der den Verschiebebutton nicht zenteriert",
        "Fehler behoben der die Items nicht richtig anzeigen lässt, wenn sie größer als 1 Zeile sind",
        "Einstellungsoption optimiert",
        "Changelog im Darkmode optimiert"
    ))

    val VERSION_18 = createNews(18, listOf(
        "Wenn man keine Internetverbindung hat und die App startet, wird man gewarnt das die Liste vom Cache geladen wird",
        "Error Dialoge wurden optimiert",
        "Neues App Icon",
        "Neues Design",
        "Drag and Drop wurde behoben"
    ))

    val VERSION_19 = createNews(19, listOf(
        "Mobile Daten Problem behoben",
        "Dark Mode farben optimiert",
        "Der Refresh Button ist zurück"
    ))

    val VERSION_20 = createNews(20, listOf(
        "Cache leeren Button gelöscht (Daten löschen verwenden)",
        "Daten löschen wurde behoben"
    ))

    val VERSION_21 = createNews(21, listOf(
        "Zurück Button bei den Anhängen verschoben",
        "Error Dialoge optimiert",
    ))

    val VERSION_22 = createNews(22, listOf(
        "Es wird nun ein eigenes DNS Plugin für den HTTP Client verwendet",
    ))

    val VERSION_24 = createNews(24, listOf(
        "Man kann jetzt beim Verschieben von Produkten die Produkte nicht mehr 'überdraggen'",
    ))

    val VERSION_25 = createNews(25, listOf(
        "Man kann nun Produkte über einen Bar Code scannen (Informationen sehen) und in die Einkaufsliste hinzufügen",
        "Code optimiert"
    ))

    val VERSION_29 = createNews(29, listOf(
        "Shops werden jetzt über Accounts gespeichert",
        "Man kann bei Karten und Shops jetzt auswählen wer sie sehen darf (siehe nächster Punkt)",
        "Man kann bei Bekannte Nutzer den QR Code vom User scannen, oder die ID manuell eingeben um in hinzuzufügen.",
    ))

    val VERSION_30 = createNews(30, listOf(
        "Bugs gefixt",
        "Man kann jetzt beim Passwort ändern & eingeben die Sichtbarkeit umstellen",
        "Beim Gruppen Icon bei Shops kann man jetzt direkt Nutzer in die \"Bekannte Nutzer\" Liste hinzufügen"
    ))

    val VERSION_31 = createNews(31, listOf(
        "Bugs gefixt"
    ))

    val VERSION_33 = createNews(33, listOf(
        "Es wird nun eine superclass für das Cache Management verwendet"
    ))

    val VERSION_34 = createNews(34, listOf(
        "Die \"Gekauft\" und \"Löschen\" Buttons der Einkaufszettel Produkte sind nun getrennt",
        "Verschieben kann man Produkte wenn man auf den Edit-Button in der Leiste oben drückt, gespeichert wird die Reihenfolge wenn man auf den Fertig-Button drückt"
    ))

    val VERSION_35 = createNews(35, listOf(
        "Desktop Version",
        "Man kann nun bei den Produkten durch das scrollen alle Shops neuladen",
        "Man kann beim Home Screen nun Shops einklappen"
    ))

    private fun createNews(version: Int, news: List<String>) {
        allNews += version to news
    }

}
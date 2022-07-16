package io.github.jan.einkaufszettel.android.modules

import io.github.jan.einkaufszettel.android.AutoUpdater
import org.koin.dsl.module

val autoUpdaterModule = module {
    includes(appModule)
    single {
        AutoUpdater(get())
    }
}

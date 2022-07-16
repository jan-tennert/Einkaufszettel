package io.github.jan.einkaufszettel.android

import android.app.Application
import io.github.jan.einkaufszettel.android.modules.appModule
import io.github.jan.einkaufszettel.android.modules.autoUpdaterModule
import io.github.jan.einkaufszettel.common.modules.cacheModule
import io.github.jan.einkaufszettel.common.modules.networkModule
import io.github.jan.einkaufszettel.common.modules.productInfoModule
import io.github.jan.einkaufszettel.common.modules.productModule
import io.github.jan.einkaufszettel.common.modules.profileModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApplication)
            modules(appModule, productModule, productInfoModule, autoUpdaterModule, profileModule, cacheModule, networkModule)
        }
    }

}
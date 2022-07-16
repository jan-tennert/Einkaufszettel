package io.github.jan.einkaufszettel.android.modules

import io.github.jan.einkaufszettel.android.viewmodel.ProductViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel {
        ProductViewModel(get(), get(), get(), get(), get(), get(), get(), get())
    }
}
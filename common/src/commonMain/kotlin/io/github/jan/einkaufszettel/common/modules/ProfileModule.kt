package io.github.jan.einkaufszettel.common.modules

import io.github.jan.einkaufszettel.common.repositories.ProfileRepository
import io.github.jan.einkaufszettel.common.repositories.ProfileRepositoryImpl
import org.koin.dsl.module

val profileModule = module {
    single<ProfileRepository> {
        ProfileRepositoryImpl(get())
    }

}
package com.ptit.btl_mobile.util

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import kotlin.reflect.KClass

fun <T: Any> NavDestination.isRoute(route: KClass<T>): Boolean {
    return this.hierarchy.any {
        it.hasRoute(route)
    }
}
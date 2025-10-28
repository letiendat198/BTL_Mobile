package com.ptit.btl_mobile.model.datastore

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {
    val MEDIASTORE_GENERATION = longPreferencesKey("MEDIASTORE_GENERATION")
    val MEDIASTORE_VERSION = stringPreferencesKey("MEDIASTORE_VERSION")
}
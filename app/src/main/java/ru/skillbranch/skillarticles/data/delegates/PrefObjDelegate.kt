package ru.skillbranch.skillarticles.data.delegates

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.skillbranch.skillarticles.data.PrefManager
import ru.skillbranch.skillarticles.data.adapters.JsonAdapter
import ru.skillbranch.skillarticles.data.adapters.UserJsonAdapter
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PrefObjDelegate<T>(private val adapter : JsonAdapter<T>, private val customKey : String? = null) : ReadWriteProperty<PrefManager, T?> {

    private var storedValue : String? = null

    override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T?) {
        storedValue = adapter.toJson(value)
        val key = stringPreferencesKey(customKey ?: property.name)

        thisRef.scope.launch {
            thisRef.dataStore.edit { pref ->
                pref[key] = storedValue!!
            }
        }
    }

    override fun getValue(thisRef: PrefManager, property: KProperty<*>): T? {
        if (storedValue == null) {
             val flowValue = thisRef.dataStore.data
                     .map { pref ->
                         pref[stringPreferencesKey(customKey ?: property.name)]
                     }

            storedValue = runBlocking(Dispatchers.IO) { flowValue.first() }
        }

        storedValue?.let {
            return adapter.fromJson(it)
        }

        return null
    }

}
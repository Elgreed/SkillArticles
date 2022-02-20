package ru.skillbranch.skillarticles.data.delegates

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.skillbranch.skillarticles.data.PrefManager
import ru.skillbranch.skillarticles.data.adapters.JsonAdapter
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PrefObjDelegate<T> (private val adapter: JsonAdapter<T>, private val customKey:String? = null):ReadWriteProperty<PrefManager, T?> {

    var _storedValue: String? = null
    override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T?) {
        val key = stringPreferencesKey(customKey ?: property.name)
        _storedValue = adapter.toJson(value)
        thisRef.scope.launch {
            thisRef.dataStore.edit { prefs ->
                prefs[key] = adapter.toJson(value)
            }
        }
    }

    override fun getValue(thisRef: PrefManager, property: KProperty<*>): T? {
        val key = stringPreferencesKey(customKey ?: property.name)
        if (_storedValue == null){
            val flowValue = thisRef.dataStore.data
                .map { prefs ->
                    prefs[key]
                }
            _storedValue = runBlocking(Dispatchers.IO) { flowValue.first() }
        }
        return adapter.fromJson(_storedValue ?: "")
    }
}
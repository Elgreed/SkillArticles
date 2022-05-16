package ru.skillbranch.skillarticles.data.delegates

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.skillbranch.skillarticles.data.PrefManager
import ru.skillbranch.skillarticles.data.adapters.JsonAdapter
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Делегат, возвращающий десериализованный результат из DataStore типа T
 */
class PrefObjDelegate<T>(private val adapter: JsonAdapter<T>,
                         private val customKey: String? = null) {
    operator fun provideDelegate(
        thisRef: PrefManager,
        prop: KProperty<*>
    ): ReadWriteProperty<PrefManager, T?> {
        return object : ReadWriteProperty<PrefManager, T?> {
            var _storedValue: T? = null
            var key = stringPreferencesKey(customKey ?: prop.name)

            override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T?) {
                _storedValue = value
                //Сериализацию лучше проводить асинхронно.
                @Suppress("UNCHECKED_CAST")
                thisRef.scope.launch {
                    thisRef.dataStore.edit { prefs ->
                        prefs[key] = adapter.toJson(value)
                    }
                }
            }

            override fun getValue(thisRef: PrefManager, property: KProperty<*>): T? {
                if (_storedValue == null) {
                    // async flow
                    //Делать flowValue var нет нужды, также парсинг можно переместить в map,
                    // так как вся цепочка вызовов в flow сработает только тогда, когда
                    // мы будем читать из него значения.
                    val flowValue = thisRef.dataStore.data.map {
                        pref -> adapter.fromJson(pref[key] ?: "")
                    }
                    // sync read
                    _storedValue = runBlocking{ flowValue.first() }
                }
                return _storedValue
            }

        }
    }
}
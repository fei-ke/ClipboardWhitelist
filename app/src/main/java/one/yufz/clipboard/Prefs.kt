package one.yufz.clipboard

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class Prefs(context: Context) {
    companion object {
        const val PREF_NAME = "white_list"
    }

    private val pref = try {
        context.getSharedPreferences(PREF_NAME, Context.MODE_WORLD_READABLE)
    } catch (e: SecurityException) {
        null
    }

    val available: Boolean = pref != null

    fun putPackage(pkgName: String) {
        pref?.run {
            edit().putBoolean(pkgName, true).commit()
        }
    }

    fun removePackage(pkgName: String) {
        pref?.run {
            edit().remove(pkgName).commit()
        }
    }

    fun getAll(): Set<String> {
        return pref?.all?.keys ?: emptySet()
    }

    fun flow(): Flow<Set<String>> = callbackFlow {
        val onChange: () -> Unit = {
            trySendBlocking(getAll())
        }

        val onChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> onChange() }

        pref?.registerOnSharedPreferenceChangeListener(onChangeListener)

        awaitClose {
            pref?.unregisterOnSharedPreferenceChangeListener(onChangeListener)
        }
    }
}
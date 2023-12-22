package one.yufz.clipboard.app

import android.app.Application
import com.airbnb.mvrx.Mavericks

class App : Application() {
    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        Mavericks.initialize(this)
        instance = this

    }
}
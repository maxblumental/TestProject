package happy.blumental.maxim.testproject

import android.app.Application
import com.parse.Parse
import com.parse.ParseObject
import happy.blumental.maxim.testproject.data.PTodoItem

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Parse.enableLocalDatastore(applicationContext)
        ParseObject.registerSubclass(PTodoItem::class.java)
        Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key))
    }
}
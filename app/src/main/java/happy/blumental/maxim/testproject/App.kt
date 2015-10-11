package happy.blumental.maxim.testproject

import android.app.Application
import com.parse.Parse
import com.parse.ParseObject
import happy.blumental.maxim.testproject.data.PTodoItem

class App : Application() {

    override fun onCreate() {
        super.onCreate()

      //  ParseObject.registerSubclass(PTodoItem::class.java)
      //  Parse.initialize(this, BuildConfig.PARSE_APP_ID, BuildConfig.PARSE_CLIENT_KEY)
    }
}
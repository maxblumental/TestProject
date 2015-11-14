package happy.blumental.maxim.testproject.mvc

import android.content.Context
import android.net.ConnectivityManager
import android.os.Process
import android.support.v7.app.AlertDialog

object NetworkManager {

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        if (networkInfo == null || !networkInfo.isConnected)
            return false

        return true
    }

    private fun showNoInternetConnectionAlert(context: Context) {
        val alertDialog = AlertDialog.Builder(context)

        alertDialog.setTitle("No internet connection")
                .setMessage("Unable to continue.")
                .setCancelable(true)
                .setNegativeButton("OK", { dialog, which -> kill() })
                .setOnCancelListener { dialog -> kill() }

        alertDialog.create().show()
    }

    private fun kill() {
        Process.killProcess(Process.myPid())
    }

    public fun checkInternetConnection(context: Context) {
        if (!isNetworkAvailable(context)) {
            showNoInternetConnectionAlert(context)
        }
    }
}
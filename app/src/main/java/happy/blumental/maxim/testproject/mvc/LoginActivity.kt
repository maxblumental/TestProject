package happy.blumental.maxim.testproject.mvc

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.EditText
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import happy.blumental.maxim.testproject.R

class LoginActivity : RxAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val loginEditText = findViewById(R.id.loginEditText) as EditText
        val passwordEditText = findViewById(R.id.passwordEditText) as EditText
        val logInButton = findViewById(R.id.logInButton)

        logInButton.setOnClickListener {
            view ->
            val login = loginEditText.text.toString()
            val pswd = passwordEditText.text.toString()

            if ( login.equals("") || pswd.equals(""))
                showMissingCredentialsAlert()
        }
    }

    private fun showMissingCredentialsAlert() {
        val alertDialog = AlertDialog.Builder(this)

        alertDialog.setTitle("You forgot something")
                .setMessage("Either login or password is missing.")
                .setCancelable(true)
                .setNegativeButton("OK", { dialog, which -> dialog.cancel() })
                .setOnCancelListener { dialog -> dialog.cancel() }

        alertDialog.create().show()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }
}
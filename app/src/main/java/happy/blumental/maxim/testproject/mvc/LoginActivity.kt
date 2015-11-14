package happy.blumental.maxim.testproject.mvc

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.Toast
import com.parse.ParseException
import com.parse.ParseUser
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import happy.blumental.maxim.testproject.MainActivity
import happy.blumental.maxim.testproject.R

class LoginActivity : RxAppCompatActivity() {

    lateinit var loginEditText: EditText
    lateinit var passwordEditText: EditText
    val LOGIN_KEY = "login_key"
    val PASSWORD_KEY = "password_key"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        loginEditText = findViewById(R.id.loginEditText) as EditText
        passwordEditText = findViewById(R.id.passwordEditText) as EditText

        passwordEditText.transformationMethod = PasswordTransformationMethod()

        val logInButton = findViewById(R.id.logInButton)
        val signUpButton = findViewById(R.id.signUpButton)

        logInButton.setOnClickListener {
            view ->
            logIn(loginEditText.text.toString(),
                    passwordEditText.text.toString())
        }

        signUpButton.setOnClickListener {
            view ->
            signUp(loginEditText.text.toString(),
                    passwordEditText.text.toString())
        }
    }

    private fun setLoginAndPassword(loginEditText: EditText, passwordEditText: EditText) {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        loginEditText.setText(sharedPref.getString(LOGIN_KEY, ""))
        passwordEditText.setText(sharedPref.getString(PASSWORD_KEY, ""))
    }

    private fun signUp(login: String, pswd: String) {
        NetworkManager.checkInternetConnection(this)

        val user = ParseUser();
        user.setUsername(login);
        user.setPassword(pswd);

        user.signUpInBackground(
                { e ->
                    if (e == null) {
                        saveLoginAndPassword()
                        launchMainActivity()
                    } else {
                        // Sign up didn't succeed. Look at the ParseException
                        // to figure out what went wrong
                        when (e.getCode()) {
                            ParseException.USERNAME_TAKEN -> showNameTakenAlert()
                            ParseException.OTHER_CAUSE -> showOtherCauseAlert()
                            else -> {
                                Toast.makeText(this, "Failed with exception ${e.getCode()}.",
                                        Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
    }

    private fun saveLoginAndPassword() {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        val editor = sharedPref.edit()
        editor.putString(PASSWORD_KEY, passwordEditText.text.toString());
        editor.putString(LOGIN_KEY, loginEditText.text.toString());
        editor.commit();
    }

    private fun logIn(login: String, pswd: String) {

        NetworkManager.checkInternetConnection(this)

        if ( login.equals("") || pswd.equals("")) {
            showMissingCredentialsAlert()
            return
        }

        //check if there's a user with such credentials
        ParseUser.logInInBackground(login, pswd,
                {
                    user, e ->
                    if (user != null) {
                        saveLoginAndPassword()
                        launchMainActivity()
                    } else {
                        // Signup failed. Look at the ParseException to see what happened.
                        when (e.getCode()) {
                            ParseException.OBJECT_NOT_FOUND -> showNoSuchUserAlert()
                            else -> Toast.makeText(this, "Failed with exception ${e.getCode()}.",
                                    Toast.LENGTH_SHORT).show()
                        }
                    }
                })
    }

    private fun launchMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun showNoSuchUserAlert() {
        val alertDialog = AlertDialog.Builder(this)

        alertDialog.setTitle("No such user")
                .setMessage("You either mistyped the credentials or should sign up first.")
                .setCancelable(true)
                .setNegativeButton("OK", { dialog, which -> dialog.cancel() })
                .setOnCancelListener { dialog -> dialog.cancel() }

        alertDialog.create().show()
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

    private fun showNameTakenAlert() {
        val alertDialog = AlertDialog.Builder(this)

        alertDialog.setTitle("The login is taken")
                .setMessage("Please select another login.")
                .setCancelable(true)
                .setNegativeButton("OK", { dialog, which -> dialog.cancel() })
                .setOnCancelListener { dialog -> dialog.cancel() }

        alertDialog.create().show()
    }

    private fun showOtherCauseAlert() {
        val alertDialog = AlertDialog.Builder(this)

        alertDialog.setTitle("Something went wrong")
                .setMessage("Make sure you provided both login and password.")
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

        if (ParseUser.getCurrentUser() != null) {
            setLoginAndPassword(loginEditText, passwordEditText)
        }
    }
}
package coraythan.decksofkeyforgeutilities.ui.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import coraythan.decksofkeyforgeutilities.*
import coraythan.decksofkeyforgeutilities.ui.loggedin.LoggedInActivity

class FindUserOrRedirectActivity : AppCompatActivity() {

     lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_find_user_or_redirect)

        val authToken = this.appPrefs().findAuthToken()

        if (authToken == null) {
            Log.i("redirect", "No auth token, send to login.")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        } else {
            DoKService.findUserInfo(authToken,this) { username, error ->
                if (username != null) {
                    Log.i("redirect", "Auth token worked for finding user info.")
                    val intent = Intent(this, LoggedInActivity::class.java).apply {
                        putExtra(USERNAME_EXTRA, username)
                    }
                    startActivity(intent)
                } else {
                    Log.i("redirect", "Auth token didn't work, redirect to login activity.")
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
            }
        }

    }


}

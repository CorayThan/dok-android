package coraythan.decksofkeyforgeutilities.ui.loggedin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import coraythan.decksofkeyforgeutilities.DoKService
import coraythan.decksofkeyforgeutilities.R
import coraythan.decksofkeyforgeutilities.ui.login.LoginActivity
import coraythan.decksofkeyforgeutilities.ui.login.USERNAME_EXTRA


class LoggedInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_logged_in)

        val username = intent.getStringExtra(USERNAME_EXTRA)

        findViewById<TextView>(R.id.welcome).apply {
            text = """
                Hey $username! 
                               
                Because you're logged into the DoK Utilities tool you can use the share button in Master Vault after scanning a deck to import that deck to
                your decks on Decks of KeyForge.
                
                Feel free to minimize or close this app.
            """.trimIndent()
        }

        val logout = findViewById<Button>(R.id.logout)

        logout.setOnClickListener {

            DoKService.logout(this)

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

}

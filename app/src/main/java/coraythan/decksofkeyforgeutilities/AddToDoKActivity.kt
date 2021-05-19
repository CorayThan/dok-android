package coraythan.decksofkeyforgeutilities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coraythan.decksofkeyforgeutilities.ui.login.LoginActivity

class AddToDoKActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_find_user_or_redirect)

        val authToken = this.appPrefs().findAuthToken()

        if (authToken == null) {
            Toast.makeText(this, "Login and then share the deck to this app again.", Toast.LENGTH_LONG).show()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        } else {
            when {
                intent?.action == Intent.ACTION_SEND -> {
                    if ("text/plain" == intent.type) {
                        handleSendText(intent, authToken) // Handle text being sent
                    }
                }
                else -> {
                    // Handle other intents, such as being started from the home screen
                }
            }
        }
    }

    private fun handleSendText(intent: Intent, authToken: String) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
            // Update UI to reflect text being shared

            Log.i("add to dok", "Send request to send deck to DoK $it")

            DoKService.addDeckToDoKAndMyDecks(authToken, this, it) { deckId, error ->
                if (deckId != null) {
                    Log.i("add to dok", "Added to DoK and called start on DoK URL $it")

                    val viewIntent = Intent(
                        "android.intent.action.VIEW",
                        Uri.parse("https://decksofkeyforge.com/decks/$deckId")
                    )
                    startActivity(viewIntent)
                } else {
                    this.runOnUiThread {
                        Toast.makeText(this, "Couldn't import and add the deck.", Toast.LENGTH_LONG).show()
                    }
                    Log.e("add to dok", "Couldn't add due to error: $error" ?: "No error?!")
                }
            }
        }
    }
}

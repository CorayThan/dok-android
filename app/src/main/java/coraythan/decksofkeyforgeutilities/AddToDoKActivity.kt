package coraythan.decksofkeyforgeutilities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coraythan.decksofkeyforgeutilities.ui.login.LoginActivity

class AddToDoKActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_to_dok)

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

            importDeck(authToken, it)
        }
    }

    private fun importDeck(authToken: String, deckUrl: String) {
        DoKService.addDeckToDoKAndMyDecks(authToken, this, deckUrl) { deckId, error ->
            if (deckId != null) {
                Log.i("add to dok", "Added to DoK and called start on DoK URL $deckUrl")

                val viewIntent = Intent(
                    "android.intent.action.VIEW",
                    Uri.parse("https://decksofkeyforge.com/decks/$deckId")
                )
                startActivity(viewIntent)
            } else {
                this.runOnUiThread {
                    val addingDeckText = findViewById<TextView>(R.id.adding_deck).apply {
                        setText(R.string.error_adding_deck)
                    }
                    val progressBar = findViewById<ProgressBar>(R.id.loading).apply {
                        visibility = View.GONE
                    }
                    findViewById<Button>(R.id.retry).apply {
                        visibility = View.VISIBLE

                        setOnClickListener {
                            importDeck(authToken, deckUrl)
                            visibility = View.GONE

                            progressBar.visibility = View.VISIBLE
                            addingDeckText.setText(R.string.adding_deck)
                        }
                    }
                }
                Log.e("add to dok", "Couldn't add due to error: $error" ?: "No error?!")
            }
        }
    }
}

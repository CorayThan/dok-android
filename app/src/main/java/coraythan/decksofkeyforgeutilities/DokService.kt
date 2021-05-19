package coraythan.decksofkeyforgeutilities

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.json.JSONObject

const val AUTH_TOKEN = "AUTH_TOKEN"

fun Activity.appPrefs() = this.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
fun SharedPreferences.findAuthToken() = this.getString(AUTH_TOKEN, null)

object DoKService {

    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().build()
    private val yourUserAdapter = moshi.adapter(YourUser::class.java)

    fun login(email: String, password: String, context: Activity, onLoggedIn: (username: String?, error: String?) -> Unit) {

        val loginJson = JSONObject()

        loginJson.put("email", email)
        loginJson.put("password", password)

        val loginBody = loginJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val url = "https://decksofkeyforge.com/api/users/login"

        val request = Request.Builder()
            .url(url)
            .post(loginBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                onLoggedIn(null, "Couldn't log in. ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        onLoggedIn(null, "Couldn't log in. ${response.message}")
                    } else {

                        val token = response.header("authorization") ?: throw RuntimeException("Auth header missing")
                        val sharedPref = context.appPrefs()
                        with(sharedPref.edit()) {
                            Log.i("put in shared prefs", "Putting token into shared prefs: $token")
                            putString(AUTH_TOKEN, token)
                            apply()
                        }
                        findUserInfo(token, context, onLoggedIn)
                    }

                }
            }
        })

    }

    fun findUserInfo(authToken: String, context: Activity, onLoggedIn: (username: String?, error: String?) -> Unit): Boolean {
        val url = "https://decksofkeyforge.com/api/users/secured/your-user"

        val request = Request.Builder()
            .url(url)
            .addHeader("authorization", authToken)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                onLoggedIn(null, "Couldn't get user info. ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        onLoggedIn(null, "Couldn't log in. ${response.message}")
                    } else {
                        val yourUser = yourUserAdapter.fromJson(response.body!!.source())

                        onLoggedIn(yourUser?.username, null)
                        Log.i("response", "Username: ${yourUser?.username}")
                    }
                }
            }
        })


        return true
    }

    fun addDeckToDoKAndMyDecks(authToken: String, context: Activity, deckUrl: String, onDeckFound: (deckId: String?, error: String?) -> Unit): Boolean {

        val uuidRegex = "[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}".toRegex(RegexOption.IGNORE_CASE)

        val uuid = uuidRegex.find(deckUrl)?.value ?: return false

        Log.i("add to DoK", "Deck UUID is $uuid")

        val url = "https://decksofkeyforge.com/api/decks/$uuid/import-and-add"

        val request = Request.Builder()
            .post(ByteArray(0).toRequestBody())
            .url(url)
            .addHeader("authorization", authToken)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                onDeckFound(null, "Couldn't add deck. ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        onDeckFound(null, "Couldn't add deck. ${response.message}")
                    } else {
                        onDeckFound(uuid, null)
                        Log.e("error adding deck", response.message)
                    }
                }
            }
        })


        return true
    }

    fun logout(context: Activity) {
        val sharedPref = context.appPrefs()
        with(sharedPref.edit()) {
            Log.i("remove auth", "Removing the auth token It currently is ${sharedPref.getString(AUTH_TOKEN, null)}.")
            remove(AUTH_TOKEN)
            apply()
        }
    }
}

@JsonClass(generateAdapter = true)
data class YourUser(
    val username: String
)

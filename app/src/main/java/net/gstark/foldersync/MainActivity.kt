package net.gstark.foldersync

import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import net.gstark.foldersync.ui.theme.FolderSyncTheme
import okhttp3.OkHttpClient
import at.bitfire.dav4jvm.BasicDigestAuthHandler
import at.bitfire.dav4jvm.DavCollection
import at.bitfire.dav4jvm.property.DisplayName
import at.bitfire.dav4jvm.property.GetLastModified
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FolderSyncTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                        Row {
                            Text("Greeting:")
                            Greeting(
                                name = "Android",
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        Row {
                            Text("URL:")
                            SimpleTextField()
                        }
                    }
                }
            }
        }

        val preferences = PreferenceManager.getDefaultSharedPreferences(this).all
        preferences.forEach {
            Log.d("Preferences", "${it.key} -> ${it.value}")
        }

        // DAV examples:
        /*lifecycleScope.launch(Dispatchers.IO) {
            val authHandler = BasicDigestAuthHandler(
                domain = null, // Optional, to only authenticate against hosts with this domain.
                username = "",
                password = ""
            )
            val okHttpClient = OkHttpClient.Builder()
                .followRedirects(false)
                .authenticator(authHandler)
                .addNetworkInterceptor(authHandler)
                .build()
            var location = "".toHttpUrl()
            var davCollection = DavCollection(okHttpClient, location)
            davCollection.put("World".toRequestBody(contentType = "text/plain".toMediaType())) { response ->
                Log.i("MainActivity", "put")
            }
            davCollection.get(accept = "", headers = null) { response ->
                response.body?.string()?.let { Log.i("MainActivity", it) }
            }

            location = "".toHttpUrl()
            davCollection = DavCollection(okHttpClient, location)
            davCollection.propfind(depth = 1, DisplayName.NAME, GetLastModified.NAME) { response, relation ->
                // This callback will be called for every file in the folder.
                // Use `response.properties` to access the successfully retrieved properties.
                Log.i("MainActivity", response.toString())
            }
        }*/
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun SimpleTextField() {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    TextField(
        value = text,
        onValueChange = { newText ->
            text = newText
        }
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FolderSyncTheme {
        Greeting("Android")
    }
}

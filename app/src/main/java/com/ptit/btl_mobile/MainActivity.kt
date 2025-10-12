package com.ptit.btl_mobile

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ptit.btl_mobile.model.database.Database
import com.ptit.btl_mobile.model.database.Song
import com.ptit.btl_mobile.ui.theme.BTL_MobileTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        Database(this.applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            testDatabase()
        }

        setContent {
            BTL_MobileTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    suspend fun testDatabase() {
        val c = Calendar.getInstance()
        val s = Song(0,
            "Hello",
            null ,
            1L,
            c.time,
            null,
            null,
            null)
        val db = Database.getInstance()
        db.SongDAO().insertAll(s)
        val songs = db.SongDAO().getAll();
        Log.d("MAIN", "Database initialized, data length " + songs.size)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BTL_MobileTheme {
        Greeting("Android")
    }
}
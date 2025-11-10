package me.idodanon.locationbasednotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import me.idodanon.locationbasednotes.auth.*
import me.idodanon.locationbasednotes.data.NoteFileRepository
import me.idodanon.locationbasednotes.main.*
import me.idodanon.locationbasednotes.navigation.AppNavHost
import me.idodanon.locationbasednotes.ui.theme.NotesAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NotesAppTheme {
                val context = LocalContext.current
                val authViewModel: AuthViewModel = viewModel()
                val authState by authViewModel.authState.collectAsState()

                val noteViewModel = viewModel<NoteViewModel>(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            val repo = NoteFileRepository(context)
                            @Suppress("UNCHECKED_CAST")
                            return NoteViewModel(repo) as T
                        }
                    }
                )

                val navController = rememberNavController()
                var startDestination: String

                val lastEmail = remember { authViewModel.getLastEmail(context) }
                lastEmail?.let {
                    noteViewModel.setUser(lastEmail)
                    startDestination = "main/$lastEmail"
                } ?: run { startDestination = "auth" }


                AppNavHost(
                    navController = navController,
                    authViewModel = authViewModel,
                    noteViewModel = noteViewModel,
                    authState = authState,
                    context = context,
                    startDestination = startDestination
                )
            }
        }
    }
}



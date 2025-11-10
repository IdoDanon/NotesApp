package me.idodanon.locationbasednotes.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import me.idodanon.locationbasednotes.auth.AuthScreen
import me.idodanon.locationbasednotes.auth.AuthState
import me.idodanon.locationbasednotes.auth.AuthViewModel
import me.idodanon.locationbasednotes.data.Note
import me.idodanon.locationbasednotes.main.EditNoteScreen
import me.idodanon.locationbasednotes.main.MainScreen
import me.idodanon.locationbasednotes.main.NoteViewModel
import me.idodanon.locationbasednotes.main.ViewNoteScreen
import me.idodanon.locationbasednotes.utils.LoadingScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    noteViewModel: NoteViewModel,
    authState: AuthState,
    context: Context,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(
            route = "auth"
        ) {
            AuthScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { email ->
                    authViewModel.saveLastUserEmail(context, email)
                    noteViewModel.setUser(email)
                    navController.navigate("main/$email") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "main/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val username = email.substringBefore("@")
            MainScreen(
                navController = navController,
                username = username,
                noteViewModel = noteViewModel,
                onLogout = {
                    authViewModel.logout()
                    authViewModel.forgetLastEmail(context)
                    navController.navigate("auth") {
                        popUpTo("main/$email") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "note/new/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            EditNoteScreen(
                userEmail = email,
                noteViewModel = noteViewModel,
                context = context,
                onSave = { navController.popBackStack() },
                onDelete = { navController.navigate("main/$email") },
                onBack = { navController.navigate("main/$email") }
            )
        }

        composable(
            route = "note/edit/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
            val note by produceState<Note?>(initialValue = null, noteId) {
                value = noteViewModel.getNoteById(noteId)
            }
            note?.let { existingNote ->
                val email = (authState as? AuthState.Authenticated)?.email ?: ""
                EditNoteScreen(
                    userEmail = email,
                    noteViewModel = noteViewModel,
                    existingNote = existingNote,
                    context = context,
                    onSave = { navController.popBackStack() },
                    onDelete = { navController.navigate("main/$email") },
                    onBack = { navController.navigate("note/view/$noteId") }
                )
            } ?: LoadingScreen()
        }

        composable(
            route = "note/view/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
            val note by produceState<Note?>(initialValue = null, noteId) {
                value = noteViewModel.getNoteById(noteId)
            }
            note?.let { n ->
                ViewNoteScreen(
                    note = n,
                    onEdit = { navController.navigate("note/edit/$noteId") },
                    onDelete = {
                        noteViewModel.deleteNote(n)
                        navController.navigate("main/${n.userEmail}")
                    },
                    onBack = { navController.navigate("main/${n.userEmail}") }
                )
            } ?: LoadingScreen()
        }
    }
}
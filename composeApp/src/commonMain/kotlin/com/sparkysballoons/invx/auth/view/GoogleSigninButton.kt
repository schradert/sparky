package com.sparkysballoons.invx.auth.view

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.andThen
import com.sparkysballoons.invx.auth.domain.GoogleAccount
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.launch

@Composable
fun GoogleSignInButton(
    modifier: Modifier = Modifier,
    onGoogleSignInResult: (GoogleAccount?) -> Unit,
) {
    val viewModel = koinViewModel<AuthViewModel>()
    val coroutineScope = rememberCoroutineScope()
    
    OutlinedButton(
        modifier = modifier,
        onClick = { 
            coroutineScope.launch {
                viewModel.signIn()
            }
        },
        content = { Text("Sign In with Google") },
    )
}
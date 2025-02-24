package com.azcode.busmanagmentsystem.presentation.auth.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azcode.busmanagmentsystem.data.remote.Result
import com.azcode.busmanagmentsystem.presentation.auth.viewmodel.AuthViewModel
import com.azcode.busmanagmentsystem.ui.theme.LatoFont
import com.azcode.busmanagmentsystem.ui.theme.NavigationGreen

// Screen containing TabRow (to switch between sign in and sign up)
@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val registerResult by authViewModel.registerState.collectAsStateWithLifecycle()

    LaunchedEffect(registerResult) {
        when (registerResult) {
            is Result.Success -> {
                selectedTabIndex = 0

            }
            else -> Unit // Do nothing for Idle state
        }
    }

    Box(
        modifier = Modifier
            .paint(
                painter = painterResource(id = com.azcode.busmanagmentsystem.R.drawable.im_login_backg), // Replace with your drawable
                contentScale = ContentScale.Crop
            )
            .background(Color.DarkGray.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Spacer(modifier = Modifier.fillMaxHeight(0.23f))
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(24.dp)),
                divider = {},
                indicator = { tabPositions ->
                    Box(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectedTabIndex]) // Animated position
                            .fillMaxWidth(0.5f) // Adjust width if needed
                            .fillMaxHeight() // Same height as tabs
                            .clip(RoundedCornerShape(24.dp))
                            .background(NavigationGreen) // Background behind text
                    )
                },
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Sign In", fontFamily = LatoFont) },
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .zIndex(1f),
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.Black,
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Sign Up", fontFamily = LatoFont) },
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .zIndex(1f),
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.Black
                )
            }
            Column(
                verticalArrangement = Arrangement.Center
            ) {


                Spacer(modifier = Modifier.height(16.dp))

                // Show the corresponding screen
                when (selectedTabIndex) {
                    0 -> SignInScreen(authViewModel)
                    1 -> SignUpScreen(authViewModel)
                }
            }
        }
    }
}

@Preview
@Composable
fun AuthPreview() {
    AuthScreen()
}



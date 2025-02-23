package com.azcode.busmanagmentsystem.presentation.auth.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.constraintlayout.compose.ConstraintLayout
import com.azcode.busmanagmentsystem.data.remote.BsbApiService
import com.azcode.busmanagmentsystem.data.remote.UserAuthRequest
import com.azcode.busmanagmentsystem.data.remote.UserAuthResponse
import com.azcode.busmanagmentsystem.data.remote.UserRegistrationRequest
import com.azcode.busmanagmentsystem.data.remote.UserRegistrationResponse
import com.azcode.busmanagmentsystem.domain.repository.AuthRepository
import com.azcode.busmanagmentsystem.presentation.auth.viewmodel.AuthViewModel
import com.azcode.busmanagmentsystem.ui.theme.*
import retrofit2.Response


@Composable
fun SignInScreen(
    authViewModel : AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    BusTrackingTheme {
        ConstraintLayout(
            modifier = Modifier
                .wrapContentSize(),
        ) {
            val card = createRef()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .constrainAs(card){
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                        start.linkTo(parent.start)
                    },
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.75f)) // More transparent for a glassy look
                            .graphicsLayer {
                                renderEffect = BlurEffect(radiusX = 35f, radiusY = 35f, edgeTreatment = TileMode.Decal)
                            },
                    )


                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        OutlinedTextField(
                            value = email, // viewModel.email
                            onValueChange = {
                                email = it
                                // viewModel.onEmailChange(it)
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors =
                            OutlinedTextFieldDefaults.colors(
                                cursorColor = Color.White,
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                focusedBorderColor = NavigationGreen,
                            ),
                            label = { Text("Email", color = Color.White, fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Email
                            ),
                            modifier = Modifier.fillMaxWidth()


                        )

                        Spacer(modifier = Modifier.height(8.dp))


                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                cursorColor = Color.White,
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                focusedBorderColor = NavigationGreen,
                            ),
                            label = { Text("Password", color = Color.White, fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Password
                            ),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                        tint = Color.White
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )


                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                // viewModel.login()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonColors(
                                containerColor = NavigationGreen,
                                contentColor = Color.White,
                                disabledContentColor = Color.White,
                                disabledContainerColor = Color.Gray
                            )
                        ) {
                            Text("Login")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            modifier = Modifier.align(Alignment.CenterHorizontally).clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null, // Disables ripple effect
                                onClick = {}
                            ),
                            colors = ButtonDefaults.textButtonColors(),
                            onClick = {
                                // navController.navigate("register")
                            }
                        ) {
                            Text("Forgot Password ?", color = Color.White)
                        }


                    }
                }
            }

//            loginState?.let { isSuccess ->
//                if (isSuccess) {
//                    navController.navigate("home") {
//                        popUpTo("login") { inclusive = true } // Clears back stack
//                    }
//                }
//            }


        }


    }
}


@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    class fakeBsbApi : BsbApiService {
        override suspend fun registerUser(userRegistrationRequest: UserRegistrationRequest): Response<UserRegistrationResponse> {
            TODO("Not yet implemented")
        }

        override suspend fun loginUser(userAuthRequest: UserAuthRequest): Response<UserAuthResponse> {
            TODO("Not yet implemented")
        }

    }
    class fakeAuthRepo(fakeBsbApi: fakeBsbApi) : AuthRepository(fakeBsbApi)
    class fakeAuthViewModel(fakeAuthRepo: fakeAuthRepo) : AuthViewModel(fakeAuthRepo)
    SignInScreen(fakeAuthViewModel(fakeAuthRepo((fakeBsbApi()))))
}





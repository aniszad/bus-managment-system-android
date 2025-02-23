package com.azcode.busmanagmentsystem.presentation.auth.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.azcode.busmanagmentsystem.data.remote.BsbApiService
import com.azcode.busmanagmentsystem.data.remote.Result
import com.azcode.busmanagmentsystem.data.remote.Role
import com.azcode.busmanagmentsystem.data.remote.UserAuthRequest
import com.azcode.busmanagmentsystem.data.remote.UserAuthResponse
import com.azcode.busmanagmentsystem.data.remote.UserRegistrationRequest
import com.azcode.busmanagmentsystem.data.remote.UserRegistrationResponse
import com.azcode.busmanagmentsystem.domain.repository.AuthRepository
import com.azcode.busmanagmentsystem.presentation.auth.viewmodel.AuthViewModel
import com.azcode.busmanagmentsystem.ui.theme.BusTrackingTheme
import com.azcode.busmanagmentsystem.ui.theme.NavigationGreen
import retrofit2.Response

@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var role by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        authViewModel.registerState.collect { registerResult ->
            when (registerResult) {
                is Result.Success -> {
                    Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()
                }

                is Result.Error -> {
                    Toast.makeText(
                        context,
                        "Registration Failed: ${(registerResult as Result.Error).message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                is Result.Loading -> {
                    Toast.makeText(context, "Registering...", Toast.LENGTH_SHORT).show()
                }

                else -> Unit // Do nothing for Idle state
            }
        }

    }

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
                    .constrainAs(card) {
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
                                renderEffect = BlurEffect(
                                    radiusX = 35f,
                                    radiusY = 35f,
                                    edgeTreatment = TileMode.Decal
                                )
                            },
                    )


                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        OutlinedTextField(
                            value = firstName, // viewModel.email
                            onValueChange = {
                                firstName = it
                                authViewModel.onFirstNameChange(it)
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
                            label = { Text("Name", color = Color.White, fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Email
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = lastName, // viewModel.email
                            onValueChange = {
                                lastName = it
                                authViewModel.onLastNameChange(it)
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
                            label = { Text("Last Name", color = Color.White, fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Email
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = email, // viewModel.email
                            onValueChange = {
                                email = it
                                authViewModel.onEmailChange(it)
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
                            modifier = Modifier
                                .fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = phoneNumber, // viewModel.email
                            onValueChange = {
                                phoneNumber = it
                                authViewModel.onPhoneNumberChange(it)
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
                            label = { Text("Phone number", color = Color.White, fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Email
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                authViewModel.onPasswordChange(it)
                            },
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
                                authViewModel.registerUser(
                                    firstName = firstName,
                                    lastName = lastName,
                                    email = email,
                                    phoneNumber = phoneNumber,
                                    password = password,
                                    role = Role.USER,
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonColors(
                                containerColor = NavigationGreen,
                                contentColor = Color.White,
                                disabledContentColor = Color.White,
                                disabledContainerColor = Color.Gray
                            )
                        ) {
                            Text("Sign up")
                        }

                        Spacer(modifier = Modifier.height(8.dp))
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
fun SignupScreenPreview() {
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

    SignUpScreen(fakeAuthViewModel(fakeAuthRepo((fakeBsbApi()))))
}
package com.azcode.busmanagmentsystem.presentation.auth.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.navigation.NavHostController
import com.azcode.busmanagmentsystem.data.local.SecuredPreferencesManager
import com.azcode.busmanagmentsystem.data.local.SessionManager
import com.azcode.busmanagmentsystem.data.remote.BsbApiService
import com.azcode.busmanagmentsystem.data.remote.RefreshTokenRequest
import com.azcode.busmanagmentsystem.data.remote.RefreshTokenResponse
import com.azcode.busmanagmentsystem.data.remote.Result
import com.azcode.busmanagmentsystem.data.remote.UserAuthRequest
import com.azcode.busmanagmentsystem.data.remote.UserAuthResponse
import com.azcode.busmanagmentsystem.data.remote.UserRegistrationRequest
import com.azcode.busmanagmentsystem.data.remote.UserRegistrationResponse
import com.azcode.busmanagmentsystem.domain.repository.AuthRepository
import com.azcode.busmanagmentsystem.presentation.auth.state.SignInFormState
import com.azcode.busmanagmentsystem.presentation.auth.state.toUserAuthRequest
import com.azcode.busmanagmentsystem.presentation.auth.viewmodel.AuthViewModel
import com.azcode.busmanagmentsystem.presentation.components.LoadingDialog
import com.azcode.busmanagmentsystem.ui.theme.BusTrackingTheme
import com.azcode.busmanagmentsystem.ui.theme.NavigationGreen
import retrofit2.Response


@Composable
fun SignInScreen(
    authViewModel: AuthViewModel,
) {

    val context = LocalContext.current
    var signInFormState by remember { mutableStateOf(SignInFormState()) }
    val signInState by authViewModel.loginState.collectAsState()
    val isLoading by authViewModel.isSigninLoading.collectAsState()

    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            !password.any { it.isDigit() } -> "Password must contain at least one number"
            !password.any { it.isUpperCase() } -> "Password must contain at least one uppercase letter"
            else -> null
        }
    }
    fun validateEmail(email: String): String? {
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
        return when {
            email.isBlank() -> "Email is required"
            !email.matches(emailRegex) -> "Please enter a valid email address"
            else -> null
        }
    }
    fun validateSignInForm(email:String, password:String): Boolean {
        val newErrors = mutableMapOf<String, String>()

        validateEmail(signInFormState.credentials)?.let { newErrors["email"] = it }
        validatePassword(signInFormState.password)?.let { newErrors["password"] = it }
        signInFormState.errors = newErrors
        return newErrors.isEmpty()
    }
    LaunchedEffect(signInState) {
        when (signInState) {
            is Result.Success -> {

            }

            is Result.Error -> {
                Toast.makeText(context, (signInState as Result.Error).message, Toast.LENGTH_SHORT)
                    .show()
                authViewModel.updateSignInLoading(false)
            }

            is Result.Loading -> {
                authViewModel.updateSignInLoading(true)
            }

            else -> null
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

                    LoadingDialog(isLoading) {}
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        OutlinedTextField(
                            value = signInFormState.credentials, // viewModel.email
                            onValueChange = {
                                signInFormState = signInFormState.copy(credentials = it)
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
                            value = signInFormState.password,
                            onValueChange = {
                                signInFormState = signInFormState.copy(password = it)
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
                            visualTransformation = if (signInFormState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = {
                                    signInFormState.passwordVisible =
                                        !signInFormState.passwordVisible
                                }) {
                                    Icon(
                                        imageVector = if (signInFormState.passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (signInFormState.passwordVisible) "Hide password" else "Show password",
                                        tint = Color.White
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )


                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                authViewModel.signIn(
                                    userAuthRequest = signInFormState.toUserAuthRequest()
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
                            Text("Login")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .clickable(
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

@Composable
fun SignInScreenUi(){

}


@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    val context = LocalContext.current
    class FakeSecuredPref() : SecuredPreferencesManager(context){

    }
    class FakeSessionManager() : SessionManager(FakeSecuredPref()){

    }
    class FakeBsbApi : BsbApiService {
        override suspend fun signUp(userRegistrationRequest: UserRegistrationRequest): Response<UserRegistrationResponse> {
            TODO("Not yet implemented")
        }

        override suspend fun signIn(userAuthRequest: UserAuthRequest): Response<UserAuthResponse> {
            TODO("Not yet implemented")
        }

        override suspend fun refreshToken(refreshTokenRequest: RefreshTokenRequest): Response<RefreshTokenResponse> {
            TODO("Not yet implemented")
        }

    }

    class FakeAuthRepo(fakeBsbApi: FakeBsbApi) : AuthRepository(FakeBsbApi(), FakeSecuredPref())
    class FakeAuthViewModel(fakeAuthRepo: FakeAuthRepo) : AuthViewModel(FakeSessionManager(), FakeAuthRepo(FakeBsbApi()))

    //SignInScreen(FakeAuthViewModel(FakeAuthRepo((FakeBsbApi()))))
}





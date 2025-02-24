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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azcode.busmanagmentsystem.data.remote.BsbApiService
import com.azcode.busmanagmentsystem.data.remote.Result
import com.azcode.busmanagmentsystem.data.remote.UserAuthRequest
import com.azcode.busmanagmentsystem.data.remote.UserAuthResponse
import com.azcode.busmanagmentsystem.data.remote.UserRegistrationRequest
import com.azcode.busmanagmentsystem.data.remote.UserRegistrationResponse
import com.azcode.busmanagmentsystem.domain.repository.AuthRepository
import com.azcode.busmanagmentsystem.presentation.auth.state.RegistrationFormState
import com.azcode.busmanagmentsystem.presentation.auth.state.toUserRegistrationRequest
import com.azcode.busmanagmentsystem.presentation.auth.viewmodel.AuthViewModel
import com.azcode.busmanagmentsystem.presentation.components.LoadingDialog
import com.azcode.busmanagmentsystem.ui.theme.BusTrackingTheme
import com.azcode.busmanagmentsystem.ui.theme.NavigationGreen
import retrofit2.Response

@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel
) {
    val isLoading by authViewModel.isSignupLoading.collectAsState()
    val context = LocalContext.current
    val registerResult by authViewModel.registerState.collectAsStateWithLifecycle()

    var registerForm by remember { mutableStateOf(RegistrationFormState()) }
    var errors by remember { mutableStateOf(mapOf<String, String>()) }

    // Validation functions
    fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Name is required"
            name.length < 2 -> "Name must be at least 2 characters"
            !name.all { it.isLetter() || it.isWhitespace() } -> "Name should only contain letters"
            else -> null
        }
    }

    fun validateLastName(lastName: String): String? {
        return when {
            lastName.isBlank() -> "Last name is required"
            lastName.length < 2 -> "Last name must be at least 2 characters"
            !lastName.all { it.isLetter() || it.isWhitespace() } -> "Last name should only contain letters"
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

    fun validatePhoneNumber(phoneNumber: String): String? {
        val phoneRegex = "^[+]?[0-9]{10,13}\$".toRegex()
        return when {
            phoneNumber.isBlank() -> "Phone number is required"
            !phoneNumber.matches(phoneRegex) -> "Please enter a valid phone number"
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            !password.any { it.isDigit() } -> "Password must contain at least one number"
            !password.any { it.isUpperCase() } -> "Password must contain at least one uppercase letter"
            else -> null
        }
    }

    fun validateForm(): Boolean {
        val newErrors = mutableMapOf<String, String>()

        validateName(registerForm.firstName)?.let { newErrors["firstName"] = it }
        validateLastName(registerForm.lastName)?.let { newErrors["lastName"] = it }
        validateEmail(registerForm.email)?.let { newErrors["email"] = it }
        validatePhoneNumber(registerForm.phoneNumber ?: "")?.let { newErrors["phoneNumber"] = it }
        validatePassword(registerForm.password)?.let { newErrors["password"] = it }

        errors = newErrors
        return newErrors.isEmpty()
    }

    fun clearRegistrationForm() {
        registerForm = RegistrationFormState()
        errors = mapOf()
    }

    when (registerResult) {
        is Result.Success -> {
            authViewModel.updateIsSignUpLoading(false)
            clearRegistrationForm()
            Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()
        }

        is Result.Error -> {
            clearRegistrationForm()
            authViewModel.updateIsSignUpLoading(false)
            Toast.makeText(
                context,
                "Registration Failed: ${(registerResult as Result.Error).message}",
                Toast.LENGTH_LONG
            ).show()
        }

        is Result.Loading -> {
            authViewModel.updateIsSignUpLoading(true)
        }

        else -> Unit // Do nothing for Idle state
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
                            .background(Color.Black.copy(alpha = 0.75f))
                            .graphicsLayer {
                                renderEffect = BlurEffect(
                                    radiusX = 35f,
                                    radiusY = 35f,
                                    edgeTreatment = TileMode.Decal
                                )
                            },
                    )
                    LoadingDialog(isLoading) {
                        Toast.makeText(context, "You account has been created", Toast.LENGTH_SHORT)
                            .show()
                    }

                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        OutlinedTextField(
                            value = registerForm.firstName,
                            onValueChange = {
                                registerForm = registerForm.copy(firstName = it)
                                // Clear error when user types
                                if (errors.containsKey("firstName")) {
                                    errors = errors.toMutableMap().apply { remove("firstName") }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                cursorColor = Color.White,
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                focusedBorderColor = NavigationGreen,
                                errorBorderColor = Color.Red,
                                errorCursorColor = Color.Red,
                                errorTextColor = Color.White
                            ),
                            label = { Text("Name", color = Color.White, fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Text
                            ),
                            isError = errors.containsKey("firstName"),
                            supportingText = {
                                errors["firstName"]?.let {
                                    Text(it, color = Color.Red, fontSize = 12.sp)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = registerForm.lastName,
                            onValueChange = {
                                registerForm = registerForm.copy(lastName = it)
                                if (errors.containsKey("lastName")) {
                                    errors = errors.toMutableMap().apply { remove("lastName") }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                cursorColor = Color.White,
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                focusedBorderColor = NavigationGreen,
                                errorBorderColor = Color.Red,
                                errorCursorColor = Color.Red,
                                errorTextColor = Color.White
                            ),
                            label = { Text("Last Name", color = Color.White, fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Text
                            ),
                            isError = errors.containsKey("lastName"),
                            supportingText = {
                                errors["lastName"]?.let {
                                    Text(it, color = Color.Red, fontSize = 12.sp)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = registerForm.email,
                            onValueChange = {
                                registerForm = registerForm.copy(email = it)
                                if (errors.containsKey("email")) {
                                    errors = errors.toMutableMap().apply { remove("email") }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                cursorColor = Color.White,
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                focusedBorderColor = NavigationGreen,
                                errorBorderColor = Color.Red,
                                errorCursorColor = Color.Red,
                                errorTextColor = Color.White
                            ),
                            label = { Text("Email", color = Color.White, fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Email
                            ),
                            isError = errors.containsKey("email"),
                            supportingText = {
                                errors["email"]?.let {
                                    Text(it, color = Color.Red, fontSize = 12.sp)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = registerForm.phoneNumber ?: "",
                            onValueChange = {
                                registerForm = registerForm.copy(phoneNumber = it)
                                if (errors.containsKey("phoneNumber")) {
                                    errors = errors.toMutableMap().apply { remove("phoneNumber") }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                cursorColor = Color.White,
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                focusedBorderColor = NavigationGreen,
                                errorBorderColor = Color.Red,
                                errorCursorColor = Color.Red,
                                errorTextColor = Color.White,
                            ),
                            label = { Text("Phone number", color = Color.White, fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Phone
                            ),
                            isError = errors.containsKey("phoneNumber"),
                            supportingText = {
                                errors["phoneNumber"]?.let {
                                    Text(it, color = Color.Red, fontSize = 12.sp)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = registerForm.password,
                            onValueChange = {
                                registerForm = registerForm.copy(password = it)
                                if (errors.containsKey("password")) {
                                    errors = errors.toMutableMap().apply { remove("password") }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                cursorColor = Color.White,
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                focusedBorderColor = NavigationGreen,
                                errorBorderColor = Color.Red,
                                errorCursorColor = Color.Red,
                                errorTextColor = Color.White
                            ),
                            label = { Text("Password", color = Color.White, fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Password
                            ),
                            visualTransformation = if (registerForm.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = {
                                    registerForm = registerForm.copy(
                                        passwordVisible = !registerForm.passwordVisible
                                    )
                                }) {
                                    Icon(
                                        imageVector = if (registerForm.passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (registerForm.passwordVisible) "Hide password" else "Show password",
                                        tint = Color.White
                                    )
                                }
                            },
                            isError = errors.containsKey("password"),
                            supportingText = {
                                errors["password"]?.let {
                                    Text(it, color = Color.Red, fontSize = 12.sp)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (validateForm()) {
                                    authViewModel.registerUser(registerForm.toUserRegistrationRequest())
                                }
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
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SignupScreenPreview() {
    class FakeBsbApi : BsbApiService {
        override suspend fun registerUser(userRegistrationRequest: UserRegistrationRequest): Response<UserRegistrationResponse> {
            TODO("Not yet implemented")
        }

        override suspend fun loginUser(userAuthRequest: UserAuthRequest): Response<UserAuthResponse> {
            TODO("Not yet implemented")
        }

    }

    class FakeAuthRepo(fakeBsbApi: FakeBsbApi) : AuthRepository(fakeBsbApi)
    class FakeAuthViewModel(fakeAuthRepo: FakeAuthRepo) : AuthViewModel(fakeAuthRepo)

    SignUpScreen(FakeAuthViewModel(FakeAuthRepo((FakeBsbApi()))))
}
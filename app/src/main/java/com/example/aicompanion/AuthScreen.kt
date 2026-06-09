package com.example.aicompanion

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {

    var isLogin by remember {
        mutableStateOf(true)
    }

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    var message by remember {
        mutableStateOf("")
    }

    val auth = FirebaseAuth.getInstance()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF000000),
                        Color(0xFF120018)
                    )
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "🤖 AICompanion",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isLogin)
                    "Welcome Back"
                else
                    "Create Account",

                color = Color.LightGray,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // EMAIL FIELD

            OutlinedTextField(

                value = email,

                onValueChange = {
                    email = it
                },
                textStyle = LocalTextStyle.current.copy(
                    color = Color.White,
                    fontSize = 18.sp
                ),
                label = {
                    Text(
                        "Email",
                        color = Color.White
                    )
                },

                placeholder = {
                    Text(
                        "Enter your email",
                        color = Color.LightGray
                    )
                },

                singleLine = true,

                isError =
                    email.isNotEmpty() &&
                            !Patterns.EMAIL_ADDRESS
                                .matcher(email)
                                .matches(),

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(20.dp),

                colors = OutlinedTextFieldDefaults.colors(

                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,

                    focusedBorderColor = Color(0xFF7C4DFF),
                    unfocusedBorderColor = Color.Gray,

                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.LightGray,

                    cursorColor = Color.White
                )
            )

            if (
                email.isNotEmpty() &&
                !Patterns.EMAIL_ADDRESS
                    .matcher(email)
                    .matches()
            ) {

                Text(
                    text = "Enter valid email",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PASSWORD FIELD

            OutlinedTextField(

                value = password,

                onValueChange = {
                    password = it
                },
                textStyle = LocalTextStyle.current.copy(
                    color = Color.White,
                    fontSize = 18.sp
                ),
                label = {
                    Text(
                        "Password",
                        color = Color.White
                    )
                },

                placeholder = {
                    Text(
                        "Minimum 6 characters",
                        color = Color.LightGray
                    )
                },

                singleLine = true,

                visualTransformation =
                    if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),

                trailingIcon = {

                    TextButton(
                        onClick = {
                            passwordVisible =
                                !passwordVisible
                        }
                    ) {

                        Text(
                            if (passwordVisible)
                                "Hide"
                            else
                                "Show",

                            color = Color(0xFF7C4DFF)
                        )
                    }
                },

                isError =
                    password.isNotEmpty() &&
                            password.length < 6,

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(20.dp),

                colors = OutlinedTextFieldDefaults.colors(

                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,

                    focusedBorderColor = Color(0xFF7C4DFF),
                    unfocusedBorderColor = Color.Gray,

                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.LightGray,

                    cursorColor = Color.White
                )
            )

            if (
                password.isNotEmpty() &&
                password.length < 6
            ) {

                Text(
                    text = "Password must be at least 6 characters",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(

                onClick = {

                    if (
                        !Patterns.EMAIL_ADDRESS
                            .matcher(email)
                            .matches()
                    ) {

                        message = "Invalid Email"
                        return@Button
                    }

                    if (password.length < 6) {

                        message =
                            "Password must be at least 6 characters"

                        return@Button
                    }

                    if (isLogin) {

                        auth.signInWithEmailAndPassword(
                            email,
                            password
                        ).addOnCompleteListener {

                            if (it.isSuccessful) {

                                onLoginSuccess()

                            } else {

                                message =
                                    it.exception?.message.toString()
                            }
                        }

                    } else {

                        auth.createUserWithEmailAndPassword(
                            email,
                            password
                        ).addOnCompleteListener {

                            if (it.isSuccessful) {

                                onLoginSuccess()

                            } else {

                                message =
                                    it.exception?.message.toString()
                            }
                        }
                    }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),

                shape = RoundedCornerShape(20.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7B3FF2)
                )
            ) {

                Text(
                    text =
                        if (isLogin)
                            "Login"
                        else
                            "Sign Up",

                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(

                onClick = {

                    isLogin = !isLogin
                    message = ""
                },

                modifier = Modifier.align(
                    Alignment.CenterHorizontally
                )
            ) {

                Text(

                    text =
                        if (isLogin)
                            "Create new account"
                        else
                            "Already have account?",

                    color = Color(0xFF9D7CFF)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = message,
                color = Color.Red
            )
        }
    }
}
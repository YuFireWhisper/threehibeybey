package com.threehibeybey

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RegisterActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            RegisterPage(onRegisterClick = { email, password, confirmPassword ->
                if (password != confirmPassword) {
                    Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                } else if (!isPasswordStrong(password)) {
                    Toast.makeText(this, "Password is too weak.", Toast.LENGTH_SHORT).show()
                } else {
                    createUser(email, password)
                }
            })
        }
    }

   override fun onStart() {
        super.onStart()
        auth.currentUser?.let { reload() }
    }

    private fun reload() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun createUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("RegisterActivity", "createUserWithEmail:success")
                    auth.currentUser?.let {
                        updateUI(it)
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                } else {
                    Log.w("RegisterActivity", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        // Update UI based on user object
    }

    private fun isPasswordStrong(password: String): Boolean {
        // Check password strength criteria
        return password.length >= 8 && password.any { it.isDigit() } && password.any { it.isUpperCase() }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterPage(onRegisterClick: (String, String, String) -> Unit = { _, _, _ -> }) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(8.dp)
        )

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(8.dp)
        )

        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(8.dp)
        )

        Button(onClick = { onRegisterClick(email, password, confirmPassword) }) {
            Text("Register")
        }
    }
}

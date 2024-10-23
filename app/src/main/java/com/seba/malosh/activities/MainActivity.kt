package com.seba.malosh.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.seba.malosh.R

class MainActivity : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var titleText: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar FirebaseAuth
        auth = FirebaseAuth.getInstance()

        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        titleText = findViewById(R.id.titleText)

        loginButton.setOnClickListener {
            val inputUsername = username.text.toString().trim()
            val inputPassword = password.text.toString().trim()

            if (inputUsername.isNotEmpty() && inputPassword.isNotEmpty()) {
                // Iniciar sesión con Firebase Authentication
                auth.signInWithEmailAndPassword(inputUsername, inputPassword)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Inicio de sesión exitoso, redirigir a BienvenidaActivity
                            Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, BienvenidaActivity::class.java)
                            startActivity(intent)
                            finish() // Finalizar MainActivity para que no quede en el historial
                        } else {
                            // Error en el inicio de sesión
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                // Validación de campos vacíos
                Toast.makeText(this, "Por favor, ingresa el correo y la contraseña", Toast.LENGTH_SHORT).show()
            }
        }

        registerButton.setOnClickListener {
            // Redirigir a la pantalla de registro
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}

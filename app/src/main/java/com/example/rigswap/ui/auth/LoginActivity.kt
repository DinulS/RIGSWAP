package com.example.rigswap.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rigswap.MainActivity
import com.example.rigswap.R
import com.example.rigswap.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        com.example.rigswap.util.InsetUtils.applyTopSystemBarInsets(binding.root)

        // Password visibility toggle with visual cyan highlight
        binding.btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            val currentTypeface = binding.etPassword.typeface
            if (isPasswordVisible) {
                binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.btnTogglePassword.setColorFilter(androidx.core.content.ContextCompat.getColor(this, R.color.accent_cyan))
            } else {
                binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.btnTogglePassword.setColorFilter(androidx.core.content.ContextCompat.getColor(this, R.color.text_secondary))
            }
            binding.etPassword.typeface = currentTypeface
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }

        // Primary Sign In Action
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Launch Main App
            Toast.makeText(this, "Welcome to RIGSWAP", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Google Sign In Mock
        binding.btnGoogleSignIn.setOnClickListener {
            Toast.makeText(this, "Connecting with Google...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Forgot Password
        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Password reset link sent to registered email.", Toast.LENGTH_LONG).show()
        }

        // Create Account Link
        binding.btnCreateAccount.setOnClickListener {
            Toast.makeText(this, "Welcome to the forge! Account created.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

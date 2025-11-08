package com.android.app.notesapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.app.notesapp.databinding.ActivityLoginAccountBinding

class LoginAccount : AppCompatActivity() {
    private lateinit var binding: ActivityLoginAccountBinding
    private var isInfoValid: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.myEmail.hint = "Email Address"
        binding.myPassword.hint = "Password"

        binding.myEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.textInputLayout5.error = null
            } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.myEmail.text.toString()).matches()) {
                isInfoValid = false
                binding.textInputLayout5.error = "Invalid Email"
            } else {
                isInfoValid = true
            }
        }

        binding.myEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.myEmail.hint = if (binding.myEmail.text.isNullOrEmpty()) "Email Address" else null
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.myPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.myPassword.hint = if (binding.myPassword.text.isNullOrEmpty()) "Password" else null
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.loginAccBtn.setOnClickListener {
            binding.myEmail.clearFocus()
            loginAcc()
        }

        binding.createAccActivityBtn.setOnClickListener { switchToCreateAcc() }
    }

    private fun switchToCreateAcc() {
        startActivity(Intent(this, CreateAccount::class.java))
        finish()
    }

    private fun loginAcc() {
        if (!isInfoValid) {
            Toast.makeText(this, "Invalid Email Entered", Toast.LENGTH_SHORT).show()
            return
        }

        setProgressBar(true)
        FirebaseRefs.auth
            .signInWithEmailAndPassword(
                binding.myEmail.text.toString(),
                binding.myPassword.text.toString()
            )
            .addOnCompleteListener(this) { task ->
                setProgressBar(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    binding.myPassword.text?.clear()
                    Toast.makeText(this, "Incorrect Credentials ", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun setProgressBar(progress: Boolean) {
        binding.progressBar.visibility = if (progress) View.VISIBLE else View.INVISIBLE
    }
}
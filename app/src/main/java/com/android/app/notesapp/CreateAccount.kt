package com.android.app.notesapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.android.app.notesapp.databinding.ActivityCreateAccountBinding

class CreateAccount : AppCompatActivity() {
    private lateinit var binding: ActivityCreateAccountBinding
    private var isInfoValid: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.regEmail.hint = "Email Address"
        binding.regPassword.hint = "Password"
        binding.regRetypePassword.hint = "Confirm Password"

        binding.regEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.regEmail.hint = if (binding.regEmail.text.isNullOrEmpty()) "Email Address" else null
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.regEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.textInputLayout3.error = null
            } else {
                val email = binding.regEmail.text.toString().trim()
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    isInfoValid = false
                    binding.textInputLayout3.error = "Invalid Email"
                } else {
                    isInfoValid = true
                }
            }
        }

        binding.regPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.regPassword.hint = if (binding.regPassword.text.isNullOrEmpty()) "Password" else null
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.regPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.textInputLayout.error = null
            } else {
                if (binding.regPassword.text.toString().length < 6) {
                    isInfoValid = false
                    binding.textInputLayout.error = "Password must be at least 6 characters"
                } else {
                    isInfoValid = true
                }
            }
        }

        binding.regRetypePassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.regRetypePassword.hint =
                    if (binding.regRetypePassword.text.isNullOrEmpty()) "Confirm Password" else null
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.regRetypePassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.textInputLayout2.error = null
            } else {
                val pass = binding.regPassword.text.toString()
                val confirm = binding.regRetypePassword.text.toString()
                if (confirm.length < 6 || pass != confirm) {
                    isInfoValid = false
                    binding.textInputLayout2.error = "Passwords don't match"
                } else {
                    isInfoValid = true
                }
            }
        }

        binding.regAccBtn.setOnClickListener {
            binding.regEmail.clearFocus()
            binding.regPassword.clearFocus()
            binding.regRetypePassword.clearFocus()
            createAcc()
        }

        binding.loginAccActivityBtn.setOnClickListener { switchToLoginAcc() }
    }

    private fun switchToLoginAcc() {
        startActivity(Intent(this, LoginAccount::class.java))
        finish()
    }

    private fun setProgressBar(progress: Boolean) {
        binding.progressBar.visibility = if (progress) View.VISIBLE else View.INVISIBLE
    }

    private fun createAcc() {
        val email = binding.regEmail.text.toString().trim()
        val pass = binding.regPassword.text.toString()
        val confirm = binding.regRetypePassword.text.toString()

        // Final in-place validation (authoritative; independent of focus listeners)
        var ok = true
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textInputLayout3.error = "Invalid Email"; ok = false
        } else binding.textInputLayout3.error = null

        if (pass.length < 6) {
            binding.textInputLayout.error = "Password must be at least 6 characters"; ok = false
        } else binding.textInputLayout.error = null

        if (pass != confirm) {
            binding.textInputLayout2.error = "Passwords don't match"; ok = false
        } else binding.textInputLayout2.error = null

        if (!ok) {
            Toast.makeText(applicationContext, "Invalid Data Entered", Toast.LENGTH_SHORT).show()
            return
        }

        setProgressBar(true)
        FirebaseRefs.auth
            .createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                setProgressBar(false)
                if (task.isSuccessful) {
                    FirebaseRefs.auth.currentUser?.sendEmailVerification()
                    Toast.makeText(
                        applicationContext,
                        "Account created. Verify your email before logging in.",
                        Toast.LENGTH_SHORT
                    ).show()
                    FirebaseRefs.auth.signOut()
                    startActivity(Intent(this, LoginAccount::class.java))
                    finish()
                } else {
                    val msg = task.exception?.localizedMessage ?: "Account Creation Failed. Try Again"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            }
    }
}
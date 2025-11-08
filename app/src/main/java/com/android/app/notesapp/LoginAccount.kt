package com.android.app.notesapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.app.notesapp.databinding.ActivityLoginAccountBinding

class LoginAccount : AppCompatActivity() {
    private lateinit var binding: ActivityLoginAccountBinding
    private var loginBtnLabel: CharSequence? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginBtnLabel = binding.loginAccBtn.text

        // Validate email on focus loss (use TRIMMED text)
        binding.myEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.textInputLayout5.error = null
            } else {
                val email = binding.myEmail.text?.toString()?.trim().orEmpty()
                if (email.isNotEmpty()) binding.myEmail.setText(email) // normalize UI once
                binding.textInputLayout5.error =
                    if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches())
                        "Invalid Email" else null
            }
        }

        // "Go" on password: hide keyboard and submit
        binding.myPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                hideKeyboard()
                binding.loginAccBtn.performClick()
                true
            } else false
        }

        binding.loginAccBtn.setOnClickListener {
            hideKeyboard()
            loginAcc()
        }

        binding.createAccActivityBtn.setOnClickListener { switchToCreateAcc() }
    }

    private fun switchToCreateAcc() {
        startActivity(Intent(this, CreateAccount::class.java))
        finish()
    }

    private fun loginAcc() {
        val email = binding.myEmail.text?.toString()?.trim().orEmpty()
        val password = binding.myPassword.text?.toString().orEmpty()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textInputLayout5.error = "Invalid Email"
            Toast.makeText(this, "Invalid Email Entered", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        FirebaseRefs.auth
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    binding.myPassword.text?.clear()
                    Toast.makeText(this, "Incorrect Credentials", Toast.LENGTH_SHORT).show()
                }
            }
    }

    /** Disable button and show in-button spinner */
    private fun setLoading(loading: Boolean) {
        binding.loginAccBtn.isEnabled = !loading
        binding.btnProgress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.loginAccBtn.text = if (loading) "" else loginBtnLabel
    }

    private fun hideKeyboard() {
        currentFocus?.windowToken?.let {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it, 0)
        }
    }
}
package com.android.app.notesapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.app.notesapp.databinding.ActivityCreateAccountBinding

class CreateAccount : AppCompatActivity() {
    private lateinit var binding: ActivityCreateAccountBinding
    private var createBtnLabel: CharSequence? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createBtnLabel = binding.regAccBtn.text

        // Email: validate on focus loss (trim + normalize UI)
        binding.regEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.textInputLayout3.error = null
            } else {
                val email = binding.regEmail.text?.toString()?.trim().orEmpty()
                if (email.isNotEmpty()) binding.regEmail.setText(email)
                binding.textInputLayout3.error =
                    if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches())
                        "Invalid Email" else null
            }
        }

        // Password: length check on focus loss
        binding.regPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.textInputLayout.error = null
            } else {
                val pass = binding.regPassword.text?.toString().orEmpty()
                binding.textInputLayout.error =
                    if (pass.length < 6) "Password must be at least 6 characters" else null
            }
        }

        // Confirm: match check on focus loss
        binding.regRetypePassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.textInputLayout2.error = null
            } else {
                val pass = binding.regPassword.text?.toString().orEmpty()
                val confirm = binding.regRetypePassword.text?.toString().orEmpty()
                binding.textInputLayout2.error =
                    if (confirm.length < 6 || pass != confirm) "Passwords don't match" else null
            }
        }

        // "Go" on confirm: hide keyboard and submit
        binding.regRetypePassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                hideKeyboard()
                binding.regAccBtn.performClick()
                true
            } else false
        }

        binding.regAccBtn.setOnClickListener {
            hideKeyboard()
            createAcc()
        }

        binding.loginAccActivityBtn.setOnClickListener { switchToLoginAcc() }
    }

    private fun switchToLoginAcc() {
        startActivity(Intent(this, LoginAccount::class.java))
        finish()
    }

    private fun createAcc() {
        val email = binding.regEmail.text?.toString()?.trim().orEmpty()
        val pass = binding.regPassword.text?.toString().orEmpty()
        val confirm = binding.regRetypePassword.text?.toString().orEmpty()

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
            Toast.makeText(this, "Invalid Data Entered", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        FirebaseRefs.auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    FirebaseRefs.auth.currentUser?.sendEmailVerification()
                    Toast.makeText(
                        this,
                        "Account created Successfully.",
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

    /** Disable button and show in-button spinner */
    private fun setLoading(loading: Boolean) {
        binding.regAccBtn.isEnabled = !loading
        binding.btnProgress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.regAccBtn.text = if (loading) "" else createBtnLabel
    }

    private fun hideKeyboard() {
        currentFocus?.windowToken?.let {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it, 0)
        }
    }
}

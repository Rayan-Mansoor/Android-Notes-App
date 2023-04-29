package com.android.app.notesapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.android.app.notesapp.databinding.ActivityLoginAccountBinding
import com.google.firebase.auth.FirebaseAuth

class LoginAccount : AppCompatActivity() {
    private lateinit var binding : ActivityLoginAccountBinding
    private var isInfoValid : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.myEmail.hint = "Email Address"
        binding.myPassword.hint = "Password"

        binding.myEmail.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus){
//                if (binding.myEmail.error != null){
//                    binding.myEmail.error = "Invalid Email"
//                }
//                else{
                    binding.textInputLayout5.error = null
//                }
            }
            else if (!hasFocus){
                if (!Patterns.EMAIL_ADDRESS.matcher(binding.myEmail.text.toString()).matches()){
                    isInfoValid = false
                    binding.textInputLayout5.error = "Invalid Email"
                }
                else{
                    isInfoValid = true
                }
            }
        }

        binding.myEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.myEmail.hint = null
                if (binding.myEmail.text.isNullOrEmpty()){
                    binding.myEmail.hint = "Email Address"
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

//        binding.myPassword.setOnFocusChangeListener { view, hasFocus ->
//            Log.d("CreateAccount","Focused change listener of password called")
//            if (hasFocus){
////
////                if (binding.textInputLayout.error != null){
////                    binding.textInputLayout.error = "Password Length should be at least 6"
////                }
////                else{
//                binding.textInputLayout4.error = null
////                }
//            }
//            else if (!hasFocus){
//                if (binding.myPassword.text.toString().length<6){
//                    binding.textInputLayout4.error = "Password Length should be at least 6"
//                    isInfoValid = false
//                    //binding.regPassword.error = "Password Length should be at least 6"
//                }
//                else{
//                    isInfoValid = true
//                }
//            }
//        }

        binding.myPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.myPassword.hint = null
                if (binding.myPassword.text.isNullOrEmpty()){
                    binding.myPassword.hint = "Password"
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        })

//        binding.myEmail.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
//
//            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
//
//            override fun afterTextChanged(p0: Editable?) {
//                isInfoValid = true
//                binding.myEmail.error = null
//                if (!Patterns.EMAIL_ADDRESS.matcher(binding.myEmail.text.toString()).matches()){
//                    isInfoValid = false
//                    binding.myEmail.error = "Invalid Email"
//                }
//            }
//        })

        binding.loginAccBtn.setOnClickListener {
            binding.myEmail.clearFocus()
            loginAcc()
        }

        binding.createAccActivityBtn.setOnClickListener {
            switchToCreateAcc()
        }

    }

    private fun switchToCreateAcc() {
        startActivity(Intent(this,CreateAccount::class.java))
        finish()
    }

    private fun loginAcc() {
        if (isInfoValid){
            setProgressBar(true)
            val firebaseAuth = FirebaseAuth.getInstance()
            firebaseAuth.signInWithEmailAndPassword(binding.myEmail.text.toString(),binding.myPassword.text.toString()).addOnCompleteListener(this) { task ->
                if (task.isSuccessful){
                    setProgressBar(false)
                    Toast.makeText(this,"Login Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this,MainActivity::class.java))
                    finish()
//                    if (firebaseAuth.currentUser!!.isEmailVerified){
//                        Toast.makeText(this,"Login Successful", Toast.LENGTH_SHORT).show()
//                        startActivity(Intent(this,MainActivity::class.java))
//                        finish()
//                    }
//                    else{
//                        Toast.makeText(this,"Email not verified. Kindly verify your Email ", Toast.LENGTH_SHORT).show()
//                    }
                }
                else{
                    setProgressBar(false)
                    binding.myPassword.text?.clear()
                    Toast.makeText(this,"Incorrect Credentials ", Toast.LENGTH_SHORT).show()
                }
            }
        }
        else{
            Toast.makeText(this,"Invalid Email Entered", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setProgressBar(progress : Boolean){
        if (progress){
            binding.progressBar.visibility = View.VISIBLE
        }
        else
            binding.progressBar.visibility = View.INVISIBLE
    }
}
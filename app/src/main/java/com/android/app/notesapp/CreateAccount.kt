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
import com.android.app.notesapp.databinding.ActivityCreateAccountBinding
import com.google.firebase.auth.FirebaseAuth

class CreateAccount : AppCompatActivity() {
    private lateinit var binding : ActivityCreateAccountBinding
    private var isInfoValid : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.regEmail.hint = "Email Address"
        binding.regPassword.hint = "Password"
        binding.regRetypePassword.hint = "Confirm Password"

        binding.regEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.regEmail.hint = null
                if (binding.regEmail.text.isNullOrEmpty()){
                    binding.regEmail.hint = "Email Address"
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.regEmail.setOnFocusChangeListener { view, hasFocus ->
            Log.d("CreateAccount","Focused change listener of email called")
            if (hasFocus){
//                if (binding.regEmail.error != null){
//                    binding.regEmail.error = "Invalid Email"
//                }
//                else{
                    binding.textInputLayout3.error = null
//                }
            }
            else if(!hasFocus){
                if (!Patterns.EMAIL_ADDRESS.matcher(binding.regEmail.text.toString()).matches()){
                    isInfoValid = false
                    binding.textInputLayout3.error = "Invalid Email"
                }
                else{
                    isInfoValid = true
                }
            }
        }


        binding.regPassword.setOnFocusChangeListener { view, hasFocus ->
            Log.d("CreateAccount","Focused change listener of password called")
            if (hasFocus){
//
//                if (binding.textInputLayout.error != null){
//                    binding.textInputLayout.error = "Password Length should be at least 6"
//                }
//                else{
                    binding.textInputLayout.error = null
//                }
            }
            else if (!hasFocus){
                if (binding.regPassword.text.toString().length<6){
                    binding.textInputLayout.error = "Password Length should be at least 6"
                    isInfoValid = false
                    //binding.regPassword.error = "Password Length should be at least 6"
                }
                else{
                    isInfoValid = true
                }
            }
        }

        binding.regPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.regPassword.hint = null
                if (binding.regPassword.text.isNullOrEmpty()){
                    binding.regPassword.hint = "Password"
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        })

        binding.regRetypePassword.setOnFocusChangeListener { view, hasFocus ->
            Log.d("CreateAccount","Focused change listener of confirm password called")
            if (hasFocus){
//                if (binding.textInputLayout2.error != null){
//                    binding.textInputLayout2.error = "Passwords don't match"
//                }
//                else{
                    binding.textInputLayout2.error = null
//                }
            }
            else if (!hasFocus){
                if (binding.regRetypePassword.text.toString().length<6){
                    isInfoValid = false
                  //  binding.regRetypePassword.error = "Passwords don't match"
                    binding.textInputLayout2.error = "Passwords don't match"
                }
                else{
                    isInfoValid = true
                }
            }
        }

        binding.regRetypePassword.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.regRetypePassword.hint = null
                if (binding.regRetypePassword.text.isNullOrEmpty()){
                    binding.regRetypePassword.hint = "Confirm Password"
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        })

        binding.regAccBtn.setOnClickListener {
            binding.regEmail.clearFocus()
            binding.regPassword.clearFocus()
            binding.regRetypePassword.clearFocus()
            createAcc()
        }

        binding.loginAccActivityBtn.setOnClickListener {
            switchToLoginAcc()
        }
    }

    private fun switchToLoginAcc() {
        startActivity(Intent(this,LoginAccount::class.java))
        finish()
    }

    private fun setProgressBar(progress : Boolean){
        if (progress){
            binding.progressBar.visibility = View.VISIBLE
        }
        else
            binding.progressBar.visibility = View.INVISIBLE
    }

    private fun createAcc(){
        if (isInfoValid){
            setProgressBar(true)
            val firebaseAuth = FirebaseAuth.getInstance()
            firebaseAuth.createUserWithEmailAndPassword(binding.regEmail.text.toString(),binding.regPassword.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful){
                        Toast.makeText(applicationContext,"Account Created Successfully",Toast.LENGTH_SHORT).show()
                        setProgressBar(false)
                        firebaseAuth.currentUser!!.sendEmailVerification()
                        firebaseAuth.signOut()
                        startActivity(Intent(this,LoginAccount::class.java))
                        finish()
                    }
                    else{
                        Toast.makeText(this,"Account Creation Failed. Try Again",Toast.LENGTH_SHORT).show()
                        setProgressBar(false)

                    }

                }

            }
            else{
                Toast.makeText(applicationContext,"Invalid Data Entered",Toast.LENGTH_SHORT).show()
            }
        }
    }

package com.capstone.carakamobile

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.capstone.carakamobile.databinding.ActivitySignUpBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val layoutPassword = binding.layoutPassword
        val editTextPassword = binding.signupPass

        editTextPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Nothing to do here
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = s.toString()

                if (password.length < 8) {
                    layoutPassword.helperText = "Minimum 8 characters"
                    layoutPassword.error = "Password must be at least 8 characters"
                } else {
                    layoutPassword.helperText = null
                    layoutPassword.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Nothing to do here
            }
        })

        binding.signinGoogle.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        binding.signupTextView.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        binding.signupButton.setOnClickListener {
            val name = binding.signupUsername.text.toString()
            val email = binding.signupEmail.text.toString()
            val pass = binding.signupPass.text.toString()
            val confirmpass = binding.signupConfirmpass.text.toString()

            database = FirebaseDatabase.getInstance()
            reference = database.getReference("Users")

            val User = User(name,email,pass)

            if (name.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() && confirmpass.isNotEmpty()){
                if (pass == confirmpass){
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful){
                            reference.child(name).setValue(User).addOnSuccessListener {
                                val intent = Intent(this, SignInActivity::class.java)
                                startActivity(intent)

                                val helperClass = HelperClass(name, email, pass)
                                reference.child(name).setValue(helperClass)

                                Toast.makeText(this, "User registered succesfully", Toast.LENGTH_SHORT).show()
                            }
                        } else{
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
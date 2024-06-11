package com.capstone.carakamobile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.capstone.carakamobile.databinding.ActivitySignInBinding
import com.capstone.carakamobile.databinding.CustomDialogBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var loadingDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)

        val layoutPassword = binding.layoutPassword
        val editTextPassword = binding.signinPass

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

        binding.signinTextView.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.signinButton.setOnClickListener {
            val email = binding.signinEmail.text.toString()
            val pass = binding.signinPass.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                showLoadingDialog()
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        Toast.makeText(this, "Signed in as ${user?.displayName}", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                        loadingDialog.dismiss()
                    } else {
                        val intent = Intent(this, SignInActivity::class.java)
                        startActivity(intent)
                        Toast.makeText(this, "SignIn Failed, Try again later", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()
            }
        }
        binding.signinGoogle.setOnClickListener {
            showLoadingDialog()
            signInWithGoogle()
        }
    }

    private fun showLoadingDialog() {
        val dialogBinding = CustomDialogBinding.inflate(LayoutInflater.from(this))
        loadingDialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()
        loadingDialog.show()

        // Simulasi loading selama 3 detik
        Handler().postDelayed({
            loadingDialog.dismiss()
            // Pindah ke HomeActivity setelah loading selesai
//            val intent = Intent(this, HomeActivity::class.java)
//            startActivity(intent)
            finish() // Menutup MainActivity
        }, 5000)
    }
    private fun signInWithGoogle(){
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if (result.resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful){
            val account:GoogleSignInAccount? = task.result
            if(account!=null){
                updateUI(account)
            }
        }else{
            Toast.makeText(this, "SignIn Failed, Try again later", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount){
//        showProgressBar()
        val credential = GoogleAuthProvider.getCredential(account.idToken,null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener{
            if(it.isSuccessful){
                val user = firebaseAuth.currentUser
                Toast.makeText(this, "Signed in as ${user?.displayName}", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
                loadingDialog.dismiss()
            }else{
                Toast.makeText(this, "SignIn Failed, Try again later", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val user = firebaseAuth.currentUser
        if(firebaseAuth.currentUser!=null){
            startActivity(Intent(this, HomeActivity::class.java))
            Toast.makeText(this, "Signed in as ${user?.displayName}", Toast.LENGTH_SHORT).show()
        }
    }
}
package com.cs4530.domaspiragas.assignment3

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.android.synthetic.main.activity_login.*

/**This activity represents the screen the player sees first when they are not logged in*/
class LoginActivity : AppCompatActivity() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onBackPressed() {
        finish()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // user is already logged in, redirect to the appropriate page
        if (firebaseAuth.currentUser != null) {
            if (firebaseAuth.currentUser!!.isEmailVerified) {
                val intent = Intent(this, GameListActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, UnverifiedActivity::class.java)
                startActivity(intent)
            }
        }
        // Create Account text clicked
        registerRedirect.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        })
        // Sign In button clicked
        signInButton.setOnClickListener(View.OnClickListener {
            userLogin()
        })
    }

    /**Determined whether or not the user has entered valid credentials.
     * Signing in will redirect to either the Unverified page if the
     * user has not verified their email. Or the game list view if they have.*/
    private fun userLogin() {
        val email: String = emailBox.text.toString().trim()
        val password: String = passwordBox.text.toString().trim()

        // Error checking
        if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter an email and password", Toast.LENGTH_SHORT).show()
            return
        } else if (TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
            return
        } else if (!TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
            return
        } else { // email and password exist log them in
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    //Sign in OK
                    Toast.makeText(this, "Welcome Back!", Toast.LENGTH_SHORT).show()
                    if (firebaseAuth.currentUser!!.isEmailVerified) {
                        val intent = Intent(this, GameListActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this, UnverifiedActivity::class.java)
                        startActivity(intent)
                    }
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "Invalid credentials!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to log in!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

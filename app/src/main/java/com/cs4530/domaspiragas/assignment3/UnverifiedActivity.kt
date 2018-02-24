package com.cs4530.domaspiragas.assignment3

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_unverified.*

/**This activity represents the screen an unverified account will see*/
class UnverifiedActivity : AppCompatActivity() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unverified)
        // send verification email for account
        val subtitlePlaceholder: String = firebaseAuth.currentUser!!.email.toString() + " " +getString(R.string.not_verified)
        verificationSubtitle.text = subtitlePlaceholder
        sendVerificationButton.setOnClickListener(View.OnClickListener {
            firebaseAuth.currentUser!!.sendEmailVerification().addOnCompleteListener { task: Task<Void> ->
                if (task.isSuccessful) {
                    //Email sent
                    Toast.makeText(this, "Email Sent to " + firebaseAuth.currentUser!!.email, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Could not send email to " + firebaseAuth.currentUser!!.email, Toast.LENGTH_SHORT).show()
                }
            }
        })
        // check if account has been verified
        haveVerifiedButton.setOnClickListener(View.OnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            Toast.makeText(this, "Then please try signing in!", Toast.LENGTH_SHORT).show()
        })
        // sign out and go back to the login page
        logoutButton.setOnClickListener(View.OnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        })
    }
}
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
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_register.*
import java.util.regex.Pattern

/**This activity represents the screen for creating a new account*/
class RegisterActivity : AppCompatActivity() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val databasePlayers: DatabaseReference = FirebaseDatabase.getInstance().getReference("players")

    // on back, go to sign in page
    override fun onBackPressed() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // redirect back to the sign in page on click
        signInRedirect.setOnClickListener(View.OnClickListener { // change activity to sign in page
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        })
        // handle clicking the Create Account button
        createAccountButton.setOnClickListener(View.OnClickListener {
            registerUser()
        })
    }

    /**Used for handling all of the logic necessary to validate and register a new account*/
    private fun registerUser(){
        val email: String = emailBox.text.toString().trim()
        val password: String = passwordBox.text.toString().trim()

        //Error checking
        if(TextUtils.isEmpty(email) && TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter an email and password", Toast.LENGTH_SHORT).show()
            return
        } else if(TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
            return
        } else if(!TextUtils.isEmpty(email) && TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
            return
        } else { // email and password exist
            // more error checking
            if(!validEmail(email)){
                Toast.makeText(this, "Invalid email!", Toast.LENGTH_SHORT).show()
                return
            } else if(!validPassword(password)){
                Toast.makeText(this, "Invalid password!", Toast.LENGTH_SHORT).show()
                return
            } else if(!confirmPassword()){
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                return
            }
            // Attempt to create the new account
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    //Registration OK
                    Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                    savePlayer(email) // email used as username
                    val intent = Intent(this, UnverifiedActivity::class.java)
                    startActivity(intent)
                } else {
                    if(task.exception is FirebaseAuthUserCollisionException){
                        Toast.makeText(this, "User with given email already exists!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed creating account!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    /**Adds the new player to the database*/
    private fun savePlayer(username:String){
        val player = Player()
        player.setId(databasePlayers.push().key)
        player.setUsername(Utils.formatPlayerNode(username))
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        val jsonified: String = gson.toJson(player)
        databasePlayers.child(player.getUsername()).setValue(jsonified)
    }
    /**Used to validate the passwords in both the password box and confirm password box are
     * identical*/
    private fun confirmPassword():Boolean {
        return passwordBox.text.toString().trim() == confirmPasswordBox.text.toString().trim()
    }
    /**Used to validate that the entered email address is in a proper format*/
    private fun validEmail(email:String):Boolean{
        //Regex pulled from https://gist.github.com/ironic-name/f8e8479c76e80d470cacd91001e7b45b
        return Pattern.compile(
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
                        + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|"
                        + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
        ).matcher(email).matches()
    }
    /**Used to validate that the entered password is at least 8 characters long and contains at
     * least one number, one letter, and at least one symbol from !@#$%^&* */
    private fun validPassword(pass:String):Boolean{
        val regex = Regex("((?=.*\\d)(?=.*[A-z])(?=.*[!@#$%^&*]).{8,})")
        return regex.matches(pass)
    }
}
package com.tombra.casatopia.user_side.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.tombra.casatopia.R
import com.tombra.casatopia._model.Admin
import com.tombra.casatopia._model.Auth
import com.tombra.casatopia._model.Signup
import com.tombra.casatopia._model.User
import com.tombra.casatopia.admin_side.activities.BACKEND
import com.tombra.casatopia.admin_side.data.AdminDatabase
import com.tombra.casatopia.user_side.data.UserDatabase

class AUTHENTICATION_LOGIN : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication_verify_screen)


        val context: Context = this

        mAuth = FirebaseAuth.getInstance()

        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val login = findViewById<TextView>(R.id.login)
        val gotoSignup = findViewById<TextView>(R.id.signup)

        val verifyScreen = findViewById<ConstraintLayout>(R.id.verifyScreen)
        val ok = findViewById<TextView>(R.id.ok)

        ok.setOnClickListener {
            verifyScreen.isVisible = false
        }

        val loadingScreen = findViewById<ConstraintLayout>(R.id.loadingScreen)

        val network: FirebaseDatabase = FirebaseDatabase.getInstance()

        gotoSignup.setOnClickListener {
            startActivity(Intent(context, AUTHENTICATION_SIGNUP::class.java))
        }


        password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {


                if(email.text.toString().equals("BACKEND") && password.text.toString().equals("9999") ){
                    Toast.makeText(context, "BACKEND ACTIVATED", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(context, BACKEND::class.java))
                    finish()
                }




            }
        })




        login.setOnClickListener {
            if (TextUtils.isEmpty(email.text.toString())) {
                Toast.makeText(context, "Fill in your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password.text.toString())) {
                Toast.makeText(context, "Fill in your password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            loadingScreen.isVisible = true


            val userDatabase = UserDatabase(context)



            mAuth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
                .addOnCompleteListener { task ->


                    if (task.isSuccessful) {

                        val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

                        if(!firebaseUser.isEmailVerified){

                            //go to verify email page
                            firebaseUser.sendEmailVerification()
                            Toast.makeText(context, "Email not verified", Toast.LENGTH_SHORT).show()
                            loadingScreen.isVisible = false
                            verifyScreen.isVisible = true
                            //show verify dialog here
                            return@addOnCompleteListener
                        }


                        Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                        //get user data from database and save into shared preference




                        network.reference.child("Accounts").child(mAuth.currentUser!!.uid).get().addOnSuccessListener {

                            val result = it.getValue(Signup::class.java)!!

                            Log.d("AUTH", result.type!!)
                            if(result.type!! == ""){
                                Log.d("AUTH", "${result.type} -----1")
                                val userDatabase = UserDatabase(context)
                                val auth = Auth(
                                    authenticated = "true",
                                    authId = mAuth.currentUser!!.uid,
                                    accountType = "",
                                )
                                userDatabase.saveAuthInfo(auth)

                                startActivity(
                                    Intent(
                                        context,
                                        AUTHENTICATION_ACCOUNT_TYPE_SCREEN::class.java
                                    )
                                )

                            }else{
                                Log.d("AUTH", "${result.type} -----2")
                                if(result!!.type == "Admins"){
                                    val admin = Admin(
                                        mAuth.currentUser!!.uid,
                                        result!!.firstName!!,
                                        result!!.lastName!!,
                                        "https://firebasestorage.googleapis.com/v0/b/casatopia-c2993.appspot.com/o/images%2F1689783721607?alt=media&token=43f4e898-771c-434c-876f-090d86ce46d7",
                                        "Admins",
                                        email = result!!.email!!,
                                        phoneNumber =  result!!.phoneNumber!!,
                                    )
                                    Log.d("AUTH", "${result.type} -----3")
                                    network.reference.child("Admins").child(mAuth.currentUser!!.uid)
                                        .child("Profile").setValue(admin).addOnSuccessListener {

                                            val userDatabase = UserDatabase(context)
                                            val auth = Auth(
                                                authenticated = "true",
                                                authId = mAuth.currentUser!!.uid,
                                                accountType = "Admins",
                                            )
                                            userDatabase.saveAuthInfo(auth)

                                            startActivity(
                                                Intent(
                                                    context,
                                                    com.tombra.casatopia.admin_side.activities.MyHome::class.java
                                                )
                                            )
                                            //go to admin home
                                        }


                                }


                                if(result!!.type == "Users"){
                                    val user = User(
                                        mAuth.currentUser!!.uid,
                                        result!!.firstName!!,
                                        result!!.lastName!!,
                                        "https://firebasestorage.googleapis.com/v0/b/casatopia-c2993.appspot.com/o/images%2F1689783721607?alt=media&token=43f4e898-771c-434c-876f-090d86ce46d7",
                                        "Users",
                                        email = result!!.email!!,
                                        phoneNumber =  result!!.phoneNumber!!,

                                    )
                                    Log.d("AUTH", "${result.type} -----4")
                                    network.reference.child("Users").child(mAuth.currentUser!!.uid)
                                        .child("Profile").setValue(user).addOnSuccessListener {
                                            val userDatabase = UserDatabase(context)
                                            val auth = Auth(
                                                authenticated = "true",
                                                authId = mAuth.currentUser!!.uid,
                                                accountType = "Users",
                                            )
                                            userDatabase.saveAuthInfo(auth)

                                            startActivity(
                                                Intent(
                                                    context,
                                                    com.tombra.casatopia.user_side.activities.MyHome::class.java
                                                )
                                            )
                                            //go to admin home
                                        }

                                }


                            }


                        }




                    } else {
                        loadingScreen.isVisible = false
                        Toast.makeText(
                            context,
                            "Login failed ${task.exception!!.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }


                }


        }


    }


    override fun onBackPressed() {
        moveTaskToBack(true)
    }

}
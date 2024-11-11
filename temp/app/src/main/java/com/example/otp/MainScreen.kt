package com.example.otp

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

@Composable
fun MainScreen(modifier: Modifier){
    val navController= rememberNavController()

    NavHost(navController = navController, startDestination = "home" ){
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("otp") {
            OtpScreen(navController = navController)
        }
        composable("success") {
            SuccessScreen(navController = navController)
        }
    }

}
val auth: FirebaseAuth = FirebaseAuth.getInstance()
var storedVerificationId =""



fun signInWithPhoneAuthCredential(context: Context,credential: PhoneAuthCredential,navController: NavController){
    auth.signInWithCredential(credential)
        .addOnCompleteListener(context as Activity){ task ->
            if(task.isSuccessful){
                Toast.makeText(context,"OTP verification Successful",Toast.LENGTH_SHORT).show()
                navController.navigate("success")
                val user=task.result?.user
            }
            else{
                if(task.exception is FirebaseAuthInvalidCredentialsException){
                    Toast.makeText(context,"Wrong OTP ", Toast.LENGTH_SHORT).show()
                }
            }
        }
}
fun onLoginClicked(
    context: Context,navController: NavController,phoneNumber: String,onCodeSend:()-> Unit
){
    auth.setLanguageCode("en")
    val callback= object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            Log.d("phoneBook", "verification completed")
            signInWithPhoneAuthCredential(context, p0, navController)
        }

//        override fun onVerificationFailed(p0: FirebaseException) {
//            Log.d("phoneBook","verification failed ${p0.message}")
//            Toast.makeText(context, "Verification failed: ${p0.message}", Toast.LENGTH_SHORT).show()
//
//
//        }
//
//        override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
//            Log.d("phoneBook","code sent $p0")
//           storedVerificationId=p0
//            onCodeSend()
//
//        }
//
//    }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(
                    context,
                    "Invalid phone number format. Please check the number.",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (e is FirebaseTooManyRequestsException) {
                Toast.makeText(
                    context,
                    "Too many requests. Please try again later.",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (e is FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(
                    context,
                    "Invalid credentials. Please check the phone number.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            else  {
                // For other exceptions, display a generic message
                Toast.makeText(
                    context,
                    "Verification failed. Please try again later.",
                    Toast.LENGTH_SHORT
                ).show()


            }
    }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:$verificationId")

            // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId
            var resendToken = token
        }
    }

    val option= PhoneAuthOptions.newBuilder(auth)
        .setPhoneNumber("+91$phoneNumber")
        .setTimeout(60L,TimeUnit.SECONDS)
        .setActivity(context as Activity)
        .setCallbacks(callback)
        .build()
    PhoneAuthProvider.verifyPhoneNumber(option)
}
fun verifyPhoneNumberWithCod(
    context: Context,verificationId:String,code:String,navController: NavController){
    val credential=PhoneAuthProvider.getCredential(verificationId,code)
    signInWithPhoneAuthCredential(context,credential,navController)
}



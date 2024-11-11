package com.example.otp

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

//
//@Composable
//fun OtpScreen(navController: NavController){
//    val context = LocalContext.current
//    var otp by remember {
//        mutableStateOf("")
//    }
//    var isOtpSent by remember { mutableStateOf(false) }
//    var resendToken: PhoneAuthProvider.ForceResendingToken? by remember { mutableStateOf(null) }
//    val bgColor = Color(0xFFECFADC)
//    val tColor = Color(0xFF2B472B)
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(bgColor),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        Text(text = "verification", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = tColor)
//        Spacer(modifier = Modifier.size(10.dp))
//        Text(text = "You ill get a OTP vai SMS", color = tColor)
//        Spacer(modifier = Modifier.size(50.dp))
//     BasicTextField(value = otp, onValueChange ={
//         if(it.length<=6) otp=it
//     } ,
//         keyboardOptions = KeyboardOptions(
//             keyboardType = KeyboardType.Number
//         )
//          ){
//         Row(horizontalArrangement = Arrangement.spacedBy(10.dp)){
//             repeat(6){
//                 index->
//                 val number=when{
//                     index>=otp.length->""
//                     else->otp[index]
//                 }
//                 Column(verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
//                     Text(
//                         text=number.toString(), style = MaterialTheme.typography.titleLarge, color = tColor
//                     )
//                     Box(
//                         modifier = Modifier.width(40.dp).height(2.dp).background(tColor)
//                     ){
//
//                     }
//                 }
//             }
//
//         }
//     }
//        Button(
//            onClick = {
//                verifyPhoneNumberWithCod(context, storedVerificationId, otp, navController)
//            },
//            colors = ButtonDefaults.buttonColors(
//                if (otp.length >= 10) tColor else Color.Gray
//            )
//        ) {
//            Text(text = "Verify", color = Color.White)
//        }
//
//    }
//}

@Composable
fun OtpScreen(navController: NavController) {
    val context = LocalContext.current
    var otp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var resendToken: PhoneAuthProvider.ForceResendingToken? by remember { mutableStateOf(null) }
    val bgColor = Color(0xFFECFADC)
    val tColor = Color(0xFF2B472B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Verification",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = tColor
        )
        Spacer(modifier = Modifier.size(10.dp))
        Text(text = "You will get an OTP via SMS", color = tColor)
        Spacer(modifier = Modifier.size(50.dp))

        // OTP Input Field
        OutlinedTextField(
            value = otp,
            onValueChange = {
                if (it.length <= 6) otp = it
            },
            label = { Text("Enter OTP") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        Spacer(modifier = Modifier.size(20.dp))

        // Verify OTP Button
        Button(
            onClick = {
                if (otp.length == 6) {
                    verifyPhoneNumberWithCod(context, storedVerificationId, otp, navController)
                } else {
                    Toast.makeText(context, "Please enter a valid 6-digit OTP", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor  = if (otp.length == 6) tColor else Color.Gray
            ),
            enabled = otp.length == 6
        ) {
            Text(text = "Verify", color = Color.White)
        }

        Spacer(modifier = Modifier.size(20.dp))

        // Resend OTP Button
        if (!isOtpSent) {
            Button(
                onClick = {
                    resendOtp(context, navController, "Enter your number",resendToken)
                },
                colors = ButtonDefaults.buttonColors(containerColor = tColor),
            ) {
                Text(text = "Resend OTP", color = Color.White)
            }
        }
    }

    // Start the OTP process (sending)
    if (!isOtpSent) {
        sendOtp(context, navController ,"Enter your number" ) { token ->
            isOtpSent = true
            resendToken = token
        }
    }
}

// Function to send OTP
fun sendOtp(
    context: Context,
    navController: NavController,
    phoneNumber: String,
    onCodeSent: (PhoneAuthProvider.ForceResendingToken) -> Unit
) {
    val callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            Log.d("phoneBook", "verification completed")
            signInWithPhoneAuthCredential(context, p0, navController)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.w(TAG, "onVerificationFailed", e)
            Toast.makeText(
                context,
                "Verification failed. Please try again later.",
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            Log.d(TAG, "onCodeSent:$verificationId")
            storedVerificationId = verificationId
            onCodeSent(token)  // Passing the token to resend
        }
    }

    val options = PhoneAuthOptions.newBuilder(auth)
        .setPhoneNumber("+91$phoneNumber")
        .setTimeout(60L, TimeUnit.SECONDS)
        .setActivity(context as Activity)
        .setCallbacks(callback)
        .build()

    PhoneAuthProvider.verifyPhoneNumber(options)
}

// Function to resend OTP
fun resendOtp(
    context: Context,
    navController: NavController,
    phoneNumber: String,
    resendToken: PhoneAuthProvider.ForceResendingToken?
) {
    if (resendToken != null) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(context as Activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    Log.d("phoneBook", "verification completed")
                    signInWithPhoneAuthCredential(context, p0, navController)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.w(TAG, "onVerificationFailed", e)
                    Toast.makeText(
                        context,
                        "Verification failed. Please try again later.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken,
                ) {
                    Log.d(TAG, "onCodeSent:$verificationId")
                    storedVerificationId = verificationId
                }
            })
            .setForceResendingToken(resendToken)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    } else {
        Toast.makeText(context, "Resend OTP failed. Please try again later.", Toast.LENGTH_SHORT).show()
    }
}

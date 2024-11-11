package com.example.otp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.provider.FontsContractCompat.Columns
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun SuccessScreen(navController: NavController){
val bgColor= Color(0xFFECFADC)
    Column(modifier = Modifier
        .fillMaxSize()
        .background(bgColor), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
        Image(
            painter= painterResource(id =R.drawable.img ),
            contentDescription = null, modifier = Modifier.size(500.dp)
        )
        Button(onClick = {
            Firebase.auth.signOut()
                navController.navigate("home")}
        ) {
Text(text="log out")
        }
    }
}



package com.example.chatapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ActivityCompat.checkSelfPermission(this
                        , Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this
                    , arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    , 10);

        BtnEnterRoom.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("name", etEnterName.text.toString())
            startActivity(intent)
        }
    }
}
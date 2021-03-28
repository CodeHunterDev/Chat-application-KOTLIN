package com.example.chatapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.item_send_image.*
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.util.*

open class ChatActivity : AppCompatActivity(), TextWatcher {

    private lateinit var name: String
    private lateinit var webSocket: WebSocket
    private var SERVER_PATH: String = "ws://10.0.2.2:3000"
    private var IMAGE_REQUEST_CODE = 8
    private lateinit var messageAdapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        name = intent.getStringExtra("name") as String
        initiateSocketConnection()

    }

    private fun initiateSocketConnection() {
        val client = OkHttpClient()
        val request = Request.Builder().url(SERVER_PATH).build()
        webSocket = client.newWebSocket(request, SocketListener())
    }
    inner class SocketListener() : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)

            runOnUiThread {
                Toast.makeText(this@ChatActivity, "Socket Connection successful!"
                        , Toast.LENGTH_SHORT).show()
                initializeView()
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            runOnUiThread {
                try {
                    val jsonObject = JSONObject(text)
                    jsonObject.put("isSent", false)
                    messageAdapter.addItem(jsonObject)
                    rvChat.smoothScrollToPosition(messageAdapter.itemCount - 1)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }
    private fun initializeView() {
        messageAdapter = MessageAdapter(this)
        rvChat.adapter = messageAdapter
        rvChat.layoutManager = LinearLayoutManager(this)
        etMessage.addTextChangedListener(this)

        tvSend.setOnClickListener {
            val jsonObject = JSONObject()
            try {
                jsonObject.put("name", name)
                jsonObject.put("message", etMessage.text.toString())
                webSocket.send(jsonObject.toString())
                jsonObject.put("isSent", true)
                messageAdapter.addItem(jsonObject)
                rvChat.smoothScrollToPosition(messageAdapter.itemCount - 1)
                resetMessageEdit()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        ivPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Pick image"),
                    IMAGE_REQUEST_CODE)
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }


    override fun afterTextChanged(s: Editable?) {
        val string = s.toString().trim()
        if (string.isEmpty()) {
            resetMessageEdit()
        } else {
            tvSend.visibility = View.VISIBLE
            ivPickImage.visibility = View.INVISIBLE
        }
    }
    private fun resetMessageEdit() {
        etMessage.removeTextChangedListener(this)
        etMessage.setText("")
        tvSend.visibility = View.INVISIBLE
        ivPickImage.visibility = View.VISIBLE
        etMessage.addTextChangedListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val image = data!!.data
            try {
                val source = ImageDecoder.createSource(this.contentResolver, image!!)
                val imageBitmap = ImageDecoder.decodeBitmap(source)
                sendImage(imageBitmap)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    private fun sendImage(imageBitmap: Bitmap) {
        val outputStream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 20, outputStream)
        val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        val jsonObject = JSONObject()

        try {
            jsonObject.put("name", name)
            jsonObject.put("image", base64String)
            webSocket.send(jsonObject.toString())
            jsonObject.put("isSent", true)
            messageAdapter.addItem(jsonObject)
            rvChat.smoothScrollToPosition(messageAdapter.itemCount - 1)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

}
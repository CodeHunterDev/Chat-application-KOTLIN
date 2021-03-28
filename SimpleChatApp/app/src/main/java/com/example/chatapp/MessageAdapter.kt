package com.example.chatapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_receive_image.view.*
import kotlinx.android.synthetic.main.item_receive_message.view.*
import kotlinx.android.synthetic.main.item_send_image.view.*
import kotlinx.android.synthetic.main.item_send_message.view.*
import org.json.JSONException
import org.json.JSONObject
import android.util.Base64
import android.util.Log
import kotlin.collections.ArrayList


class MessageAdapter(private val context: Context): RecyclerView.Adapter<MessageAdapter.MyViewHolder>() {

    private val TYPE_MESSAGE_SENT = 5
    private val TYPE_MESSAGE_RECEIVED = 10
    private val TYPE_IMAGE_SENT = 93
    private val TYPE_IMAGE_RECEIVED = 38
    private var messages : ArrayList<JSONObject> = ArrayList()


    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {

    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        try {
            return if (message.getBoolean("isSent")) {

                if (message.has("message")) {
                    TYPE_MESSAGE_SENT
                } else {
                    TYPE_IMAGE_SENT
                }
            } else {

                if (message.has("message")) {
                    TYPE_MESSAGE_RECEIVED
                } else {
                    TYPE_IMAGE_RECEIVED
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return  -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var view : View? = null
        when (viewType) {
            TYPE_MESSAGE_SENT -> {
                view = LayoutInflater.from(context).inflate(R.layout.item_send_message, parent, false)
                return MyViewHolder(view)
            }
            TYPE_MESSAGE_RECEIVED -> {
                view = LayoutInflater.from(context).inflate(R.layout.item_receive_message, parent, false)
                return MyViewHolder(view)
            }
            TYPE_IMAGE_SENT -> {
                view = LayoutInflater.from(context).inflate(R.layout.item_send_image, parent, false)
                return MyViewHolder(view)
            }
            TYPE_IMAGE_RECEIVED -> {
                view = LayoutInflater.from(context).inflate(R.layout.item_receive_image, parent, false)
                return MyViewHolder(view)
            }
            else -> {
                return MyViewHolder(view!!)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val message = messages[position]
        try {
            if (message.getBoolean("isSent")) {

                if (message.has("message")) {
                    holder.itemView.tvSendText.text = message.getString("message")
                } else {
                    val bitmap = getBitmapFromString(message.getString("image"))
                    holder.itemView.ivSendImage.setImageBitmap(bitmap)
                }
            } else {

                if (message.has("message")) {
                    holder.itemView.tvMessageSenderName.text = message.getString("name")
                    holder.itemView.tvReceiveMessage.text = message.getString("message")
                } else {
                    holder.itemView.tvImageSenderName.text = message.getString("name")
                    val bitmap = getBitmapFromString(message.getString("image"))
                    holder.itemView.ivReceiveImage.setImageBitmap(bitmap)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getBitmapFromString(image: String): Bitmap? {
        val bytes = Base64.decode(image, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
    internal fun addItem(jsonObject: JSONObject) {
        messages.add(jsonObject)
        notifyDataSetChanged()
    }

    override fun getItemCount() = messages.size
}
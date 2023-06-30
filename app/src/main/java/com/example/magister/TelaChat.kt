package com.example.magister

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
//import com.example.magister.TelaChat.Companion.EXTRA_USER_ID
import com.example.magister.databinding.ActivityTelaChatBinding
import com.example.magister.ui.fragments.BuscarFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class TelaChat : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GroupAdapter<GroupieViewHolder>
    private lateinit var binding: ActivityTelaChatBinding

    private var toId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTelaChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = intent.getStringExtra("USERNAME")
        toId = intent.getStringExtra(BuscarFragment.EXTRA_USER_ID)

        supportActionBar?.title = username

        setupDummyData()

        binding.btChat.setOnClickListener {
            performSendMessage()
        }
    }

    companion object {
        const val EXTRA_USER_ID = "EXTRA_USER_ID"
    }

    private fun setupDummyData() {
        recyclerView = binding.recyclerChat
        adapter = GroupAdapter()

        recyclerView.adapter = adapter

        adapter.add(ChatFromItem())
        adapter.add(ChatToItem())
        adapter.add(ChatFromItem())
        adapter.add(ChatToItem())
    }


    private fun performSendMessage() {
        val text = binding.editChat.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val timestamp = System.currentTimeMillis() / 1000

        if (fromId == null) return

        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("Mensagens")

        val chatMessage = ChatMessage(text, timestamp, fromId, toId)
        collectionRef.add(chatMessage)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "A mensagem foi salva: ${documentReference.id}")
                Log.d(TAG, "Id do usuário que recebe a mensagem: $toId")
                Log.d(TAG, "Id do usuário que envia a mensagem: $fromId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro ao salvar a mensagem no banco de dados", e)
            }
    }


}

class ChatFromItem: Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.txt_msg).text = "From Message......."
    }

    override fun getLayout(): Int {
        return R.layout.item_from_message
    }
}

class ChatToItem: Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.txt_msg1).text = "This is the to row text message that is longer"
    }

    override fun getLayout(): Int {
        return R.layout.item_to_message
    }
}
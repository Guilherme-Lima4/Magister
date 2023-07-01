package com.example.magister.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.magister.ChatMessage
import com.example.magister.LatestMessagesAdapter
import com.example.magister.databinding.ActivityConversasBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ConversasFragment : Fragment() {

    private lateinit var binding: ActivityConversasBinding
    private lateinit var adapter: LatestMessagesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityConversasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = LatestMessagesAdapter()
        binding.recyclerviewLatestMessages.adapter = adapter
        binding.recyclerviewLatestMessages.layoutManager = LinearLayoutManager(requireContext())


        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()
        val messagesRef = db.collection("Mensagens")

        val query = messagesRef
            .whereEqualTo("toId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(3)

        query.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Handle the exception
                return@addSnapshotListener
            }

            snapshot?.let { documents ->
                val latestMessages = mutableMapOf<String, ChatMessage>()

                for (document in documents) {
                    val chatMessage = document.toObject(ChatMessage::class.java)
                    if (chatMessage != null && chatMessage.fromId != null) {
                        // Verifica se a mensagem é a mais recente do remetente
                        if (!latestMessages.containsKey(chatMessage.fromId) || chatMessage.timestamp > latestMessages[chatMessage.fromId]?.timestamp ?: 0) {
                            latestMessages[chatMessage.fromId!!] = chatMessage
                        }
                    }
                }

                val latestMessagesList = latestMessages.values.toList()
                adapter.setLatestMessages(latestMessagesList)
            }
        }
    }

    private fun getUsernameFromChatMessage(chatMessage: ChatMessage): String? {
        // Implement your logic to retrieve the username based on the chatMessage
        // You may need to query the Firestore to get the username associated with the chatMessage.fromId
        // For now, let's assume you have a function to get the username
        return chatMessage.fromId?.let { getUsernameFromId(it) }
    }

    private fun getUsernameFromId(userId: String): String {
        // Implement your logic to retrieve the username based on the userId
        // You may need to query the Firestore to get the username associated with the userId
        // For now, let's assume you have a function to get the username
        return "Username"
    }
}
package com.example.magister.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.magister.ChatMessage
import com.example.magister.LatestMessagesAdapter
import com.example.magister.TelaChat
import com.example.magister.databinding.ActivityConversasBinding
//import com.example.magister.ui.activities.ChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.onesignal.OSSubscriptionObserver
import com.onesignal.OSSubscriptionStateChanges
import com.onesignal.OneSignal



class ConversasFragment : Fragment() {

    private lateinit var binding: ActivityConversasBinding
    private lateinit var adapter: LatestMessagesAdapter
    private lateinit var activityContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityContext = context
    }

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

        // Inicializar o OneSignal
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.DEBUG, OneSignal.LOG_LEVEL.NONE)
        OneSignal.initWithContext(requireActivity().applicationContext)
        OneSignal.setAppId("4449f9dd-e81a-44bd-8d55-6cbdf1cfbd44")

        val subscriptionObserver = object : OSSubscriptionObserver {
            override fun onOSSubscriptionChanged(stateChanges: OSSubscriptionStateChanges) {
                if (stateChanges.to.isSubscribed) {
                    // A inscrição foi bem-sucedida
                    val playerId = stateChanges.to.userId
                    Log.d("OneSignal", "Dispositivo inscrito com sucesso. Player ID: $playerId")
                } else if (stateChanges.to.isPushDisabled) {
                    // O usuário desativou as notificações push
                    Log.d("OneSignal", "Notificações push desativadas pelo usuário")
                } else {
                    // Falha na inscrição
                    val errors = stateChanges.to.toJSONObject().optJSONArray("errors")
                    val error = errors?.optString(0)
                    Log.e("OneSignal", "Falha na inscrição: $error")
                }
            }
        }

        OneSignal.addSubscriptionObserver(subscriptionObserver)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("Usuarios").document(currentUser.uid)

            userRef.get().addOnSuccessListener { documentSnapshot ->
                val pushToken = documentSnapshot.getString("pushToken")

                if (pushToken.isNullOrEmpty()) {
                    // O usuário não possui um pushToken, vamos criar um
                    val playerId = OneSignal.getDeviceState()?.userId

                    if (playerId != null) {
                        userRef.update("pushToken", playerId)
                            .addOnSuccessListener {
                                Log.d("ConversasFragment", "Token de registro criado com sucesso")
                                // Chame a função getUsernameFromChatMessage() aqui
                                val chatMessage = ChatMessage()
                                val username = getUsernameFromChatMessage(chatMessage)
                                Log.d("ConversasFragment", "Username: $username")
                            }
                            .addOnFailureListener { exception ->
                                Log.e("ConversasFragment", "Falha ao criar o token de registro", exception)
                            }
                    }
                } else {
                    // O usuário já possui um pushToken
                    Log.d("ConversasFragment", "Usuário já possui um token de registro")
                    // Chame a função getUsernameFromChatMessage() aqui
                    val chatMessage = ChatMessage()
                    val username = getUsernameFromChatMessage(chatMessage)
                    Log.d("ConversasFragment", "Username: $username")
                }
            }.addOnFailureListener { exception ->
                Log.e("ConversasFragment", "Falha ao obter o documento do usuário", exception)
            }
        }

        adapter.setOnItemClickListener(object : LatestMessagesAdapter.OnItemClickListener {
            override fun onItemClick(chatMessage: ChatMessage) {
                val userId = chatMessage.fromId

                val db = FirebaseFirestore.getInstance()
                val usersRef = userId?.let { db.collection("Usuarios").document(it) }

                if (usersRef != null) {
                    usersRef.get().addOnSuccessListener { documentSnapshot ->
                        val username = documentSnapshot.getString("nome")
                        val intent = Intent(requireContext(), TelaChat::class.java)
                        intent.putExtra("USERNAME", username)
                        intent.putExtra(BuscarFragment.EXTRA_USER_ID, userId)
                        startActivity(intent)
                    }.addOnFailureListener { exception ->
                        // Lide com falhas ao obter o nome de usuário
                    }
                }
            }
        })

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()
        val messagesRef = db.collection("Mensagens")

        val query = messagesRef
            .whereEqualTo("toId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        query.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Handle the exception
                return@addSnapshotListener
            }

            snapshot?.let { documents ->
                val latestMessagesMap = mutableMapOf<String, ChatMessage>()

                for (document in documents) {
                    val chatMessage = document.toObject(ChatMessage::class.java)
                    if (chatMessage != null && chatMessage.fromId != null) {
                        // Verifica se a mensagem é a mais recente da conversa
                        if (!latestMessagesMap.containsKey(chatMessage.fromId) || chatMessage.timestamp > latestMessagesMap[chatMessage.fromId]?.timestamp ?: 0) {
                            latestMessagesMap[chatMessage.fromId!!] = chatMessage
                        }
                    }
                }

                val latestMessagesList = latestMessagesMap.values.toList()
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
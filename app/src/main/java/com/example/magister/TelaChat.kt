package com.example.magister

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.magister.databinding.ActivityTelaChatBinding
import com.example.magister.ui.fragments.BuscarFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.onesignal.OneSignal
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class TelaChat : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GroupAdapter<GroupieViewHolder>
    private lateinit var binding: ActivityTelaChatBinding

    private var toId: String? = null
    private var username: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTelaChatBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val intentUsername = intent.getStringExtra("nameUserTarget")
        toId = intent.getStringExtra("EXTRA_USER_ID")

        if (intentUsername != null) {
            // Se o extra "nameUserTarget" estiver presente, use o valor diretamente
            username = intentUsername
        } else {
            // Caso contrário, busque o nome do usuário usando o ID do remetente
            val db = FirebaseFirestore.getInstance()
            val usersRef = toId?.let { db.collection("Usuarios").document(it) }

            if (usersRef != null) {
                usersRef.get().addOnSuccessListener { documentSnapshot ->
                    username = documentSnapshot.getString("nome") ?: "Username"
                    supportActionBar?.title = username
                }.addOnFailureListener { exception ->
                    // Lide com falhas ao obter o nome de usuário
                }
            }
        }

        supportActionBar?.title = username

        listenForMessages()
        setupAdapter()

        binding.btChat.setOnClickListener {
            performSendMessage()
        }

        // A função listenForMessages() será chamada automaticamente quando uma nova mensagem for adicionada ao Firestore
    }

    companion object {
        const val EXTRA_USER_ID = "EXTRA_USER_ID"
    }

    private fun setupAdapter() {
        recyclerView = binding.recyclerChat
        adapter = GroupAdapter()

        recyclerView.adapter = adapter

        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun performSendMessage() {
        val text = binding.editChat.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val timestamp = System.currentTimeMillis() / 1000

        if (fromId == null) return

        val db = FirebaseFirestore.getInstance()

        val conversationId = generateConversationId(fromId, toId)

        val chatMessage = ChatMessage(text, timestamp, fromId, toId, conversationId)

        val messagesRef = db.collection("Mensagens")
        messagesRef.add(chatMessage)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "A mensagem foi salva: ${documentReference.id}")

                // Obter o pushToken do usuário destinatário
                getUserPushToken(toId) { pushToken ->
                    // Enviar a notificação para o usuário destinatário
                    sendNotificationToUser(pushToken)
                    binding.editChat.text = null
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro ao salvar a mensagem no banco de dados", e)
            }
    }

    private fun getUserPushToken(userId: String?, completion: (String) -> Unit) {
        if (userId == null) return

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("Usuarios").document(userId)
        userRef.get()
            .addOnSuccessListener { documentSnapshot ->
                val pushToken = documentSnapshot.getString("pushToken") ?: ""
                completion(pushToken)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao obter o pushToken do usuário", exception)
                completion("")
            }
    }

    private fun sendNotificationToUser(pushToken: String) {
        // Verificar se o pushToken existe
        if (pushToken.isNotEmpty()) {
            // Configurar os parâmetros da notificação
            val notificationContent = JSONObject()
            notificationContent.put("en", "You have a new message") // Mensagem em inglês
            notificationContent.put("pt", "Você recebeu uma nova mensagem") // Mensagem em português

            val notificationData = JSONObject()
            notificationData.put("userId", toId)

            val notification = JSONObject()
            notification.put("contents", notificationContent)
            notification.put("data", notificationData)
            notification.put("include_player_ids", JSONArray().put(pushToken))

            // Enviar a notificação para o OneSignal
            OneSignal.postNotification(notification, object : OneSignal.PostNotificationResponseHandler {
                override fun onSuccess(response: JSONObject?) {
                    Log.d(TAG, "Notificação enviada com sucesso para o usuário: $toId")
                }

                override fun onFailure(response: JSONObject?) {
                    Log.e(TAG, "Erro ao enviar notificação para o usuário: $toId")
                }
            })
        } else {
            Log.e(TAG, "Push token do usuário não encontrado")
        }
    }

    private fun listenForMessages() {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("Mensagens")

        val conversationId = generateConversationId(FirebaseAuth.getInstance().uid, toId)

        collectionRef
            .whereEqualTo("conversationId", conversationId)
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e(TAG, "Erro ao ler as mensagens: ", exception)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d(TAG, "Total de mensagens: ${snapshot.documents.size}")
                }

                val messages = mutableListOf<Item<GroupieViewHolder>>() // Lista temporária para armazenar as mensagens

                for (document in snapshot!!.documents) {
                    val chatMessage = document.toObject(ChatMessage::class.java)

                    if (chatMessage != null) {
                        chatMessage.text?.let { Log.d(TAG, "Mensagem: $it") }

                        if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                            messages.add(ChatToItem(chatMessage.text ?: ""))
                        } else {
                            messages.add(ChatFromItem(chatMessage.text ?: ""))
                        }
                    }
                }

                // Limpar o adaptador antes de adicionar as novas mensagens
                adapter.clear()

                // Adicionar as novas mensagens ao adaptador
                adapter.update(messages)

                // Rolar automaticamente para a última mensagem
                if (adapter.itemCount >= 2) {
                    recyclerView.scrollToPosition(adapter.itemCount - 1)
                }
            }
    }

    private fun generateConversationId(userId1: String?, userId2: String?): String {
        // Ordenar os IDs dos usuários em ordem alfabética
        val sortedUserIds = listOfNotNull(userId1, userId2).sortedWith(compareBy { it })

        // Combinação dos IDs de usuário para formar o ID da conversa
        return "${sortedUserIds[0]}_${sortedUserIds[1]}"
    }
}

class ChatFromItem(private val messageText: String) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.txt_msg).text = messageText
    }

    override fun getLayout(): Int {
        return R.layout.item_from_message
    }
}

class ChatToItem(private val messageText: String) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.txt_msg1).text = messageText
    }

    override fun getLayout(): Int {
        return R.layout.item_to_message
    }
}
package com.example.magister

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.magister.ChatMessage
import com.example.magister.databinding.LatestMessageRowBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class LatestMessagesAdapter : RecyclerView.Adapter<LatestMessagesAdapter.LatestMessageViewHolder>() {

    private val latestMessages = mutableListOf<ChatMessage>()
    private val userIdToUsernameMap = mutableMapOf<String, String>() // Mapa para armazenar os nomes de usuário com base nos IDs
    private val userIdToProfileImageUrlMap = mutableMapOf<String, String>() // Mapa para armazenar as URLs das fotos de perfil com base nos IDs
    private var onItemClickListener: OnItemClickListener? = null

    fun setLatestMessages(messages: List<ChatMessage>) {
        latestMessages.clear()
        latestMessages.addAll(messages)
        notifyDataSetChanged()

        // Chame a função para buscar os nomes de usuário e as fotos de perfil com base nos IDs
        fetchUsernamesAndProfileImagesFromIds()
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    private fun fetchUsernamesAndProfileImagesFromIds() {
        val userIds = latestMessages.mapNotNull { it.fromId }

        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("Usuarios")

        usersRef.whereIn("id", userIds)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val userId = document.id
                    val username = document.getString("nome")
                    val profileImageUrl = document.getString("fotoUrl")
                    if (userId != null && username != null && profileImageUrl != null) {
                        userIdToUsernameMap[userId] = username
                        userIdToProfileImageUrlMap[userId] = profileImageUrl
                    }
                }
                notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Lidar com falhas ao buscar os nomes de usuário e as fotos de perfil
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LatestMessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LatestMessageRowBinding.inflate(inflater, parent, false)
        return LatestMessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LatestMessageViewHolder, position: Int) {
        holder.bind(latestMessages[position])
    }

    override fun getItemCount(): Int {
        return latestMessages.size
    }

    inner class LatestMessageViewHolder(private val binding: LatestMessageRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val chatMessage = latestMessages[position]
                    onItemClickListener?.onItemClick(chatMessage)
                }
            }
        }

        fun bind(chatMessage: ChatMessage) {
            val username = userIdToUsernameMap[chatMessage.fromId]
            val profileImageUrl = userIdToProfileImageUrlMap[chatMessage.fromId]

            binding.username.text = username ?: ""
            binding.latestMessage.text = chatMessage.text ?: ""

            if (profileImageUrl != null) {
                Picasso.get().load(profileImageUrl).into(binding.profileImage)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(chatMessage: ChatMessage)
    }
}

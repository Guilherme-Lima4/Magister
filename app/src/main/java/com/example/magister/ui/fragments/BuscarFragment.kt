package com.example.magister.ui.fragments

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.magister.R
import com.example.magister.TelaChat
import com.example.magister.TelaContatos.User
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.example.magister.TelaContatos
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item


class BuscarFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GroupieAdapter

    companion object {
        const val EXTRA_USER_ID = "EXTRA_USER_ID"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_buscar, container, false)

        recyclerView = view.findViewById(R.id.recycler)
        adapter = GroupieAdapter()


        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter



        adapter.setOnItemClickListener { item, _ ->
            val intent = Intent(view.context, TelaChat::class.java)
            val userItem = item as UserItem
            intent.putExtra("Usuarios", userItem.user)
            intent.putExtra("USERNAME", userItem.user?.nome)
            intent.putExtra(TelaChat.EXTRA_USER_ID, userItem.uid)
            startActivity(intent)
        }

        fetchUsers()

        return view
    }

    private fun fetchUsers() {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("Usuarios")

        docRef.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val fotoUrl = document.getString("fotoUrl")
                    val nomeUsuario = document.getString("nome")
                    val uid = document.id // Obtenha o ID do usuário diretamente do documento

                    if (fotoUrl != null && nomeUsuario != null) {
                        val user = User(fotoUrl, nomeUsuario, uid) // Adicione o ID do usuário ao objeto User
                        adapter.add(UserItem(user, uid))
                    }
                }
            }
            .addOnFailureListener { exception ->
                // Lidar com falhas ao buscar os usuários
            }
    }

    inner class UserItem(val user: User?, val uid: String?) : Item<GroupieViewHolder>() {

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val imgPhoto = viewHolder.itemView.findViewById<ImageView>(R.id.imageView)
            val txtUserName = viewHolder.itemView.findViewById<TextView>(R.id.textView)

            txtUserName.text = user?.nome

            Picasso.get().load(user?.fotoUrl).into(imgPhoto)
        }

        override fun getLayout(): Int {
            return R.layout.item_user
        }
    }
}

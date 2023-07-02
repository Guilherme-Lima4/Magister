package com.example.magister.ui.fragments

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.magister.BuscarEntity
import com.example.magister.R
import com.example.magister.TelaChat
import com.example.magister.TelaContatos.User
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.example.magister.TelaContatos
import com.example.magister.databinding.ActivityBuscarBinding
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item


class BuscarFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GroupieAdapter
    private lateinit var binding: ActivityBuscarBinding
    private var buscarList: List<BuscarEntity> = emptyList()
    private lateinit var buscarAdapter: BuscarAdapter

    companion object {
        const val EXTRA_USER_ID = "EXTRA_USER_ID"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ActivityBuscarBinding.inflate(inflater, container, false)
        val view = binding.root

        recyclerView = view.findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        //recyclerView.layoutManager = LinearLayoutManager(requireContext())
        //recyclerView.adapter = adapter



        buscarAdapter = BuscarAdapter(buscarList) { buscarEntity, position ->
            val intent = Intent(requireContext(), TelaChat::class.java)
            intent.putExtra("EXTRA_USER_ID", buscarEntity.idTargetUser)
            intent.putExtra("nameUserTarget", buscarEntity.nameUserTarget)
            intent.putExtra("imageUserTarget", buscarEntity.imageUserTarget)

            startActivity(intent)
        }

        recyclerView.adapter = buscarAdapter

        initSearchView()
        fetchUsers()

        return view
    }

    private fun initSearchView() {

        binding.menuBuscar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                if (query.isNotBlank()) {
                    searchUsers(query, buscarList)
                } else {
                    fetchUsers()
                }
                return true
            }
        })
    }

    private fun searchUsers(query: String, buscarEntityList: List<BuscarEntity>) {
        val filteredList = buscarEntityList.filter { it.nameUserTarget!!.contains(query, ignoreCase = true) }

        if (filteredList.isNotEmpty()) {
            buscarAdapter.updateBuscarList(filteredList)
            binding.txtInfo.visibility = View.GONE
        } else {
            buscarAdapter.clearBuscarList()
            binding.txtInfo.text = "Nenhum usuário encontrado."
            binding.txtInfo.visibility = View.VISIBLE
        }
    }

    class BuscarAdapter(
        private val buscarEntityList: List<BuscarEntity>,
        val userSelected: (BuscarEntity, Int) -> Unit?
    ) : RecyclerView.Adapter<BuscarAdapter.MyViewHolder>() {

        private var buscarList = buscarEntityList.toMutableList()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView =
                LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val buscar = buscarList[position]

            val imgFoto = holder.itemView.findViewById<ImageView>(R.id.imageView)
            Picasso.get().load(buscar.imageUserTarget).into(imgFoto)

            val txtNome = holder.itemView.findViewById<TextView>(R.id.textView)
            txtNome.text = buscar.nameUserTarget

            // Configurar o clique do item
            holder.itemView.setOnClickListener {
                userSelected(buscar, position)
            }
        }

        override fun getItemCount() = buscarList.size
        fun search(query: String): Boolean {
            buscarList.clear()

            buscarList.addAll(buscarEntityList.filter { it.nameUserTarget!!.contains(query, true) })
            notifyDataSetChanged()

            return buscarList.isEmpty()
        }

        fun updateBuscarList(newList: List<BuscarEntity>) {
            buscarList.clear()
            buscarList.addAll(newList)
            notifyDataSetChanged()
        }

        fun clearBuscarList() {
            buscarList.clear()
            notifyDataSetChanged()
        }

        class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            init {
                itemView.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val adapter = itemView.parent.parent as BuscarAdapter
                        val buscarEntity = adapter.buscarEntityList[position]
                        adapter.userSelected(buscarEntity, position)
                    }
                }
            }
        }
    }

    fun fetchUsers() {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("Usuarios")

        docRef.get()
            .addOnSuccessListener { querySnapshot ->
                val users = mutableListOf<BuscarEntity>()
                for (document in querySnapshot) {
                    val fotoUrl = document.getString("fotoUrl")
                    val nomeUsuario = document.getString("nome")

                    if (fotoUrl != null && nomeUsuario != null) {
                        val user = BuscarEntity(
                            idSourceUser = null,
                            idTargetUser = document.id, // Use o ID do documento como idTargetUser
                            nameUserTarget = nomeUsuario,
                            imageUserTarget = fotoUrl,
                            LastMessage = null
                        )
                        users.add(user)
                    }
                }
                buscarList = users
                buscarAdapter.updateBuscarList(users)
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
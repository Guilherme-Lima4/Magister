package com.example.magister

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.magister.databinding.ActivityTelaContatosBinding
import com.google.firebase.firestore.FirebaseFirestore
import androidx.recyclerview.widget.RecyclerView.*
import com.google.android.gms.common.util.UidVerifier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.auth.User
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.get
import com.squareup.picasso.RequestCreator
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import java.lang.reflect.Array.set


class TelaContatos : AppCompatActivity() {
    private lateinit var binding: ActivityTelaContatosBinding

    private val db = FirebaseFirestore.getInstance()
    val docRef = db.collection("Usuarios")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_contatos)
        binding = ActivityTelaContatosBinding.inflate(layoutInflater)

        //fetchUsers()

        val rv = findViewById<RecyclerView>(R.id.recicler)
        val adapter = GroupieAdapter()
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(this)

        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, TelaChat::class.java)
            //val userItem = item as UserItem
           // intent.putExtra("Usuarios", userItem.user)
            //intent.putExtra(USER_KEY, userItem.user?.uid)
            startActivity(intent)
        }

        docRef.get()
            .addOnSuccessListener { querySnapshot ->   //método para recuperar os dados do banco de dados
                for (document in querySnapshot) {
                    val fotoUrl = document.getString("fotoUrl")
                    val nomeUsuario = document.getString("nome")
                    val escolaUser = document.getString("escola")
                    val materia1 = document.getString("materia 1")
                    val materia2 = document.getString("materia 2")
                    val materia3 = document.getString("materia 3")

                    if (fotoUrl != null && nomeUsuario != null && escolaUser != null && materia1 != null && materia2 != null && materia3 != null) {
                        val user = User(fotoUrl, nomeUsuario, escolaUser, materia1, materia2, materia3)
                        //adapter.add(UserItem(user))
                    }
                }
            }
            .addOnFailureListener { exception ->
            }

    }

    /***private fun fetchUsers() {
        val listener = meuDocumentoRef.addSnapshotListener { snapshot, exception ->
            val rv = findViewById<RecyclerView>(R.id.recicler)
            val adapter = GroupieAdapter()
            rv.adapter = adapter
            rv.layoutManager = LinearLayoutManager(this)
            if (exception != null) {
                Log.e(TAG, "Teste", exception)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                adapter.add(UserItem(user))

            } else {
            // O documento não existe
            }
        }
    }***/



    class User(var fotoUrl: String = "", var nome: String = "", var uid: String = "", var escola: String = "", var materia1: String = "", var materia2: String = "", var materia3: String = "", var token: String = "", var online: Boolean = false): Parcelable{ //método para poder acessar os atributos do usuário
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readInt() != 0
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(fotoUrl)
            parcel.writeString(nome)
            parcel.writeString(uid)
            parcel.writeString(escola)
            parcel.writeString(materia1)
            parcel.writeString(materia2)
            parcel.writeString(materia3)
            parcel.writeString(token)
            parcel.writeInt(if (online) 1 else 0)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<User> {
            val fotoUrl: String
                get() = Unit.toString()
            val nome: String
                get() = Unit.toString()
            val uid: String
                get() = Unit.toString()
            val escola: String
                get() = Unit.toString()
            val materia1: String
                get() = Unit.toString()
            val materia2: String
                get() = Unit.toString()
            val materia3: String
                get() = Unit.toString()
            val token: String
                get() = Unit.toString()
            val online: String
                get() = Unit.toString()

            override fun createFromParcel(parcel: Parcel): User {
                return User(parcel)
            }

            override fun newArray(size: Int): Array<User?> {
                return arrayOfNulls(size)
            }
        }
    }
}


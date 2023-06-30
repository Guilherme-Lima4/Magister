package com.example.magister.ui.fragments

import android.R
import android.app.Activity.RESULT_OK
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.graphics.Color

import android.graphics.Picture
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.squareup.picasso.Picasso
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.magister.FormLogin
import com.example.magister.databinding.ActivityPerfilBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID
import kotlin.math.log

class PerfilFragment : Fragment() {
    private lateinit var binding: ActivityPerfilBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityPerfilBinding.inflate(inflater, container, false)
        val view = binding.root

        val spinner1 = binding.spinner1
        val spinner2 = binding.spinner2
        val spinner3 = binding.spinner3

        spinner1.adapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_dropdown_item,
            listOf(
                "Selecione...",
                "Estrutura de dados",
                "Programação Web",
                "POO",
                "Banco de dados",
                "Engenharia de Softwares"
            )
        )

        spinner2.adapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_dropdown_item,
            listOf(
                "Selecione...",
                "Estrutura de dados",
                "Programação Web",
                "POO",
                "Banco de dados",
                "Engenharia de Softwares"
            )
        )

        spinner3.adapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_dropdown_item,
            listOf(
                "Selecione...",
                "Estrutura de dados",
                "Programação Web",
                "POO",
                "Banco de dados",
                "Engenharia de Softwares"
            )
        )

        binding.btSair.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val telaLogin = Intent(requireContext(), FormLogin::class.java)
            startActivity(telaLogin)
            requireActivity().finish()
        }



        binding.btEscolherft.setOnClickListener {
            SelecionarFt()
        }

        binding.btSalvar.setOnClickListener {
            //Esse aqui é um metodo para esconder o teclado quando o botão for apertado
            val InputMethodManager =
                requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            InputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)

            //Apartir daqui começa os códigos para atualizar os dados
            val nome = binding.editNome.text.toString()
            val escola = binding.editEscola.text.toString()
            val materia1 = binding.spinner1.selectedItem as String
            val materia2 = binding.spinner2.selectedItem as String
            val materia3 = binding.spinner3.selectedItem as String

            val usuarios = hashMapOf<String, Any?>()
            usuarios.put("nome", nome)
            usuarios.put("escola", escola)
            usuarios.put("materia 1", materia1)
            usuarios.put("materia 2", materia2)
            usuarios.put("materia 3", materia3)

            val usuarioID = FirebaseAuth.getInstance().currentUser!!.uid
            if (nome.isNotEmpty()) {
                db.collection("Usuarios").document(usuarioID)
                    .update(usuarios).addOnCompleteListener {
                        Log.d("db_update", "Sucesso ao atualizar os dados")
                        val snackbar =
                            Snackbar.make(
                                view,
                                "Dados atualizados com sucesso!",
                                Snackbar.LENGTH_LONG
                            )
                        snackbar.setBackgroundTint(Color.parseColor("#562D8B"))
                        snackbar.setTextColor(Color.WHITE)
                        snackbar.show()
                    }
            } else {
                val snackbar = Snackbar.make(view,"Insira o nome!",Snackbar.LENGTH_LONG)
                snackbar.setBackgroundTint(Color.parseColor("#562D8B"))
                snackbar.setTextColor(Color.WHITE)
                snackbar.show()
                binding.editNome.error = "Insira o nome!"
            }
        }
        return view
    }

    override fun onStart() {  // Esse método ele recupera todos os dados do db e a foto de perfil
        super.onStart()

        val email = FirebaseAuth.getInstance().currentUser?.email
        val usuarioID = FirebaseAuth.getInstance().currentUser?.uid

        if (usuarioID != null) {
            db.collection("Usuarios").document(usuarioID)
                .addSnapshotListener { value, error ->
                    if (value != null) {
                        binding.editNome.setText(value.getString("nome"))
                        binding.textEmailUsuario.setText(email)
                        binding.editEscola.setText(value.getString("escola"))

                        val spinner1 = binding.spinner1
                        val spinner2 = binding.spinner2
                        val spinner3 = binding.spinner3

                        val valueFromFirebase1 = value.getString("materia 1")
                        val valueFromFirebase2 = value.getString("materia 2")
                        val valueFromFirebase3 = value.getString("materia 3")

                        val adapter1 = spinner1.adapter
                        val adapter2 = spinner2.adapter
                        val adapter3 = spinner3.adapter

                        val foto = binding.containerUser
                        val fotoPerfil = FirebaseFirestore.getInstance().collection("Usuarios").document(usuarioID)

                        fotoPerfil.get().addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                val imageUser = binding.imgUser
                                val imageUrl = documentSnapshot.getString("fotoUrl")
                                Picasso.get().load(imageUrl).into(foto)
                                imageUser.alpha = 0f
                                Log.d("img", "imagem recuperada com sucesso")
                            } else {
                                Log.d("img", "erro ao recuperar a imagem")
                            }
                        }

                        for (i in 0 until adapter1.count) {
                            val item = adapter1.getItem(i).toString()
                            if (item == valueFromFirebase1) {
                                spinner1.setSelection(i)
                                break
                            }
                        }

                        for (i in 0 until adapter2.count) {
                            val item = adapter2.getItem(i).toString()
                            if (item == valueFromFirebase2) {
                                spinner2.setSelection(i)
                                break
                            }
                        }

                        for (i in 0 until adapter3.count) {
                            val item = adapter3.getItem(i).toString()
                            if (item == valueFromFirebase3) {
                                spinner3.setSelection(i)
                                break
                            }
                        }
                    }
                }
        }
    }

//Esses três métodos abaixo são para colocar a foto de perfil, mas antes precisa de uma permissão no Manifest
    companion object {
        private const val REQUEST_CODE_GALLERY = 0
    }

    private fun SelecionarFt() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null) {
            val selectedImage: Uri? = data.data

            if (selectedImage != null) {
                val imageView = binding.containerUser
                val imageUser = binding.imgUser
                Glide.with(this)
                    .load(selectedImage)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageView)
                imageUser.alpha = 0f
                val storage = FirebaseStorage.getInstance()
                val filename = UUID.randomUUID().toString()
                val storageRef: StorageReference = storage.reference.child("/images/" + filename)
                storageRef.putFile(selectedImage)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener {uri ->
                            val usuarioID = FirebaseAuth.getInstance().currentUser!!.uid
                            val fotoUrl = uri.toString()
                            val usuarios = hashMapOf<String, Any?>()
                            usuarios.put("fotoUrl", fotoUrl)

                            Log.d("img", "imagem salva com sucesso")

                            db.collection("Usuarios").document(usuarioID)
                                .update(usuarios).addOnCompleteListener {
                                    Log.d("img", "imagem puxada com sucesso")
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("img_error", "imagem não foi salva")
                    }
            }
        }
    }
}

package com.example.magister.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.content.ContextCompat
import com.example.magister.R
import com.example.magister.TelaContatos
import com.example.magister.databinding.ActivityConversasBinding
import com.google.firebase.auth.FirebaseAuth

class ConversasFragment : Fragment() {

    private lateinit var binding: ActivityConversasBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = ActivityConversasBinding.inflate(inflater, container, false)
        val view = binding.root

        //val btContatos = binding.contatos
       // val color = ContextCompat.getColorStateList(requireContext(), R.color.white)
        //btContatos.imageTintList = color

       // binding.contatos.setOnClickListener {
          //  val telaContatos = Intent(requireContext(), TelaContatos::class.java)
          //  startActivity(telaContatos)
         //   requireActivity().finish()
        //}

        return view
    }

}
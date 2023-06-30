package com.example.magister

import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import com.example.magister.databinding.ActivityTelaPrincipalBinding
import com.example.magister.ui.fragments.PerfilFragment
import com.example.magister.ui.fragments.SectionPagerAdapter

class TelaPrincipal : AppCompatActivity() {

    private lateinit var binding: ActivityTelaPrincipalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTelaPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val viewPager: ViewPager = binding.viewPager
        val tabLayout:  TabLayout = binding.tabs

        //val fab: FloatingActionButton = binding.fab

        //fab.setOnClickListener { view ->
        //    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        //        .setAction("Action", null).show()
        //}
        val pagerAdapter = SectionPagerAdapter(supportFragmentManager)
        viewPager.adapter = pagerAdapter
        tabLayout.setupWithViewPager(viewPager)

    }

    //configuração para sair do app quando apertar o botão voltar
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}
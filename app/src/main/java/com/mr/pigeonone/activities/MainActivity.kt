package com.mr.pigeonone.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import androidx.databinding.DataBindingUtil
import com.google.android.material.tabs.TabLayoutMediator
import com.mr.pigeonone.R
import com.mr.pigeonone.adapters.ScreenSlidePagerAdapter
import com.mr.pigeonone.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this, R.layout.activity_main)

        setSupportActionBar(binding.toolbar)
        binding.viewPager.adapter = ScreenSlidePagerAdapter(this)
        TabLayoutMediator(binding.tabs, binding.viewPager
        ) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "CHATS"
                }
                1 -> {
                    tab.text = "PEOPLE"
                }
            }
        }.attach()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
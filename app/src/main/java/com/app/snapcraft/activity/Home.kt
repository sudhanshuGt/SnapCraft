package com.app.snapcraft.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.app.snapcraft.R
import com.app.snapcraft.databinding.ActivityHomeBinding

class Home : AppCompatActivity() {

    private lateinit var binding : ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


     }
}
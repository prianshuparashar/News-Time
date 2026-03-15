package com.prianshuparashar.newstime.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.prianshuparashar.newstime.NewsApplication
import com.prianshuparashar.newstime.R
import com.prianshuparashar.newstime.databinding.ActivityMainBinding
import com.prianshuparashar.newstime.di.module.ActivityModule

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inject Dependencies
        injectDependencies()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun injectDependencies() {
        (application as NewsApplication)
            .applicationComponent
            .getActivityComponentBuilder()
            .activityModule(ActivityModule(this))
            .build()
            .inject(this)
    }
}
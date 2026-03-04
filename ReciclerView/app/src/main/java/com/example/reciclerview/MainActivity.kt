package com.example.reciclerview

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private val viewModel: ColorViewModel by viewModels()
    private lateinit var adapter: ColorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView: RecyclerView = findViewById(R.id.rvColors)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // Animación por defecto del RecyclerView para inserciones/borrados
        recyclerView.itemAnimator = DefaultItemAnimator()

        adapter = ColorAdapter(
            colors = emptyList(),
            onItemClick = { color ->
                viewModel.toggleInversion(color.id, true)
            },
            onItemLongClick = { color ->
                viewModel.toggleInversion(color.id, false)
            }
        )
        recyclerView.adapter = adapter

        viewModel.colors.observe(this) { colors ->
            // Enviamos una copia de los elementos para que DiffUtil funcione correctamente
            adapter.updateData(colors.map { it.copy() })
            viewModel.colors.observe(this) { colors ->
                adapter.updateData(colors.toList()) // .toList() crea una copia necesaria para DiffUtil
            }
        }

        findViewById<Button>(R.id.btnAdd).setOnClickListener {
            viewModel.addColor()
        }

        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            viewModel.deleteLastColor()
        }
    }
}

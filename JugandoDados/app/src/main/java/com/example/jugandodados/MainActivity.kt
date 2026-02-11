package com.example.jugandodados

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.jugandodados.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var saldo = 100
    private var apuestaActual = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvSaldo.text = saldo.toString()
        binding.toggleGroup.check(R.id.btnParImpar)
        updateSpinnerOptions(R.id.btnParImpar)

        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) updateSpinnerOptions(checkedId)
        }

        binding.btnLanzar.setOnClickListener { lanzarDados() }
    }

    private fun updateSpinnerOptions(checkedId: Int) {
        val options = if (checkedId == R.id.btnParImpar) listOf("PAR", "IMPAR")
        else listOf("Mayor o igual que 7", "Menor o igual que 7")
        binding.spinnerOpciones.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
    }

    private fun lanzarDados() {
        val apuestaStr = binding.etApuesta.text.toString()
        if (apuestaStr.isEmpty()) return
        val apuesta = apuestaStr.toInt()
        if (apuesta <= 0 || apuesta > saldo) return

        apuestaActual = apuesta
        binding.btnLanzar.isEnabled = false
        binding.ivDiceGif.visibility = View.VISIBLE
        
        // Reactivamos Glide para cargar el GIF
        Glide.with(this).asGif().load(R.drawable.dados_movimiento).into(binding.ivDiceGif)

        // Devolvemos el retardo original de 3 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            binding.ivDiceGif.visibility = View.GONE
            binding.layoutResultados.visibility = View.VISIBLE
            val d1 = Random.nextInt(1, 7)
            val d2 = Random.nextInt(1, 7)
            binding.tvDado1.text = d1.toString()
            binding.tvDado2.text = d2.toString()
            
            val gano = calcularSiGano(d1 + d2)
            if (gano) {
                saldo += apuestaActual
                binding.ivEstadoPartida.setImageResource(R.drawable.descarga)
            } else {
                saldo -= apuestaActual
                binding.ivEstadoPartida.setImageResource(R.drawable.perder)
            }
            binding.tvSaldo.text = saldo.toString()
            binding.ivEstadoPartida.visibility = View.VISIBLE
            
            Handler(Looper.getMainLooper()).postDelayed({
                if (saldo <= 0) {
                    binding.ivEstadoPartida.setImageResource(R.drawable.bancarrota)
                    Snackbar.make(binding.root, "BANCARROTA", Snackbar.LENGTH_INDEFINITE).setAction("SALIR") { finish() }.show()
                } else {
                    Snackbar.make(binding.root, "¿Seguir?", Snackbar.LENGTH_LONG).addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(b: Snackbar?, e: Int) { binding.btnLanzar.isEnabled = true }
                    }).show()
                }
            }, 1000)
        }, 3000)
    }

    private fun calcularSiGano(suma: Int): Boolean {
        val opcion = binding.spinnerOpciones.selectedItem.toString()
        return if (binding.toggleGroup.checkedButtonId == R.id.btnParImpar) {
            if (opcion == "PAR") suma % 2 == 0 else suma % 2 != 0
        } else {
            if (opcion == "Mayor o igual que 7") suma >= 7 else suma < 7
        }
    }
}
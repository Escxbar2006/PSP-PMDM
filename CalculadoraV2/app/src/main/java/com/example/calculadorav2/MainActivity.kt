package com.example.calculadorav2

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.calculadorav2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.display.observe(this) { text ->
            binding.tvDisplay.text = text
        }

        viewModel.history.observe(this) { text ->
            binding.tvHistory.text = text
        }

        viewModel.warning.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.resetWarning()
            }
        }
    }

    private fun setupListeners() {
        val digitButtons = listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4,
            binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9
        )

        digitButtons.forEach { button ->
            button.setOnClickListener {
                viewModel.onDigitClicked(button.text.toString())
            }
        }

        binding.btnAdd.setOnClickListener { viewModel.onOperatorClicked("+") }
        binding.btnSub.setOnClickListener { viewModel.onOperatorClicked("-") }
        binding.btnMult.setOnClickListener { viewModel.onOperatorClicked("*") }
        binding.btnDiv.setOnClickListener { viewModel.onOperatorClicked("/") }

        binding.btnEquals.setOnClickListener { viewModel.onEqualsClicked() }
        binding.btnClear.setOnClickListener { viewModel.onClearClicked() }
    }
}

package com.example.mymoneynotes

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymoneynotes.adapter.TransactionAdapter
import com.example.mymoneynotes.model.Transaction
import com.example.mymoneynotes.viewmodel.TransactionViewModel
//import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.PieChart
//import com.github.mikephil.charting.components.XAxis
//import com.github.mikephil.charting.data.BarData
//import com.github.mikephil.charting.data.BarDataSet
//import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
//import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.NumberFormat
import java.util.Locale
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import android.content.SharedPreferences
import android.content.Context
import android.graphics.Color
import com.github.mikephil.charting.components.LegendEntry


class MainActivity : AppCompatActivity() {

    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var adapter: TransactionAdapter
    private lateinit var tvIncome: TextView
    private lateinit var tvExpense: TextView
    private lateinit var tvBalance: TextView
    private lateinit var pieChart: PieChart
    //private lateinit var categoryChart: HorizontalBarChart
    private lateinit var themeSwitch: SwitchCompat
    private lateinit var sharedPreferences: SharedPreferences

    private val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Get shared preferences to save theme selection
        sharedPreferences = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)

        // Apply theme before setting content view
        applyTheme(getThemeFromPreferences())

        setContentView(R.layout.activity_main)

        // Initialize theme switch
        themeSwitch = findViewById(R.id.themeSwitch)

        // Set the switch state based on saved preference
        themeSwitch.isChecked = getThemeFromPreferences() == AppCompatDelegate.MODE_NIGHT_YES

        // Add listener to the switch
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                saveThemeToPreferences(AppCompatDelegate.MODE_NIGHT_YES)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                saveThemeToPreferences(AppCompatDelegate.MODE_NIGHT_NO)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
        // Inisialisasi komponen UI
        tvIncome = findViewById(R.id.tvIncome)
        tvExpense = findViewById(R.id.tvExpense)
        tvBalance = findViewById(R.id.tvBalance)
        pieChart = findViewById(R.id.pieChart)

        // Setup RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.rvTransactions)
        adapter = TransactionAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Setup ViewModel
        transactionViewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

        // Observasi data transaksi
        transactionViewModel.allTransactions.observe(this) { transactions ->
            adapter.submitList(transactions)
        }

        // Observasi total pemasukan dan pengeluaran
        observeFinancialData()

        // Setup tombol tambah transaksi
        val fab = findViewById<FloatingActionButton>(R.id.fabAddTransaction)
        fab.setOnClickListener {
            showAddTransactionDialog()
        }

        // Setup tombol filter
        setupFilterButtons()

        // Setup grafik
        setupPieChart()

        // Tambahkan listener untuk item click
        adapter.setOnItemClickListener { transaction ->
            showEditDeleteDialog(transaction)
        }

    }


    // Tambahkan di MainActivity.kt setelah onCreate
    private fun setupFilterButtons() {
        val btnAllTransactions = findViewById<Button>(R.id.btnAllTransactions)
        val btnIncome = findViewById<Button>(R.id.btnIncome)
        val btnExpense = findViewById<Button>(R.id.btnExpense)

        btnAllTransactions.setOnClickListener {
            transactionViewModel.allTransactions.observe(this) { transactions ->
                adapter.submitList(transactions)
            }
            btnAllTransactions.isSelected = true
            btnIncome.isSelected = false
            btnExpense.isSelected = false
        }

        btnIncome.setOnClickListener {
            transactionViewModel.getTransactionsByType("INCOME").observe(this) { transactions ->
                adapter.submitList(transactions)
            }
            btnAllTransactions.isSelected = false
            btnIncome.isSelected = true
            btnExpense.isSelected = false
        }

        btnExpense.setOnClickListener {
            transactionViewModel.getTransactionsByType("EXPENSE").observe(this) { transactions ->
                adapter.submitList(transactions)
            }
            btnAllTransactions.isSelected = false
            btnIncome.isSelected = false
            btnExpense.isSelected = true
        }
    }

    private fun observeFinancialData() {
        var income = 0.0
        var expense = 0.0

        transactionViewModel.totalIncome.observe(this) { totalIncome ->
            income = totalIncome ?: 0.0
            tvIncome.text = formatter.format(income)
            updateBalance(income, expense)
            updatePieChart(income, expense)
        }

        transactionViewModel.totalExpense.observe(this) { totalExpense ->
            expense = totalExpense ?: 0.0
            tvExpense.text = formatter.format(expense)
            updateBalance(income, expense)
            updatePieChart(income, expense)
        }
    }

    private fun updateBalance(income: Double, expense: Double) {
        val balance = income - expense
        tvBalance.text = formatter.format(balance)
    }

    private fun setupPieChart() {
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.setDrawEntryLabels(false)
        pieChart.legend.isEnabled = true
        pieChart.setHoleColor(android.R.color.black)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.setEntryLabelColor(android.R.color.black)
        pieChart.centerText = "Distribusi"

        // Manual set legend entries
        val legend = pieChart.legend
        val entries = ArrayList<LegendEntry>()

        entries.add(
            LegendEntry().apply {
                label = "Pemasukan"
                formColor = Color.parseColor("#4CAF50") // hijau
            }
        )
        entries.add(
            LegendEntry().apply {
                label = "Pengeluaran"
                formColor = Color.parseColor("#F44336") // merah
            }
        )
        legend.setCustom(entries) // Gunakan custom legend
    }

    private fun updatePieChart(income: Double, expense: Double) {
        val entries = ArrayList<PieEntry>()

        if (income > 0) {
            entries.add(PieEntry(income.toFloat(), "Pemasukan"))
        }

        if (expense > 0) {
            entries.add(PieEntry(expense.toFloat(), "Pengeluaran"))
        }

        val dataSet = PieDataSet(entries, "Keuangan")
        dataSet.colors = listOf(
            ColorTemplate.rgb("#4CAF50"),  // Hijau untuk pemasukan
            ColorTemplate.rgb("#F44336")   // Merah untuk pengeluaran
        )

        val data = PieData(dataSet)
        data.setValueTextSize(12f)
        pieChart.data = data
        pieChart.invalidate()
    }

    private fun showAddTransactionDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_transaction, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        val rgTransactionType = dialogView.findViewById<RadioGroup>(R.id.rgTransactionType)
        val etCategory = dialogView.findViewById<AutoCompleteTextView>(R.id.etCategory)

        // Setup autocomplete untuk kategori
        setupCategoryAutoComplete(rgTransactionType, etCategory)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val category = etCategory.text.toString().trim()
            val amountStr = etAmount.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (category.isEmpty() || amountStr.isEmpty()) {
                // Tampilkan pesan error
                return@setOnClickListener
            }

            val amount = amountStr.toDouble()
            val type = if (rgTransactionType.checkedRadioButtonId == R.id.rbIncome) "INCOME" else "EXPENSE"

            val transaction = Transaction(
                type = type,
                category = category,
                amount = amount,
                description = description
            )

            transactionViewModel.insert(transaction)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showEditDeleteDialog(transaction: Transaction) {
        val options = arrayOf("Edit Transaksi", "Hapus Transaksi")

        AlertDialog.Builder(this)
            .setTitle("Pilih Tindakan")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditTransactionDialog(transaction)
                    1 -> showDeleteConfirmationDialog(transaction)
                }
            }
            .show()
    }

    private fun showEditTransactionDialog(transaction: Transaction) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_transaction, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        val rgTransactionType = dialogView.findViewById<RadioGroup>(R.id.rgTransactionType)
        val etCategory = dialogView.findViewById<AutoCompleteTextView>(R.id.etCategory)

        // Setup autocomplete untuk kategori
        setupCategoryAutoComplete(rgTransactionType, etCategory)

        // Isi data yang sudah ada
        if (transaction.type == "INCOME") {
            rgTransactionType.check(R.id.rbIncome)
        } else {
            rgTransactionType.check(R.id.rbExpense)
        }

        etCategory.setText(transaction.category)
        val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
        etAmount.setText(numberFormat.format(transaction.amount))
        etDescription.setText(transaction.description)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Edit Transaksi")
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val category = etCategory.text.toString().trim()
            val amountStr = etAmount.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (category.isEmpty() || amountStr.isEmpty()) {
                // Tampilkan pesan error
                return@setOnClickListener
            }

            val amount = amountStr.toDouble()
            val type = if (rgTransactionType.checkedRadioButtonId == R.id.rbIncome) "INCOME" else "EXPENSE"

            val updatedTransaction = Transaction(
                id = transaction.id,
                type = type,
                category = category,
                amount = amount,
                description = description,
                date = transaction.date
            )

            transactionViewModel.update(updatedTransaction)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(transaction: Transaction) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Transaksi")
            .setMessage("Apakah Anda yakin ingin menghapus transaksi ini?")
            .setPositiveButton("Hapus") { _, _ ->
                transactionViewModel.delete(transaction)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun setupCategoryAutoComplete(
        rgTransactionType: RadioGroup,
        etCategory: AutoCompleteTextView
    ) {
        // Load kategori pemasukan secara default
        var categories = resources.getStringArray(R.array.income_categories)
        var adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        etCategory.setAdapter(adapter)

        // Listen for transaction type changes
        rgTransactionType.setOnCheckedChangeListener { _, checkedId ->
            categories = if (checkedId == R.id.rbIncome) {
                resources.getStringArray(R.array.income_categories)
            } else {
                resources.getStringArray(R.array.expense_categories)
            }
            adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
            etCategory.setAdapter(adapter)
        }
    }

    private fun applyTheme(themeMode: Int) {
        AppCompatDelegate.setDefaultNightMode(themeMode)
    }

    private fun saveThemeToPreferences(themeMode: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("ThemeMode", themeMode)
        editor.apply()
    }

    private fun getThemeFromPreferences(): Int {
        return sharedPreferences.getInt("ThemeMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}
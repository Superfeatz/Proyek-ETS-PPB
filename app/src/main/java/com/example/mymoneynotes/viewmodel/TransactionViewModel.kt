package com.example.mymoneynotes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.mymoneynotes.model.AppDatabase
import com.example.mymoneynotes.model.Transaction
import com.example.mymoneynotes.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TransactionRepository
    val allTransactions: LiveData<List<Transaction>>
    val totalIncome: LiveData<Double>
    val totalExpense: LiveData<Double>

    init {
        val transactionDao = AppDatabase.getDatabase(application).transactionDao()
        repository = TransactionRepository(transactionDao)
        allTransactions = repository.allTransactions
        totalIncome = repository.totalIncome
        totalExpense = repository.totalExpense
    }

    fun getTransactionsByType(type: String): LiveData<List<Transaction>> {
        return repository.getTransactionsByType(type)
    }

    fun insert(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(transaction)
    }

    fun update(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(transaction)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(transaction)
    }
}
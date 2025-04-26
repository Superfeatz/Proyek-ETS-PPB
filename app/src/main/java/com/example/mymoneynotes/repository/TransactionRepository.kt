package com.example.mymoneynotes.repository

import androidx.lifecycle.LiveData
import com.example.mymoneynotes.model.Transaction
import com.example.mymoneynotes.model.TransactionDao

class TransactionRepository(private val transactionDao: TransactionDao) {
    val allTransactions: LiveData<List<Transaction>> = transactionDao.getAllTransactions()
    val totalIncome: LiveData<Double> = transactionDao.getTotalIncome()
    val totalExpense: LiveData<Double> = transactionDao.getTotalExpense()

    fun getTransactionsByType(type: String): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByType(type)
    }

    suspend fun insert(transaction: Transaction): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun update(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun delete(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }
}
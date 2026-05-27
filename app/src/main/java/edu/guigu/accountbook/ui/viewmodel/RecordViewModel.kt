package edu.guigu.accountbook.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import edu.guigu.accountbook.data.dao.CategorySummary
import edu.guigu.accountbook.data.database.AppDatabase
import edu.guigu.accountbook.data.model.Record
import edu.guigu.accountbook.data.repository.RecordRepository
import kotlinx.coroutines.launch

class RecordViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecordRepository

    private val _allRecords = MutableLiveData<List<Record>>(emptyList())
    val allRecords: LiveData<List<Record>> get() = _allRecords

    private val _totalIncome = MutableLiveData(0.0)
    val totalIncome: LiveData<Double> get() = _totalIncome

    private val _totalExpense = MutableLiveData(0.0)
    val totalExpense: LiveData<Double> get() = _totalExpense

    private val _expenseCategorySummary = MutableLiveData<List<CategorySummary>>(emptyList())
    val expenseCategorySummary: LiveData<List<CategorySummary>> get() = _expenseCategorySummary

    init {
        val dao = AppDatabase.getInstance(application).recordDao()
        repository = RecordRepository(dao)
        refreshData()
    }

    private fun refreshData() {
        viewModelScope.launch {
            _allRecords.postValue(repository.getAllRecords())
            _totalIncome.postValue(repository.getTotalIncome() ?: 0.0)
            _totalExpense.postValue(repository.getTotalExpense() ?: 0.0)
            _expenseCategorySummary.postValue(repository.getCategorySummary(Record.TYPE_EXPENSE))
        }
    }

    fun insert(record: Record) {
        viewModelScope.launch {
            repository.insert(record)
            refreshData()
        }
    }

    fun update(record: Record) {
        viewModelScope.launch {
            repository.update(record)
            refreshData()
        }
    }

    fun delete(record: Record) {
        viewModelScope.launch {
            repository.delete(record)
            refreshData()
        }
    }
}

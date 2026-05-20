package com.example.bankingapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bankingapp.model.Transaction;
import com.example.bankingapp.repository.TransactionRepository;
import com.example.bankingapp.utils.AppExecutors;

import java.util.Collections;
import java.util.List;

public class TransactionHistoryViewModel extends ViewModel {

    public static final int MINI_STATEMENT_SIZE = 5;

    private final TransactionRepository transactions;
    private final AppExecutors executors;
    private final MutableLiveData<List<Transaction>> result = new MutableLiveData<>(Collections.emptyList());

    public TransactionHistoryViewModel(TransactionRepository transactions, AppExecutors executors) {
        this.transactions = transactions;
        this.executors = executors;
    }

    public LiveData<List<Transaction>> getResult() { return result; }

    public void loadMini(String accountNumber) {
        executors.io().execute(() ->
                result.postValue(transactions.findLastN(accountNumber, MINI_STATEMENT_SIZE)));
    }

    public void loadCustomized(String accountNumber,
                                long fromMillis,
                                long toMillis,
                                long amountLowerLimit,
                                int maxCount) {
        if (fromMillis > toMillis) {
            // F41 / F43 — from > to → empty result.
            result.setValue(Collections.emptyList());
            return;
        }
        executors.io().execute(() -> result.postValue(transactions.findCustomized(
                accountNumber, fromMillis, toMillis, amountLowerLimit, maxCount)));
    }
}

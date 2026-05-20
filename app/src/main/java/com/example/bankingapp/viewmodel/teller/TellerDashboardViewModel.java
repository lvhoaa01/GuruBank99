package com.example.bankingapp.viewmodel.teller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bankingapp.model.Teller;
import com.example.bankingapp.repository.TellerRepository;
import com.example.bankingapp.utils.AppExecutors;

public class TellerDashboardViewModel extends ViewModel {

    private final TellerRepository tellers;
    private final AppExecutors executors;
    private final MutableLiveData<Teller> teller = new MutableLiveData<>();

    public TellerDashboardViewModel(TellerRepository tellers, AppExecutors executors) {
        this.tellers = tellers;
        this.executors = executors;
    }

    public LiveData<Teller> getTeller() { return teller; }

    public void load(int userId) {
        executors.io().execute(() -> teller.postValue(tellers.findByUserId(userId)));
    }
}

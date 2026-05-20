package com.example.bankingapp;

import android.app.Application;

import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

import com.example.bankingapp.repository.RepositoryProvider;

/**
 * Application entry point. Bootstraps the in-memory repositories
 * with seeded fake data so the app is usable on first launch.
 *
 * Also exposes an application-scoped {@link ViewModelStore} so that
 * a few screens (Transfer → OTP) can share a single ViewModel
 * instance across activities.
 */
public class BankingApp extends Application implements ViewModelStoreOwner {

    private final ViewModelStore appViewModelStore = new ViewModelStore();

    @Override
    public void onCreate() {
        super.onCreate();
        RepositoryProvider.init();
    }

    @Override
    public ViewModelStore getViewModelStore() {
        return appViewModelStore;
    }
}

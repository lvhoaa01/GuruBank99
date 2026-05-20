package com.example.bankingapp.repository;

import com.example.bankingapp.model.Teller;

import java.util.List;

public interface TellerRepository {

    Teller findById(int tellerId);

    Teller findByUserId(int userId);

    /** All tellers in a branch — handy for admin views (not used by Customer flows). */
    List<Teller> findByBranch(String branchId);

    void save(Teller teller);

    /** The next integer to use for a new MaNV; thread-safe at the DB level. */
    int nextTellerId();
}

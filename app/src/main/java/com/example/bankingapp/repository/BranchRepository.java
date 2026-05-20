package com.example.bankingapp.repository;

import com.example.bankingapp.model.Branch;

import java.util.List;

public interface BranchRepository {

    Branch findById(String branchId);

    List<Branch> findAll();

    void save(Branch branch);
}

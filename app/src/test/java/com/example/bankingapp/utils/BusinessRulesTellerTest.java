package com.example.bankingapp.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Branch isolation tests for {@link BusinessRules#tellerCanAccessCustomer}
 * and {@link BusinessRules#tellerCanAccessAccount}. 4-branch CFG → 4 dedicated tests.
 */
public class BusinessRulesTellerTest {

    @Test public void tellerBranchMissing_returnsTellerBranchMissing() {
        assertEquals(BusinessRules.BranchAccessResult.TELLER_BRANCH_MISSING,
                BusinessRules.tellerCanAccessCustomer(null, "CN001"));
    }

    @Test public void targetBranchMissing_returnsTargetBranchMissing() {
        assertEquals(BusinessRules.BranchAccessResult.TARGET_BRANCH_MISSING,
                BusinessRules.tellerCanAccessCustomer("CN001", null));
    }

    @Test public void differentBranches_returnsNotSameBranch() {
        assertEquals(BusinessRules.BranchAccessResult.NOT_SAME_BRANCH,
                BusinessRules.tellerCanAccessCustomer("CN001", "CN002"));
    }

    @Test public void sameBranch_returnsAllowed() {
        assertEquals(BusinessRules.BranchAccessResult.ALLOWED,
                BusinessRules.tellerCanAccessCustomer("CN001", "CN001"));
    }

    @Test public void accountAccess_sameBranch_allowed() {
        assertEquals(BusinessRules.BranchAccessResult.ALLOWED,
                BusinessRules.tellerCanAccessAccount("CN001", "CN001"));
    }

    @Test public void accountAccess_differentBranch_notAllowed() {
        assertEquals(BusinessRules.BranchAccessResult.NOT_SAME_BRANCH,
                BusinessRules.tellerCanAccessAccount("CN001", "CN002"));
    }
}

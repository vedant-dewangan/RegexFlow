package com.regexflow.backend.Enums;

public enum TransactionType {

    // UPI & Transfers
    UPI_CREDIT,
    UPI_DEBIT,

    // Bank Transactions
    ATM_WITHDRAWAL,
    CASH_DEPOSIT,

    // Bills & Utilities
    BILL,
    SALARY,

    // Loans & EMI
    EMI_DEBIT,
    LOAN_CREDIT,

    // Cards
    CREDIT_CARD_PAYMENT,
    DEBIT_CARD_SPEND,

    // Investments
    MUTUAL_FUND_PURCHASE,
    FIXED_DEPOSIT_MATURITY,
}
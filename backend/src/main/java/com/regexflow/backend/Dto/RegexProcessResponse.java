package com.regexflow.backend.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegexProcessResponse {
    // Transaction Details
    private String bankAcId;
    private String amount;
    private Boolean amountNegative;
    private String date;
    private String merchant;
    private String txnNote;
    private String balance;
    private Boolean balanceNegative;

    // Sender/Receiver Details
    private String senderName;
    private String sBank;
    private String sAcType;
    private String sAcId;
    private String receiverName;
    private String rBank;

    // General Information
    private String availLimit;
    private String creditLimit;
    private String paymentType;
    private String city;

    // Biller Details
    private String billerAcId;
    private String billId;
    private String billDate;
    private String billPeriod;
    private String dueDate;
    private String minAmtDue;
    private String totAmtDue;

    // FD Details (Fixed Deposit)
    private String principalAmount;
    private String frequency;
    private String maturityDate;
    private String maturityAmount;
    private String rateOfInterest;

    // MF Details (Mutual Fund)
    private String mfNav;
    private String mfUnits;
    private String mfArn;
    private String mfBalUnits;
    private String mfSchemeBal;

    // Order Details
    private String amountPaid;
    private String offerAmount;
    private String minPurchaseAmt;
}

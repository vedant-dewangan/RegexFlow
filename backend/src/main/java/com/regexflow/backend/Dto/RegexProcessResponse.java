package com.regexflow.backend.Dto;

import lombok.Data;

@Data
public class RegexProcessResponse {

    // Basic Transaction Fields
    private FieldResult bankAcId = new FieldResult(null, -1);
    private FieldResult amount = new FieldResult(null, -1);
    private FieldResult amountNegative = new FieldResult(null, -1);
    private FieldResult date = new FieldResult(null, -1);
    private FieldResult merchant = new FieldResult(null, -1);
    private FieldResult txnNote = new FieldResult(null, -1);
    private FieldResult balance = new FieldResult(null, -1);
    private FieldResult balanceNegative = new FieldResult(null, -1);

    // Sender/Receiver Details
    private FieldResult senderName = new FieldResult(null, -1);
    private FieldResult sBank = new FieldResult(null, -1);
    private FieldResult sAcType = new FieldResult(null, -1);
    private FieldResult sAcId = new FieldResult(null, -1);
    private FieldResult receiverName = new FieldResult(null, -1);
    private FieldResult rBank = new FieldResult(null, -1);

    // General Information
    private FieldResult availLimit = new FieldResult(null, -1);
    private FieldResult creditLimit = new FieldResult(null, -1);
    private FieldResult paymentType = new FieldResult(null, -1);
    private FieldResult city = new FieldResult(null, -1);

    // Biller Details
    private FieldResult billerAcId = new FieldResult(null, -1);
    private FieldResult billId = new FieldResult(null, -1);
    private FieldResult billDate = new FieldResult(null, -1);
    private FieldResult billPeriod = new FieldResult(null, -1);
    private FieldResult dueDate = new FieldResult(null, -1);
    private FieldResult minAmtDue = new FieldResult(null, -1);
    private FieldResult totAmtDue = new FieldResult(null, -1);

    // FD Details (Fixed Deposit)
    private FieldResult principalAmount = new FieldResult(null, -1);
    private FieldResult frequency = new FieldResult(null, -1);
    private FieldResult maturityDate = new FieldResult(null, -1);
    private FieldResult maturityAmount = new FieldResult(null, -1);
    private FieldResult rateOfInterest = new FieldResult(null, -1);

    // MF Details (Mutual Fund)
    private FieldResult mfNav = new FieldResult(null, -1);
    private FieldResult mfUnits = new FieldResult(null, -1);
    private FieldResult mfArn = new FieldResult(null, -1);
    private FieldResult mfBalUnits = new FieldResult(null, -1);
    private FieldResult mfSchemeBal = new FieldResult(null, -1);

    // Order Details
    private FieldResult amountPaid = new FieldResult(null, -1);
    private FieldResult offerAmount = new FieldResult(null, -1);
    private FieldResult minPurchaseAmt = new FieldResult(null, -1);
}

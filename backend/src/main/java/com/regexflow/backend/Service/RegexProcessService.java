package com.regexflow.backend.Service;

import com.regexflow.backend.Dto.RegexProcessRequest;
import com.regexflow.backend.Dto.RegexProcessResponse;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Service
public class RegexProcessService {

    public RegexProcessResponse processRegex(RegexProcessRequest request) {
        RegexProcessResponse response = new RegexProcessResponse();
        
        // Initialize all fields with "-1" as default (matching the image)
        initializeDefaultValues(response);
        
        try {
            // Compile the regex pattern
            Pattern pattern = Pattern.compile(request.getRegexPattern());
            Matcher matcher = pattern.matcher(request.getRawMsg());
            
            if (matcher.find()) {
                // Extract fields based on named groups or positional groups
                extractFields(matcher, response, request);
            } else {
                // If pattern doesn't match, return default values
                return response;
            }
        } catch (PatternSyntaxException e) {
            // Invalid regex pattern - return default values
            return response;
        }
        
        return response;
    }
    
    public void initializeDefaultValues(RegexProcessResponse response) {
        response.setBankAcId("-1");
        response.setAmount("-1");
        response.setAmountNegative(false);
        response.setDate("-1");
        response.setMerchant("-1");
        response.setTxnNote("-1");
        response.setBalance("-1");
        response.setBalanceNegative(false);
        
        response.setSenderName("-1");
        response.setSBank("-1");
        response.setSAcType("-1");
        response.setSAcId("-1");
        response.setReceiverName("-1");
        response.setRBank("-1");
        
        response.setAvailLimit("-1");
        response.setCreditLimit("-1");
        response.setPaymentType("-1");
        response.setCity("-1");
        
        response.setBillerAcId("-1");
        response.setBillId("-1");
        response.setBillDate("-1");
        response.setBillPeriod("-1");
        response.setDueDate("-1");
        response.setMinAmtDue("-1");
        response.setTotAmtDue("-1");
        
        response.setPrincipalAmount("-1");
        response.setFrequency("-1");
        response.setMaturityDate("-1");
        response.setMaturityAmount("-1");
        response.setRateOfInterest("-1");
        
        response.setMfNav("-1");
        response.setMfUnits("-1");
        response.setMfArn("-1");
        response.setMfBalUnits("-1");
        response.setMfSchemeBal("-1");
        
        response.setAmountPaid("-1");
        response.setOfferAmount("-1");
        response.setMinPurchaseAmt("-1");
    }
    
    private void extractFields(Matcher matcher, RegexProcessResponse response, RegexProcessRequest request) {
        int groupCount = matcher.groupCount();
        
        // Try to extract using named groups first (if pattern uses named groups like (?<amount>...))
        // Java regex named groups are accessed via group("name")
        try {
            tryExtractNamedGroup(matcher, "bankAcId", response::setBankAcId);
            tryExtractNamedGroup(matcher, "amount", response::setAmount);
            tryExtractNamedGroup(matcher, "date", response::setDate);
            tryExtractNamedGroup(matcher, "merchant", response::setMerchant);
            tryExtractNamedGroup(matcher, "txnNote", response::setTxnNote);
            tryExtractNamedGroup(matcher, "balance", response::setBalance);
            tryExtractNamedGroup(matcher, "senderName", response::setSenderName);
            tryExtractNamedGroup(matcher, "sBank", response::setSBank);
            tryExtractNamedGroup(matcher, "sAcType", response::setSAcType);
            tryExtractNamedGroup(matcher, "sAcId", response::setSAcId);
            tryExtractNamedGroup(matcher, "receiverName", response::setReceiverName);
            tryExtractNamedGroup(matcher, "rBank", response::setRBank);
            tryExtractNamedGroup(matcher, "availLimit", response::setAvailLimit);
            tryExtractNamedGroup(matcher, "creditLimit", response::setCreditLimit);
            tryExtractNamedGroup(matcher, "paymentType", response::setPaymentType);
            tryExtractNamedGroup(matcher, "city", response::setCity);
            tryExtractNamedGroup(matcher, "billerAcId", response::setBillerAcId);
            tryExtractNamedGroup(matcher, "billId", response::setBillId);
            tryExtractNamedGroup(matcher, "billDate", response::setBillDate);
            tryExtractNamedGroup(matcher, "billPeriod", response::setBillPeriod);
            tryExtractNamedGroup(matcher, "dueDate", response::setDueDate);
            tryExtractNamedGroup(matcher, "minAmtDue", response::setMinAmtDue);
            tryExtractNamedGroup(matcher, "totAmtDue", response::setTotAmtDue);
            tryExtractNamedGroup(matcher, "principalAmount", response::setPrincipalAmount);
            tryExtractNamedGroup(matcher, "frequency", response::setFrequency);
            tryExtractNamedGroup(matcher, "maturityDate", response::setMaturityDate);
            tryExtractNamedGroup(matcher, "maturityAmount", response::setMaturityAmount);
            tryExtractNamedGroup(matcher, "rateOfInterest", response::setRateOfInterest);
            tryExtractNamedGroup(matcher, "mfNav", response::setMfNav);
            tryExtractNamedGroup(matcher, "mfUnits", response::setMfUnits);
            tryExtractNamedGroup(matcher, "mfArn", response::setMfArn);
            tryExtractNamedGroup(matcher, "mfBalUnits", response::setMfBalUnits);
            tryExtractNamedGroup(matcher, "mfSchemeBal", response::setMfSchemeBal);
            tryExtractNamedGroup(matcher, "amountPaid", response::setAmountPaid);
            tryExtractNamedGroup(matcher, "offerAmount", response::setOfferAmount);
            tryExtractNamedGroup(matcher, "minPurchaseAmt", response::setMinPurchaseAmt);
        } catch (Exception e) {
            // Named groups not available, will use positional groups
        }
        
        // Extract positional groups (groups 1, 2, 3, etc.)
        // Note: This is a basic implementation. In production, you'd want a configuration
        // that maps group positions to field names based on the pattern structure
        if (groupCount > 0) {
            // Store all matched groups - the actual mapping would depend on the pattern structure
            // For now, we'll extract what we can and leave the rest as "-1"
            for (int i = 1; i <= groupCount && i <= 40; i++) {
                try {
                    String groupValue = matcher.group(i);
                    if (groupValue != null && !groupValue.isEmpty()) {
                        // Basic heuristic mapping - can be enhanced with pattern-specific logic
                        mapPositionalGroup(i, groupValue, response);
                    }
                } catch (Exception e) {
                    // Group doesn't exist - continue
                }
            }
        }
        
        // Set payment type from request if not extracted from pattern
        if ("-1".equals(response.getPaymentType()) && request.getPaymentType() != null) {
            response.setPaymentType(request.getPaymentType().name());
        }
        
        // Check for negative amounts/balances in the raw message
        String rawMsg = request.getRawMsg().toLowerCase();
        if (rawMsg.contains("debited") || rawMsg.contains("debit")) {
            response.setAmountNegative(true);
        }
        if (rawMsg.contains("withdrawal") || rawMsg.contains("withdrawn")) {
            response.setBalanceNegative(true);
        }
    }
    
    private void tryExtractNamedGroup(Matcher matcher, String groupName, java.util.function.Consumer<String> setter) {
        try {
            String value = matcher.group(groupName);
            if (value != null && !value.isEmpty() && !"-1".equals(value)) {
                setter.accept(value);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Named group doesn't exist or matcher not in valid state - ignore
        }
    }
    
    private void mapPositionalGroup(int groupIndex, String value, RegexProcessResponse response) {
        // Basic heuristic mapping - this is a simplified approach
        // In production, you'd want pattern-specific configuration
        // Common patterns: group 1 = amount, group 2 = date, etc.
        switch (groupIndex) {
            case 1:
                if ("-1".equals(response.getAmount())) {
                    response.setAmount(value);
                }
                break;
            case 2:
                if ("-1".equals(response.getDate())) {
                    response.setDate(value);
                }
                break;
            case 3:
                if ("-1".equals(response.getMerchant())) {
                    response.setMerchant(value);
                }
                break;
            case 4:
                if ("-1".equals(response.getBalance())) {
                    response.setBalance(value);
                }
                break;
            // Add more mappings as needed based on common pattern structures
        }
    }
}

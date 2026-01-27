package com.regexflow.backend.Service;

import com.regexflow.backend.Dto.FieldResult;
import com.regexflow.backend.Dto.RegexProcessRequest;
import com.regexflow.backend.Dto.RegexProcessResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RegexProcessService {

    public RegexProcessResponse processRegex(RegexProcessRequest request) {

        RegexProcessResponse response = new RegexProcessResponse();

        try {
            Pattern pattern = Pattern.compile(request.getRegexPattern(), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(request.getRawMsg());

            if (!matcher.find()) {
                return response; // all fields will have index -1 and value null
            }

            // Map to store group number for each named group
            Map<String, Integer> groupNumberMap = findGroupNumbers(request.getRegexPattern());
            
            // Extract all named groups from the matcher
            Map<String, FieldResult> extractedFields = new HashMap<>();
            
            // All possible field names to extract
            String[] fieldNames = {
                // Basic Transaction Fields
                "bankAcId", "amount", "amountNegative", "date", "merchant", "txnNote", "balance", "balanceNegative",
                // Sender/Receiver Details
                "senderName", "sBank", "sAcType", "sAcId", "receiverName", "rBank",
                // General Information
                "availLimit", "creditLimit", "paymentType", "city",
                // Biller Details
                "billerAcId", "billId", "billDate", "billPeriod", "dueDate", "minAmtDue", "totAmtDue",
                // FD Details
                "principalAmount", "frequency", "maturityDate", "maturityAmount", "rateOfInterest",
                // MF Details
                "mfNav", "mfUnits", "mfArn", "mfBalUnits", "mfSchemeBal",
                // Order Details
                "amountPaid", "offerAmount", "minPurchaseAmt"
            };
            
            for (String fieldName : fieldNames) {
                try {
                    String groupValue = matcher.group(fieldName);
                    if (groupValue != null) {
                        // Get the group number (1-based index) from the pattern
                        int groupIndex = groupNumberMap.getOrDefault(fieldName, -1);
                        extractedFields.put(fieldName, new FieldResult(groupValue, groupIndex));
                    }
                } catch (IllegalArgumentException e) {
                    // Group name doesn't exist in pattern, skip it
                }
            }

            // Map extracted fields to response
            // Basic Transaction Fields
            if (extractedFields.containsKey("bankAcId")) {
                response.setBankAcId(extractedFields.get("bankAcId"));
            }
            if (extractedFields.containsKey("amount")) {
                response.setAmount(extractedFields.get("amount"));
            }
            if (extractedFields.containsKey("amountNegative")) {
                response.setAmountNegative(extractedFields.get("amountNegative"));
            }
            if (extractedFields.containsKey("date")) {
                response.setDate(extractedFields.get("date"));
            }
            if (extractedFields.containsKey("merchant")) {
                response.setMerchant(extractedFields.get("merchant"));
            }
            if (extractedFields.containsKey("txnNote")) {
                response.setTxnNote(extractedFields.get("txnNote"));
            }
            if (extractedFields.containsKey("balance")) {
                response.setBalance(extractedFields.get("balance"));
            }
            if (extractedFields.containsKey("balanceNegative")) {
                response.setBalanceNegative(extractedFields.get("balanceNegative"));
            }

            // Sender/Receiver Details
            if (extractedFields.containsKey("senderName")) {
                response.setSenderName(extractedFields.get("senderName"));
            }
            if (extractedFields.containsKey("sBank")) {
                response.setSBank(extractedFields.get("sBank"));
            }
            if (extractedFields.containsKey("sAcType")) {
                response.setSAcType(extractedFields.get("sAcType"));
            }
            if (extractedFields.containsKey("sAcId")) {
                response.setSAcId(extractedFields.get("sAcId"));
            }
            if (extractedFields.containsKey("receiverName")) {
                response.setReceiverName(extractedFields.get("receiverName"));
            }
            if (extractedFields.containsKey("rBank")) {
                response.setRBank(extractedFields.get("rBank"));
            }

            // General Information
            if (extractedFields.containsKey("availLimit")) {
                response.setAvailLimit(extractedFields.get("availLimit"));
            }
            if (extractedFields.containsKey("creditLimit")) {
                response.setCreditLimit(extractedFields.get("creditLimit"));
            }
            if (extractedFields.containsKey("paymentType")) {
                response.setPaymentType(extractedFields.get("paymentType"));
            }
            if (extractedFields.containsKey("city")) {
                response.setCity(extractedFields.get("city"));
            }

            // Biller Details
            if (extractedFields.containsKey("billerAcId")) {
                response.setBillerAcId(extractedFields.get("billerAcId"));
            }
            if (extractedFields.containsKey("billId")) {
                response.setBillId(extractedFields.get("billId"));
            }
            if (extractedFields.containsKey("billDate")) {
                response.setBillDate(extractedFields.get("billDate"));
            }
            if (extractedFields.containsKey("billPeriod")) {
                response.setBillPeriod(extractedFields.get("billPeriod"));
            }
            if (extractedFields.containsKey("dueDate")) {
                response.setDueDate(extractedFields.get("dueDate"));
            }
            if (extractedFields.containsKey("minAmtDue")) {
                response.setMinAmtDue(extractedFields.get("minAmtDue"));
            }
            if (extractedFields.containsKey("totAmtDue")) {
                response.setTotAmtDue(extractedFields.get("totAmtDue"));
            }

            // FD Details
            if (extractedFields.containsKey("principalAmount")) {
                response.setPrincipalAmount(extractedFields.get("principalAmount"));
            }
            if (extractedFields.containsKey("frequency")) {
                response.setFrequency(extractedFields.get("frequency"));
            }
            if (extractedFields.containsKey("maturityDate")) {
                response.setMaturityDate(extractedFields.get("maturityDate"));
            }
            if (extractedFields.containsKey("maturityAmount")) {
                response.setMaturityAmount(extractedFields.get("maturityAmount"));
            }
            if (extractedFields.containsKey("rateOfInterest")) {
                response.setRateOfInterest(extractedFields.get("rateOfInterest"));
            }

            // MF Details
            if (extractedFields.containsKey("mfNav")) {
                response.setMfNav(extractedFields.get("mfNav"));
            }
            if (extractedFields.containsKey("mfUnits")) {
                response.setMfUnits(extractedFields.get("mfUnits"));
            }
            if (extractedFields.containsKey("mfArn")) {
                response.setMfArn(extractedFields.get("mfArn"));
            }
            if (extractedFields.containsKey("mfBalUnits")) {
                response.setMfBalUnits(extractedFields.get("mfBalUnits"));
            }
            if (extractedFields.containsKey("mfSchemeBal")) {
                response.setMfSchemeBal(extractedFields.get("mfSchemeBal"));
            }

            // Order Details
            if (extractedFields.containsKey("amountPaid")) {
                response.setAmountPaid(extractedFields.get("amountPaid"));
            }
            if (extractedFields.containsKey("offerAmount")) {
                response.setOfferAmount(extractedFields.get("offerAmount"));
            }
            if (extractedFields.containsKey("minPurchaseAmt")) {
                response.setMinPurchaseAmt(extractedFields.get("minPurchaseAmt"));
            }

        } catch (Exception e) {
            return response;
        }

        return response;
    }

    /**
     * Finds the group number (1-based) for each named group in the regex pattern.
     * Named groups are in the format (?<name>...)
     */
    private Map<String, Integer> findGroupNumbers(String regexPattern) {
        Map<String, Integer> groupNumberMap = new HashMap<>();
        int groupCount = 0;
        int i = 0;
        
        while (i < regexPattern.length()) {
            if (regexPattern.charAt(i) == '\\') {
                // Skip escaped characters (including escaped parentheses)
                i += 2;
                if (i > regexPattern.length()) break;
                continue;
            }
            
            if (regexPattern.charAt(i) == '(') {
                // Check if it's a named group: (?<name>...)
                if (i + 3 < regexPattern.length() && 
                    regexPattern.charAt(i + 1) == '?' && 
                    regexPattern.charAt(i + 2) == '<') {
                    
                    // Find the closing '>' for the group name
                    int nameStart = i + 3;
                    int nameEnd = regexPattern.indexOf('>', nameStart);
                    
                    if (nameEnd != -1) {
                        String groupName = regexPattern.substring(nameStart, nameEnd);
                        groupCount++;
                        groupNumberMap.put(groupName, groupCount);
                        i = nameEnd + 1;
                        continue;
                    }
                } else if (i + 2 < regexPattern.length() && 
                          regexPattern.charAt(i + 1) == '?') {
                    // Non-capturing group: (?:...), (?=...), (?!...), (?<=...), (?<!...)
                    // Skip the '?', don't increment groupCount, continue parsing normally
                    i += 2;
                    continue;
                } else {
                    // Regular capturing group
                    groupCount++;
                }
            }
            
            i++;
        }
        
        return groupNumberMap;
    }
}

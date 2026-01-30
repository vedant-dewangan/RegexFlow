package com.regexflow.backend.Service;

import com.regexflow.backend.Dto.RegexProcessRequest;
import com.regexflow.backend.Dto.RegexProcessResponse;
import com.regexflow.backend.Enums.PaymentType;
import com.regexflow.backend.Enums.SmsType;
import com.regexflow.backend.Enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegexProcessService Tests")
class RegexProcessServiceTest {

    @InjectMocks
    private RegexProcessService regexProcessService;

    private RegexProcessRequest request;

    @BeforeEach
    void setUp() {
        request = new RegexProcessRequest();
        request.setSmsType(SmsType.DEBIT);
        request.setPaymentType(PaymentType.UPI);
        request.setTransactionType(TransactionType.UPI_DEBIT);
    }

    @Nested
    @DisplayName("Basic Regex Processing Tests")
    class BasicRegexProcessingTests {

        @Test
        @DisplayName("Should extract amount from SMS")
        void processRegex_ShouldExtractAmount() {
            // Arrange
            request.setRegexPattern("Rs\\.?\\s*(?<amount>[\\d,]+\\.?\\d*)");
            request.setRawMsg("Your account has been debited Rs. 1,500.00");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getAmount());
            assertEquals("1,500.00", response.getAmount().getValue());
            assertTrue(response.getAmount().getIndex() >= 0);
        }

        @Test
        @DisplayName("Should extract multiple named groups")
        void processRegex_ShouldExtractMultipleNamedGroups() {
            // Arrange
            request.setRegexPattern("Rs\\.?\\s*(?<amount>[\\d,]+\\.?\\d*).*(?<merchant>[A-Za-z]+\\s*[A-Za-z]*)");
            request.setRawMsg("Rs. 500.00 paid to Amazon Store");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getAmount());
            assertEquals("500.00", response.getAmount().getValue());
            assertNotNull(response.getMerchant());
            assertNotNull(response.getMerchant().getValue());
        }

        @Test
        @DisplayName("Should return empty response when pattern does not match")
        void processRegex_WhenNoMatch_ShouldReturnEmptyResponse() {
            // Arrange
            request.setRegexPattern("(?<amount>\\$[\\d,]+)");
            request.setRawMsg("Your account has been debited Rs. 1,500.00");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNull(response.getAmount().getValue());
            assertEquals(-1, response.getAmount().getIndex());
        }

        @Test
        @DisplayName("Should handle case insensitive matching")
        void processRegex_ShouldHandleCaseInsensitiveMatching() {
            // Arrange
            request.setRegexPattern("(?<amount>RS\\.?\\s*[\\d,]+)");
            request.setRawMsg("Your account has been debited rs. 1500");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getAmount());
            assertEquals("rs. 1500", response.getAmount().getValue());
        }
    }

    @Nested
    @DisplayName("Bank Transaction SMS Tests")
    class BankTransactionSmsTests {

        @Test
        @DisplayName("Should extract bank account ID")
        void processRegex_ShouldExtractBankAcId() {
            // Arrange
            request.setRegexPattern("A/c\\s*(?<bankAcId>\\d+)");
            request.setRawMsg("Your A/c 1234567890 has been debited");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getBankAcId());
            assertEquals("1234567890", response.getBankAcId().getValue());
        }

        @Test
        @DisplayName("Should extract balance")
        void processRegex_ShouldExtractBalance() {
            // Arrange
            request.setRegexPattern("Bal:\\s*Rs\\.?\\s*(?<balance>[\\d,]+\\.?\\d*)");
            request.setRawMsg("Debited Rs.500. Bal: Rs. 10,000.50");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getBalance());
            assertEquals("10,000.50", response.getBalance().getValue());
        }

        @Test
        @DisplayName("Should extract date")
        void processRegex_ShouldExtractDate() {
            // Arrange
            request.setRegexPattern("on\\s*(?<date>\\d{2}/\\d{2}/\\d{4})");
            request.setRawMsg("Transaction on 15/01/2024 for Rs.500");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getDate());
            assertEquals("15/01/2024", response.getDate().getValue());
        }

        @Test
        @DisplayName("Should extract transaction note")
        void processRegex_ShouldExtractTxnNote() {
            // Arrange
            request.setRegexPattern("Ref:\\s*(?<txnNote>[A-Za-z0-9]+)");
            request.setRawMsg("Debited Rs.500. Ref: TXN123456789");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getTxnNote());
            assertEquals("TXN123456789", response.getTxnNote().getValue());
        }
    }

    @Nested
    @DisplayName("UPI Transaction Tests")
    class UpiTransactionTests {

        @Test
        @DisplayName("Should extract sender name")
        void processRegex_ShouldExtractSenderName() {
            // Arrange
            request.setRegexPattern("from\\s*(?<senderName>[A-Za-z\\s]+)");
            request.setRawMsg("Received Rs.1000 from John Doe via UPI");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getSenderName());
            assertTrue(response.getSenderName().getValue().contains("John"));
        }

        @Test
        @DisplayName("Should extract receiver name")
        void processRegex_ShouldExtractReceiverName() {
            // Arrange
            request.setRegexPattern("to\\s*(?<receiverName>[A-Za-z\\s]+)");
            request.setRawMsg("Paid Rs.500 to Jane Smith via UPI");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getReceiverName());
            assertTrue(response.getReceiverName().getValue().contains("Jane"));
        }
    }

    @Nested
    @DisplayName("Credit Card Tests")
    class CreditCardTests {

        @Test
        @DisplayName("Should extract available limit")
        void processRegex_ShouldExtractAvailLimit() {
            // Arrange
            request.setRegexPattern("Avl Lmt:\\s*Rs\\.?\\s*(?<availLimit>[\\d,]+)");
            request.setRawMsg("Credit Card spent Rs.5000. Avl Lmt: Rs. 45,000");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getAvailLimit());
            assertEquals("45,000", response.getAvailLimit().getValue());
        }

        @Test
        @DisplayName("Should extract credit limit")
        void processRegex_ShouldExtractCreditLimit() {
            // Arrange
            request.setRegexPattern("Credit Limit:\\s*Rs\\.?\\s*(?<creditLimit>[\\d,]+)");
            request.setRawMsg("Your Credit Limit: Rs. 50,000");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getCreditLimit());
            assertEquals("50,000", response.getCreditLimit().getValue());
        }
    }

    @Nested
    @DisplayName("Bill Payment Tests")
    class BillPaymentTests {

        @Test
        @DisplayName("Should extract bill ID")
        void processRegex_ShouldExtractBillId() {
            // Arrange
            request.setRegexPattern("Bill No:\\s*(?<billId>[A-Za-z0-9]+)");
            request.setRawMsg("Bill No: ELEC123456 paid successfully");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getBillId());
            assertEquals("ELEC123456", response.getBillId().getValue());
        }

        @Test
        @DisplayName("Should extract due date")
        void processRegex_ShouldExtractDueDate() {
            // Arrange
            request.setRegexPattern("Due Date:\\s*(?<dueDate>\\d{2}-\\d{2}-\\d{4})");
            request.setRawMsg("Bill amount Rs.1500. Due Date: 25-01-2024");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getDueDate());
            assertEquals("25-01-2024", response.getDueDate().getValue());
        }

        @Test
        @DisplayName("Should extract total amount due")
        void processRegex_ShouldExtractTotAmtDue() {
            // Arrange
            request.setRegexPattern("Total Due:\\s*Rs\\.?\\s*(?<totAmtDue>[\\d,]+\\.?\\d*)");
            request.setRawMsg("Your bill. Total Due: Rs. 2,500.00");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getTotAmtDue());
            assertEquals("2,500.00", response.getTotAmtDue().getValue());
        }
    }

    @Nested
    @DisplayName("Fixed Deposit Tests")
    class FixedDepositTests {

        @Test
        @DisplayName("Should extract principal amount")
        void processRegex_ShouldExtractPrincipalAmount() {
            // Arrange
            request.setRegexPattern("Principal:\\s*Rs\\.?\\s*(?<principalAmount>[\\d,]+)");
            request.setRawMsg("FD created. Principal: Rs. 1,00,000");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getPrincipalAmount());
            assertEquals("1,00,000", response.getPrincipalAmount().getValue());
        }

        @Test
        @DisplayName("Should extract maturity date")
        void processRegex_ShouldExtractMaturityDate() {
            // Arrange
            request.setRegexPattern("Maturity:\\s*(?<maturityDate>\\d{2}/\\d{2}/\\d{4})");
            request.setRawMsg("FD Maturity: 15/01/2025");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getMaturityDate());
            assertEquals("15/01/2025", response.getMaturityDate().getValue());
        }

        @Test
        @DisplayName("Should extract rate of interest")
        void processRegex_ShouldExtractRateOfInterest() {
            // Arrange
            request.setRegexPattern("ROI:\\s*(?<rateOfInterest>[\\d.]+)%");
            request.setRawMsg("FD created at ROI: 7.5%");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getRateOfInterest());
            assertEquals("7.5", response.getRateOfInterest().getValue());
        }
    }

    @Nested
    @DisplayName("Mutual Fund Tests")
    class MutualFundTests {

        @Test
        @DisplayName("Should extract MF NAV")
        void processRegex_ShouldExtractMfNav() {
            // Arrange
            request.setRegexPattern("NAV:\\s*Rs\\.?\\s*(?<mfNav>[\\d.]+)");
            request.setRawMsg("MF Purchase. NAV: Rs. 125.50");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getMfNav());
            assertEquals("125.50", response.getMfNav().getValue());
        }

        @Test
        @DisplayName("Should extract MF units")
        void processRegex_ShouldExtractMfUnits() {
            // Arrange
            request.setRegexPattern("Units:\\s*(?<mfUnits>[\\d.]+)");
            request.setRawMsg("MF Purchase. Units: 79.68 allotted");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getMfUnits());
            assertEquals("79.68", response.getMfUnits().getValue());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle invalid regex pattern gracefully")
        void processRegex_WithInvalidPattern_ShouldReturnEmptyResponse() {
            // Arrange
            request.setRegexPattern("[invalid(regex");
            request.setRawMsg("Some message");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            // Should return empty response without throwing exception
        }

        @Test
        @DisplayName("Should handle empty message")
        void processRegex_WithEmptyMessage_ShouldReturnEmptyResponse() {
            // Arrange
            request.setRegexPattern("(?<amount>\\d+)");
            request.setRawMsg("");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNull(response.getAmount().getValue());
        }

        @Test
        @DisplayName("Should handle null values in request")
        void processRegex_WithNullMessage_ShouldReturnEmptyResponse() {
            // Arrange
            request.setRegexPattern("(?<amount>\\d+)");
            request.setRawMsg(null);

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
        }

        @Test
        @DisplayName("Should handle non-capturing groups correctly")
        void processRegex_WithNonCapturingGroups_ShouldWork() {
            // Arrange
            request.setRegexPattern("(?:Rs\\.?\\s*)(?<amount>[\\d,]+)");
            request.setRawMsg("Debited Rs. 1500");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getAmount());
            assertEquals("1500", response.getAmount().getValue());
        }

        @Test
        @DisplayName("Should handle escaped characters in pattern")
        void processRegex_WithEscapedCharacters_ShouldWork() {
            // Arrange
            request.setRegexPattern("Rs\\.\\s*(?<amount>[\\d,]+\\.\\d{2})");
            request.setRawMsg("Amount: Rs. 1,234.56");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getAmount());
            assertEquals("1,234.56", response.getAmount().getValue());
        }

        @Test
        @DisplayName("Should extract city from SMS")
        void processRegex_ShouldExtractCity() {
            // Arrange
            request.setRegexPattern("at\\s*(?<city>[A-Za-z]+)");
            request.setRawMsg("Transaction at Mumbai for Rs.500");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getCity());
            assertEquals("Mumbai", response.getCity().getValue());
        }

        @Test
        @DisplayName("Should extract payment type")
        void processRegex_ShouldExtractPaymentType() {
            // Arrange
            request.setRegexPattern("via\\s*(?<paymentType>[A-Za-z]+)");
            request.setRawMsg("Paid Rs.500 via UPI");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getPaymentType());
            assertEquals("UPI", response.getPaymentType().getValue());
        }
    }

    @Nested
    @DisplayName("Complex Pattern Tests")
    class ComplexPatternTests {

        @Test
        @DisplayName("Should extract multiple fields from complex SMS")
        void processRegex_ComplexSms_ShouldExtractMultipleFields() {
            // Arrange
            String pattern = "A/c\\s*(?<bankAcId>\\d+).*debited.*Rs\\.?\\s*(?<amount>[\\d,]+).*Bal:\\s*Rs\\.?\\s*(?<balance>[\\d,]+)";
            request.setRegexPattern(pattern);
            request.setRawMsg("Your A/c 1234567890 is debited for Rs. 5,000. Bal: Rs. 45,000");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getBankAcId());
            assertEquals("1234567890", response.getBankAcId().getValue());
            assertNotNull(response.getAmount());
            assertEquals("5,000", response.getAmount().getValue());
            assertNotNull(response.getBalance());
            assertEquals("45,000", response.getBalance().getValue());
        }

        @Test
        @DisplayName("Should handle real-world bank SMS format")
        void processRegex_RealWorldBankSms_ShouldExtractFields() {
            // Arrange
            String pattern = "(?<amount>[\\d,]+\\.?\\d*)\\s*debited.*A/c\\s*(?<bankAcId>XX\\d+).*(?<date>\\d{2}-\\w{3}-\\d{2})";
            request.setRegexPattern(pattern);
            request.setRawMsg("INR 2,500.00 debited from A/c XX1234 on 15-Jan-24");

            // Act
            RegexProcessResponse response = regexProcessService.processRegex(request);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getAmount());
            assertEquals("2,500.00", response.getAmount().getValue());
            assertNotNull(response.getBankAcId());
            assertEquals("XX1234", response.getBankAcId().getValue());
            assertNotNull(response.getDate());
            assertEquals("15-Jan-24", response.getDate().getValue());
        }
    }
}

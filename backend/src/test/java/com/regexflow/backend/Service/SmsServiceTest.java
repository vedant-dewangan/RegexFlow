package com.regexflow.backend.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.regexflow.backend.Dto.*;
import com.regexflow.backend.Entity.RegexTemplate;
import com.regexflow.backend.Entity.Sms;
import com.regexflow.backend.Entity.TemplateRequestNotification;
import com.regexflow.backend.Entity.Users;
import com.regexflow.backend.Enums.*;
import com.regexflow.backend.Repository.RegexTemplateRepository;
import com.regexflow.backend.Repository.SmsRepository;
import com.regexflow.backend.Repository.TemplateRequestNotificationRepository;
import com.regexflow.backend.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SmsService Tests")
class SmsServiceTest {

    @Mock
    private SmsRepository smsRepository;

    @Mock
    private RegexTemplateRepository regexTemplateRepository;

    @Mock
    private TemplateRequestNotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RegexProcessService regexProcessService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private SmsService smsService;

    private Users testUser;
    private RegexTemplate testTemplate;
    private Sms testSms;

    @BeforeEach
    void setUp() {
        testUser = new Users();
        testUser.setUId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setRole(UserRole.CUSTOMER);

        testTemplate = new RegexTemplate();
        testTemplate.setTemplateId(1L);
        testTemplate.setSenderHeader("TESTBK");
        testTemplate.setPattern("Rs\\.?\\s*(?<amount>[\\d,]+)");
        testTemplate.setSampleRawMsg("TESTBK: Rs.1000 debited");
        testTemplate.setSmsType(SmsType.DEBIT);
        testTemplate.setTransactionType(TransactionType.UPI_DEBIT);
        testTemplate.setPaymentType(PaymentType.UPI);
        testTemplate.setStatus(RegexTemplateStatus.VERIFIED);

        testSms = new Sms();
        testSms.setSmsId(1L);
        testSms.setSmsText("TESTBK: Rs.1000 debited from your account");
        testSms.setSenderHeader("TESTBK");
        testSms.setUser(testUser);
        testSms.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Process SMS Tests")
    class ProcessSmsTests {

        @Test
        @DisplayName("Should process SMS with matching template successfully")
        void processSms_WithMatchingTemplate_ShouldReturnSuccessResponse() {
            // Arrange
            String smsText = "TESTBK: Rs.1000 debited from your account";
            
            RegexProcessResponse processResponse = new RegexProcessResponse();
            processResponse.setAmount(new FieldResult("1000", 1));

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(regexTemplateRepository.findBySenderHeaderAndStatus("TESTBK", RegexTemplateStatus.VERIFIED))
                .thenReturn(Collections.singletonList(testTemplate));
            when(regexProcessService.processRegex(any(RegexProcessRequest.class))).thenReturn(processResponse);
            when(smsRepository.save(any(Sms.class))).thenAnswer(invocation -> {
                Sms sms = invocation.getArgument(0);
                sms.setSmsId(1L);
                sms.setCreatedAt(LocalDateTime.now());
                return sms;
            });

            // Act
            SmsSubmissionResponse response = smsService.processSms(smsText, 1L);

            // Assert
            assertNotNull(response);
            assertTrue(response.getHasMatch());
            assertEquals(testTemplate.getTemplateId(), response.getMatchedTemplateId());
            assertEquals("Template matched successfully", response.getMessage());
            assertNotNull(response.getExtractedFields());
            verify(smsRepository).save(any(Sms.class));
        }

        @Test
        @DisplayName("Should create notification when no template found")
        void processSms_WithNoMatchingTemplate_ShouldCreateNotification() {
            // Arrange
            String smsText = "UNKNOWN: Rs.500 debited";

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(regexTemplateRepository.findBySenderHeaderAndStatus("UNKNOWN", RegexTemplateStatus.VERIFIED))
                .thenReturn(Collections.emptyList());
            when(smsRepository.save(any(Sms.class))).thenAnswer(invocation -> {
                Sms sms = invocation.getArgument(0);
                sms.setSmsId(1L);
                sms.setCreatedAt(LocalDateTime.now());
                return sms;
            });
            when(notificationRepository.save(any(TemplateRequestNotification.class)))
                .thenReturn(new TemplateRequestNotification());

            // Act
            SmsSubmissionResponse response = smsService.processSms(smsText, 1L);

            // Assert
            assertNotNull(response);
            assertFalse(response.getHasMatch());
            assertTrue(response.getMessage().contains("No available template"));
            verify(notificationRepository).save(any(TemplateRequestNotification.class));
        }

        @Test
        @DisplayName("Should create notification when template pattern does not match")
        void processSms_WhenPatternDoesNotMatch_ShouldCreateNotification() {
            // Arrange
            String smsText = "TESTBK: Some unmatched message";
            
            RegexProcessResponse emptyResponse = new RegexProcessResponse();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(regexTemplateRepository.findBySenderHeaderAndStatus("TESTBK", RegexTemplateStatus.VERIFIED))
                .thenReturn(Collections.singletonList(testTemplate));
            when(regexProcessService.processRegex(any(RegexProcessRequest.class))).thenReturn(emptyResponse);
            when(smsRepository.save(any(Sms.class))).thenAnswer(invocation -> {
                Sms sms = invocation.getArgument(0);
                sms.setSmsId(1L);
                sms.setCreatedAt(LocalDateTime.now());
                return sms;
            });
            when(notificationRepository.save(any(TemplateRequestNotification.class)))
                .thenReturn(new TemplateRequestNotification());

            // Act
            SmsSubmissionResponse response = smsService.processSms(smsText, 1L);

            // Assert
            assertNotNull(response);
            assertFalse(response.getHasMatch());
            assertTrue(response.getMessage().contains("No template matched"));
            verify(notificationRepository).save(any(TemplateRequestNotification.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void processSms_WhenUserNotFound_ShouldThrowException() {
            // Arrange
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> smsService.processSms("TESTBK: Rs.1000", 999L));
            assertEquals("User not found with id: 999", exception.getMessage());
        }

        @Test
        @DisplayName("Should select best matching template when multiple templates exist")
        void processSms_WithMultipleTemplates_ShouldSelectBestMatch() {
            // Arrange
            String smsText = "TESTBK: Rs.1000 debited. Bal: Rs.5000";

            RegexTemplate template2 = new RegexTemplate();
            template2.setTemplateId(2L);
            template2.setSenderHeader("TESTBK");
            template2.setPattern("Rs\\.?\\s*(?<amount>[\\d,]+).*Bal:\\s*Rs\\.?\\s*(?<balance>[\\d,]+)");
            template2.setSmsType(SmsType.DEBIT);
            template2.setTransactionType(TransactionType.UPI_DEBIT);
            template2.setPaymentType(PaymentType.UPI);
            template2.setStatus(RegexTemplateStatus.VERIFIED);

            RegexProcessResponse response1 = new RegexProcessResponse();
            response1.setAmount(new FieldResult("1000", 1));

            RegexProcessResponse response2 = new RegexProcessResponse();
            response2.setAmount(new FieldResult("1000", 1));
            response2.setBalance(new FieldResult("5000", 2));

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(regexTemplateRepository.findBySenderHeaderAndStatus("TESTBK", RegexTemplateStatus.VERIFIED))
                .thenReturn(Arrays.asList(testTemplate, template2));
            when(regexProcessService.processRegex(any(RegexProcessRequest.class)))
                .thenReturn(response1)
                .thenReturn(response2);
            when(smsRepository.save(any(Sms.class))).thenAnswer(invocation -> {
                Sms sms = invocation.getArgument(0);
                sms.setSmsId(1L);
                sms.setCreatedAt(LocalDateTime.now());
                return sms;
            });

            // Act
            SmsSubmissionResponse response = smsService.processSms(smsText, 1L);

            // Assert
            assertNotNull(response);
            assertTrue(response.getHasMatch());
            assertEquals(template2.getTemplateId(), response.getMatchedTemplateId());
        }

        @Test
        @DisplayName("Should extract sender header correctly with colon")
        void processSms_ShouldExtractSenderHeaderWithColon() {
            // Arrange
            String smsText = "HDFC-BANK: Your account debited";

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(regexTemplateRepository.findBySenderHeaderAndStatus("HDFC-BANK", RegexTemplateStatus.VERIFIED))
                .thenReturn(Collections.emptyList());
            when(smsRepository.save(any(Sms.class))).thenAnswer(invocation -> {
                Sms sms = invocation.getArgument(0);
                assertEquals("HDFC-BANK", sms.getSenderHeader());
                sms.setSmsId(1L);
                sms.setCreatedAt(LocalDateTime.now());
                return sms;
            });
            when(notificationRepository.save(any(TemplateRequestNotification.class)))
                .thenReturn(new TemplateRequestNotification());

            // Act
            SmsSubmissionResponse response = smsService.processSms(smsText, 1L);

            // Assert
            assertNotNull(response);
            verify(regexTemplateRepository).findBySenderHeaderAndStatus("HDFC-BANK", RegexTemplateStatus.VERIFIED);
        }

        @Test
        @DisplayName("Should handle SMS without colon")
        void processSms_WithoutColon_ShouldUseFirstWord() {
            // Arrange
            String smsText = "TESTBANK Your account debited";

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(regexTemplateRepository.findBySenderHeaderAndStatus("TESTBANK", RegexTemplateStatus.VERIFIED))
                .thenReturn(Collections.emptyList());
            when(smsRepository.save(any(Sms.class))).thenAnswer(invocation -> {
                Sms sms = invocation.getArgument(0);
                sms.setSmsId(1L);
                sms.setCreatedAt(LocalDateTime.now());
                return sms;
            });
            when(notificationRepository.save(any(TemplateRequestNotification.class)))
                .thenReturn(new TemplateRequestNotification());

            // Act
            SmsSubmissionResponse response = smsService.processSms(smsText, 1L);

            // Assert
            assertNotNull(response);
        }

        @Test
        @DisplayName("Should handle empty SMS text")
        void processSms_WithEmptyText_ShouldHandleGracefully() {
            // Arrange
            String smsText = "";

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(regexTemplateRepository.findBySenderHeaderAndStatus("", RegexTemplateStatus.VERIFIED))
                .thenReturn(Collections.emptyList());
            when(smsRepository.save(any(Sms.class))).thenAnswer(invocation -> {
                Sms sms = invocation.getArgument(0);
                sms.setSmsId(1L);
                sms.setCreatedAt(LocalDateTime.now());
                return sms;
            });
            when(notificationRepository.save(any(TemplateRequestNotification.class)))
                .thenReturn(new TemplateRequestNotification());

            // Act
            SmsSubmissionResponse response = smsService.processSms(smsText, 1L);

            // Assert
            assertNotNull(response);
            assertFalse(response.getHasMatch());
        }
    }

    @Nested
    @DisplayName("Get SMS History Tests")
    class GetSmsHistoryTests {

        @Test
        @DisplayName("Should return SMS history for user")
        void getSmsHistory_ShouldReturnUserHistory() {
            // Arrange
            testSms.setMatchedTemplate(testTemplate);
            testSms.setExtractedFields("{\"amount\":\"1000\"}");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(smsRepository.findByUserOrderByCreatedAtDesc(testUser))
                .thenReturn(Collections.singletonList(testSms));

            // Act
            List<SmsSubmissionResponse> result = smsService.getSmsHistory(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).getHasMatch());
            assertEquals(testSms.getSmsId(), result.get(0).getSmsId());
        }

        @Test
        @DisplayName("Should return empty list when no SMS history")
        void getSmsHistory_WhenNoHistory_ShouldReturnEmptyList() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(smsRepository.findByUserOrderByCreatedAtDesc(testUser))
                .thenReturn(Collections.emptyList());

            // Act
            List<SmsSubmissionResponse> result = smsService.getSmsHistory(1L);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void getSmsHistory_WhenUserNotFound_ShouldThrowException() {
            // Arrange
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> smsService.getSmsHistory(999L));
            assertEquals("User not found with id: 999", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle SMS without matched template")
        void getSmsHistory_WithUnmatchedSms_ShouldReturnCorrectly() {
            // Arrange
            testSms.setMatchedTemplate(null);
            testSms.setExtractedFields(null);

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(smsRepository.findByUserOrderByCreatedAtDesc(testUser))
                .thenReturn(Collections.singletonList(testSms));

            // Act
            List<SmsSubmissionResponse> result = smsService.getSmsHistory(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertFalse(result.get(0).getHasMatch());
            assertNull(result.get(0).getMatchedTemplateId());
        }

        @Test
        @DisplayName("Should parse extracted fields correctly")
        void getSmsHistory_ShouldParseExtractedFields() {
            // Arrange
            testSms.setMatchedTemplate(testTemplate);
            testSms.setExtractedFields("{\"amount\":\"1000\",\"date\":\"15/01/2024\",\"merchant\":\"Amazon\",\"balance\":\"5000\"}");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(smsRepository.findByUserOrderByCreatedAtDesc(testUser))
                .thenReturn(Collections.singletonList(testSms));

            // Act
            List<SmsSubmissionResponse> result = smsService.getSmsHistory(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            ExtractedFieldsDto extractedFields = result.get(0).getExtractedFields();
            assertNotNull(extractedFields);
            assertEquals("1000", extractedFields.getAmount());
            assertEquals("15/01/2024", extractedFields.getDate());
            assertEquals("Amazon", extractedFields.getMerchant());
            assertEquals("5000", extractedFields.getBalance());
        }

        @Test
        @DisplayName("Should handle invalid JSON in extracted fields")
        void getSmsHistory_WithInvalidJson_ShouldHandleGracefully() {
            // Arrange
            testSms.setMatchedTemplate(testTemplate);
            testSms.setExtractedFields("invalid json");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(smsRepository.findByUserOrderByCreatedAtDesc(testUser))
                .thenReturn(Collections.singletonList(testSms));

            // Act
            List<SmsSubmissionResponse> result = smsService.getSmsHistory(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            // Should have empty extracted fields due to JSON parsing error
        }
    }

    @Nested
    @DisplayName("Get Pending Notifications Tests")
    class GetPendingNotificationsTests {

        @Test
        @DisplayName("Should return pending notifications")
        void getPendingNotifications_ShouldReturnPendingNotifications() {
            // Arrange
            TemplateRequestNotification notification = new TemplateRequestNotification();
            notification.setNotificationId(1L);
            notification.setSms(testSms);
            notification.setSmsText("TESTBK: Rs.1000");
            notification.setSenderHeader("TESTBK");
            notification.setRequestedBy(testUser);
            notification.setStatus(NotificationStatus.PENDING);
            notification.setCreatedAt(LocalDateTime.now());

            when(notificationRepository.findByStatusOrderByCreatedAtDesc(NotificationStatus.PENDING))
                .thenReturn(Collections.singletonList(notification));

            // Act
            List<TemplateRequestNotificationDto> result = smsService.getPendingNotifications();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(notification.getNotificationId(), result.get(0).getNotificationId());
            assertEquals(notification.getSmsText(), result.get(0).getSmsText());
            assertEquals(notification.getSenderHeader(), result.get(0).getSenderHeader());
            assertEquals(NotificationStatus.PENDING, result.get(0).getStatus());
        }

        @Test
        @DisplayName("Should return empty list when no pending notifications")
        void getPendingNotifications_WhenNoPending_ShouldReturnEmptyList() {
            // Arrange
            when(notificationRepository.findByStatusOrderByCreatedAtDesc(NotificationStatus.PENDING))
                .thenReturn(Collections.emptyList());

            // Act
            List<TemplateRequestNotificationDto> result = smsService.getPendingNotifications();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Mark Notification As Resolved Tests")
    class MarkNotificationAsResolvedTests {

        @Test
        @DisplayName("Should mark notification as resolved")
        void markNotificationAsResolved_ShouldUpdateStatus() {
            // Arrange
            TemplateRequestNotification notification = new TemplateRequestNotification();
            notification.setNotificationId(1L);
            notification.setStatus(NotificationStatus.PENDING);

            when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
            when(notificationRepository.save(any(TemplateRequestNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            smsService.markNotificationAsResolved(1L);

            // Assert
            verify(notificationRepository).save(argThat(n -> 
                n.getStatus() == NotificationStatus.RESOLVED && n.getResolvedAt() != null
            ));
        }

        @Test
        @DisplayName("Should throw exception when notification not found")
        void markNotificationAsResolved_WhenNotFound_ShouldThrowException() {
            // Arrange
            when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> smsService.markNotificationAsResolved(999L));
            assertEquals("Notification not found with id: 999", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Transaction Type Detection Tests")
    class TransactionTypeDetectionTests {

        @Test
        @DisplayName("Should detect DEBIT transaction type from template")
        void processSms_ShouldDetectDebitFromTemplate() {
            // Arrange
            String smsText = "TESTBK: Rs.1000 debited";
            testTemplate.setSmsType(SmsType.DEBIT);

            RegexProcessResponse processResponse = new RegexProcessResponse();
            processResponse.setAmount(new FieldResult("1000", 1));

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(regexTemplateRepository.findBySenderHeaderAndStatus("TESTBK", RegexTemplateStatus.VERIFIED))
                .thenReturn(Collections.singletonList(testTemplate));
            when(regexProcessService.processRegex(any(RegexProcessRequest.class))).thenReturn(processResponse);
            when(smsRepository.save(any(Sms.class))).thenAnswer(invocation -> {
                Sms sms = invocation.getArgument(0);
                sms.setSmsId(1L);
                sms.setCreatedAt(LocalDateTime.now());
                return sms;
            });

            // Act
            SmsSubmissionResponse response = smsService.processSms(smsText, 1L);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getExtractedFields());
            assertEquals("DEBIT", response.getExtractedFields().getSmsType());
        }

        @Test
        @DisplayName("Should detect CREDIT transaction type from template")
        void processSms_ShouldDetectCreditFromTemplate() {
            // Arrange
            String smsText = "TESTBK: Rs.1000 credited";
            testTemplate.setSmsType(SmsType.CREDIT);

            RegexProcessResponse processResponse = new RegexProcessResponse();
            processResponse.setAmount(new FieldResult("1000", 1));

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(regexTemplateRepository.findBySenderHeaderAndStatus("TESTBK", RegexTemplateStatus.VERIFIED))
                .thenReturn(Collections.singletonList(testTemplate));
            when(regexProcessService.processRegex(any(RegexProcessRequest.class))).thenReturn(processResponse);
            when(smsRepository.save(any(Sms.class))).thenAnswer(invocation -> {
                Sms sms = invocation.getArgument(0);
                sms.setSmsId(1L);
                sms.setCreatedAt(LocalDateTime.now());
                return sms;
            });

            // Act
            SmsSubmissionResponse response = smsService.processSms(smsText, 1L);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getExtractedFields());
            assertEquals("CREDIT", response.getExtractedFields().getSmsType());
        }

        @Test
        @DisplayName("Should detect LOAN transaction type from template")
        void processSms_ShouldDetectLoanFromTemplate() {
            // Arrange
            String smsText = "TESTBK: EMI Rs.5000 debited";
            testTemplate.setSmsType(SmsType.LOAN);

            RegexProcessResponse processResponse = new RegexProcessResponse();
            processResponse.setAmount(new FieldResult("5000", 1));

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(regexTemplateRepository.findBySenderHeaderAndStatus("TESTBK", RegexTemplateStatus.VERIFIED))
                .thenReturn(Collections.singletonList(testTemplate));
            when(regexProcessService.processRegex(any(RegexProcessRequest.class))).thenReturn(processResponse);
            when(smsRepository.save(any(Sms.class))).thenAnswer(invocation -> {
                Sms sms = invocation.getArgument(0);
                sms.setSmsId(1L);
                sms.setCreatedAt(LocalDateTime.now());
                return sms;
            });

            // Act
            SmsSubmissionResponse response = smsService.processSms(smsText, 1L);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getExtractedFields());
            assertEquals("LOAN", response.getExtractedFields().getSmsType());
        }
    }
}

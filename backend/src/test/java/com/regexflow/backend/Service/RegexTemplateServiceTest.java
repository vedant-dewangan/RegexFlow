package com.regexflow.backend.Service;

import com.regexflow.backend.Dto.RegexTemplateDto;
import com.regexflow.backend.Entity.Bank;
import com.regexflow.backend.Entity.RegexTemplate;
import com.regexflow.backend.Entity.Users;
import com.regexflow.backend.Enums.*;
import com.regexflow.backend.Repository.BankRepository;
import com.regexflow.backend.Repository.RegexTemplateRepository;
import com.regexflow.backend.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegexTemplateService Tests")
class RegexTemplateServiceTest {

    @Mock
    private RegexTemplateRepository regexTemplateRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BankRepository bankRepository;

    @InjectMocks
    private RegexTemplateService regexTemplateService;

    private Users testUser;
    private Bank testBank;
    private RegexTemplate testTemplate;
    private RegexTemplateDto testTemplateDto;

    @BeforeEach
    void setUp() {
        testUser = new Users();
        testUser.setUId(1L);
        testUser.setName("Test Maker");
        testUser.setEmail("maker@test.com");
        testUser.setRole(UserRole.MAKER);

        testBank = new Bank();
        testBank.setBId(1L);
        testBank.setName("Test Bank");
        testBank.setAddress("123 Test Street");

        testTemplate = new RegexTemplate();
        testTemplate.setTemplateId(1L);
        testTemplate.setSenderHeader("TESTBK");
        testTemplate.setPattern("(?<amount>\\d+)");
        testTemplate.setSampleRawMsg("TESTBK: Rs.1000 debited");
        testTemplate.setSmsType(SmsType.DEBIT);
        testTemplate.setTransactionType(TransactionType.UPI_DEBIT);
        testTemplate.setPaymentType(PaymentType.UPI);
        testTemplate.setStatus(RegexTemplateStatus.DRAFT);
        testTemplate.setCreatedBy(testUser);
        testTemplate.setBank(testBank);
        testTemplate.setCreatedAt(LocalDateTime.now());

        testTemplateDto = new RegexTemplateDto();
        testTemplateDto.setSenderHeader("TESTBK");
        testTemplateDto.setPattern("(?<amount>\\d+)");
        testTemplateDto.setSampleRawMsg("TESTBK: Rs.1000 debited");
        testTemplateDto.setSmsType(SmsType.DEBIT);
        testTemplateDto.setTransactionType(TransactionType.UPI_DEBIT);
        testTemplateDto.setPaymentType(PaymentType.UPI);
        testTemplateDto.setBankId(1L);
    }

    @Nested
    @DisplayName("Get All Templates Tests")
    class GetAllTemplatesTests {

        @Test
        @DisplayName("Should return all templates")
        void getAllRegexTemplates_ShouldReturnAllTemplates() {
            // Arrange
            RegexTemplate template2 = new RegexTemplate();
            template2.setTemplateId(2L);
            template2.setSenderHeader("BANK2");
            template2.setPattern("(?<balance>\\d+)");
            template2.setSampleRawMsg("BANK2: Balance Rs.5000");
            template2.setSmsType(SmsType.CREDIT);
            template2.setTransactionType(TransactionType.UPI_CREDIT);
            template2.setPaymentType(PaymentType.UPI);
            template2.setStatus(RegexTemplateStatus.VERIFIED);
            template2.setCreatedBy(testUser);
            template2.setBank(testBank);

            when(regexTemplateRepository.findAll()).thenReturn(Arrays.asList(testTemplate, template2));

            // Act
            List<RegexTemplateDto> result = regexTemplateService.getAllRegexTemplates();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(regexTemplateRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no templates exist")
        void getAllRegexTemplates_WhenNoTemplates_ShouldReturnEmptyList() {
            // Arrange
            when(regexTemplateRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<RegexTemplateDto> result = regexTemplateService.getAllRegexTemplates();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Get Templates By Maker ID Tests")
    class GetTemplatesByMakerIdTests {

        @Test
        @DisplayName("Should return templates for specific maker")
        void getTemplatesByMakerId_ShouldReturnMakerTemplates() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(regexTemplateRepository.findByCreatedBy(testUser)).thenReturn(Collections.singletonList(testTemplate));

            // Act
            List<RegexTemplateDto> result = regexTemplateService.getTemplatesByMakerId(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testTemplate.getSenderHeader(), result.get(0).getSenderHeader());
            verify(userRepository).findById(1L);
            verify(regexTemplateRepository).findByCreatedBy(testUser);
        }

        @Test
        @DisplayName("Should throw exception when maker not found")
        void getTemplatesByMakerId_WhenMakerNotFound_ShouldThrowException() {
            // Arrange
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> regexTemplateService.getTemplatesByMakerId(999L));
            assertEquals("Maker not found with id: 999", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Save As Draft Tests")
    class SaveAsDraftTests {

        @Test
        @DisplayName("Should save template as draft successfully")
        void saveAsDraft_WithValidData_ShouldReturnSavedTemplate() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(bankRepository.findById(1L)).thenReturn(Optional.of(testBank));
            when(regexTemplateRepository.findBySenderHeaderAndPatternAndBankAndSmsTypeAndTransactionTypeAndPaymentTypeAndStatus(
                any(), any(), any(), any(), any(), any(), any())).thenReturn(Optional.empty());
            when(regexTemplateRepository.save(any(RegexTemplate.class))).thenAnswer(invocation -> {
                RegexTemplate saved = invocation.getArgument(0);
                saved.setTemplateId(1L);
                return saved;
            });

            // Act
            RegexTemplateDto result = regexTemplateService.saveAsDraft(testTemplateDto, 1L);

            // Assert
            assertNotNull(result);
            assertEquals(RegexTemplateStatus.DRAFT, result.getStatus());
            verify(regexTemplateRepository).save(any(RegexTemplate.class));
        }

        @Test
        @DisplayName("Should throw exception when sender header is blank")
        void saveAsDraft_WithBlankSenderHeader_ShouldThrowException() {
            // Arrange
            testTemplateDto.setSenderHeader("");

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> regexTemplateService.saveAsDraft(testTemplateDto, 1L));
            assertEquals("Sender header is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when sender header is null")
        void saveAsDraft_WithNullSenderHeader_ShouldThrowException() {
            // Arrange
            testTemplateDto.setSenderHeader(null);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> regexTemplateService.saveAsDraft(testTemplateDto, 1L));
            assertEquals("Sender header is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when pattern is blank")
        void saveAsDraft_WithBlankPattern_ShouldThrowException() {
            // Arrange
            testTemplateDto.setPattern("");

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> regexTemplateService.saveAsDraft(testTemplateDto, 1L));
            assertEquals("Pattern is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when bank ID is null")
        void saveAsDraft_WithNullBankId_ShouldThrowException() {
            // Arrange
            testTemplateDto.setBankId(null);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> regexTemplateService.saveAsDraft(testTemplateDto, 1L));
            assertEquals("Bank ID is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when SMS type is null")
        void saveAsDraft_WithNullSmsType_ShouldThrowException() {
            // Arrange
            testTemplateDto.setSmsType(null);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> regexTemplateService.saveAsDraft(testTemplateDto, 1L));
            assertEquals("SMS type is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when transaction type is null")
        void saveAsDraft_WithNullTransactionType_ShouldThrowException() {
            // Arrange
            testTemplateDto.setTransactionType(null);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> regexTemplateService.saveAsDraft(testTemplateDto, 1L));
            assertEquals("Transaction type is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when payment type is null")
        void saveAsDraft_WithNullPaymentType_ShouldThrowException() {
            // Arrange
            testTemplateDto.setPaymentType(null);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> regexTemplateService.saveAsDraft(testTemplateDto, 1L));
            assertEquals("Payment type is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void saveAsDraft_WhenUserNotFound_ShouldThrowException() {
            // Arrange
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> regexTemplateService.saveAsDraft(testTemplateDto, 999L));
            assertEquals("User not found with id: 999", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when bank not found")
        void saveAsDraft_WhenBankNotFound_ShouldThrowException() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(bankRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> regexTemplateService.saveAsDraft(testTemplateDto, 1L));
            assertEquals("Bank not found with id: 1", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when duplicate draft exists")
        void saveAsDraft_WhenDuplicateDraftExists_ShouldThrowException() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(bankRepository.findById(1L)).thenReturn(Optional.of(testBank));
            when(regexTemplateRepository.findBySenderHeaderAndPatternAndBankAndSmsTypeAndTransactionTypeAndPaymentTypeAndStatus(
                any(), any(), any(), any(), any(), any(), any())).thenReturn(Optional.of(testTemplate));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> regexTemplateService.saveAsDraft(testTemplateDto, 1L));
            assertTrue(exception.getMessage().contains("A draft template with the same pattern"));
        }
    }

    @Nested
    @DisplayName("Update To Pending Tests")
    class UpdateToPendingTests {

        @Test
        @DisplayName("Should update draft template to pending successfully")
        void updateToPending_WithValidData_ShouldReturnUpdatedTemplate() {
            // Arrange
            testTemplateDto.setSampleRawMsg("TESTBK: Rs.1000 debited from account");
            when(regexTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
            when(bankRepository.findById(1L)).thenReturn(Optional.of(testBank));
            when(regexTemplateRepository.save(any(RegexTemplate.class))).thenAnswer(invocation -> {
                RegexTemplate saved = invocation.getArgument(0);
                return saved;
            });

            // Act
            RegexTemplateDto result = regexTemplateService.updateToPending(1L, testTemplateDto, 1L);

            // Assert
            assertNotNull(result);
            assertEquals(RegexTemplateStatus.PENDING, result.getStatus());
            verify(regexTemplateRepository).save(any(RegexTemplate.class));
        }

        @Test
        @DisplayName("Should throw exception when template not found")
        void updateToPending_WhenTemplateNotFound_ShouldThrowException() {
            // Arrange
            when(regexTemplateRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> regexTemplateService.updateToPending(999L, testTemplateDto, 1L));
            assertEquals("Template not found with id: 999", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when user is not the creator")
        void updateToPending_WhenUserNotCreator_ShouldThrowException() {
            // Arrange
            when(regexTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> regexTemplateService.updateToPending(1L, testTemplateDto, 999L));
            assertEquals("You can only update templates created by you", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when template is not in DRAFT status")
        void updateToPending_WhenNotDraft_ShouldThrowException() {
            // Arrange
            testTemplate.setStatus(RegexTemplateStatus.VERIFIED);
            when(regexTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> regexTemplateService.updateToPending(1L, testTemplateDto, 1L));
            assertEquals("Only DRAFT templates can be updated to PENDING status", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when sample raw message is blank")
        void updateToPending_WithBlankSampleRawMsg_ShouldThrowException() {
            // Arrange
            testTemplateDto.setSampleRawMsg("");

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> regexTemplateService.updateToPending(1L, testTemplateDto, 1L));
            assertEquals("Sample Raw Message is required and cannot be empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when sample raw message is null")
        void updateToPending_WithNullSampleRawMsg_ShouldThrowException() {
            // Arrange
            testTemplateDto.setSampleRawMsg(null);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> regexTemplateService.updateToPending(1L, testTemplateDto, 1L));
            assertEquals("Sample Raw Message is required and cannot be empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when sender header is blank")
        void updateToPending_WithBlankSenderHeader_ShouldThrowException() {
            // Arrange
            testTemplateDto.setSenderHeader("");

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> regexTemplateService.updateToPending(1L, testTemplateDto, 1L));
            assertEquals("Sender header is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when pattern is blank")
        void updateToPending_WithBlankPattern_ShouldThrowException() {
            // Arrange
            testTemplateDto.setPattern("");

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> regexTemplateService.updateToPending(1L, testTemplateDto, 1L));
            assertEquals("Pattern is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when bank not found during update")
        void updateToPending_WhenBankNotFound_ShouldThrowException() {
            // Arrange
            testTemplateDto.setSampleRawMsg("Sample message");
            when(regexTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
            when(bankRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> regexTemplateService.updateToPending(1L, testTemplateDto, 1L));
            assertEquals("Bank not found with id: 1", exception.getMessage());
        }

        @Test
        @DisplayName("Should update all fields correctly")
        void updateToPending_ShouldUpdateAllFields() {
            // Arrange
            testTemplateDto.setSampleRawMsg("Updated sample message");
            testTemplateDto.setSenderHeader("NEWHEADER");
            testTemplateDto.setPattern("(?<newAmount>\\d+)");
            testTemplateDto.setSmsType(SmsType.CREDIT);
            testTemplateDto.setTransactionType(TransactionType.UPI_CREDIT);
            testTemplateDto.setPaymentType(PaymentType.NET_BANKING);

            when(regexTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
            when(bankRepository.findById(1L)).thenReturn(Optional.of(testBank));
            when(regexTemplateRepository.save(any(RegexTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            RegexTemplateDto result = regexTemplateService.updateToPending(1L, testTemplateDto, 1L);

            // Assert
            assertNotNull(result);
            assertEquals("NEWHEADER", result.getSenderHeader());
            assertEquals("(?<newAmount>\\d+)", result.getPattern());
            assertEquals("Updated sample message", result.getSampleRawMsg());
            assertEquals(SmsType.CREDIT, result.getSmsType());
            assertEquals(TransactionType.UPI_CREDIT, result.getTransactionType());
            assertEquals(PaymentType.NET_BANKING, result.getPaymentType());
            assertEquals(RegexTemplateStatus.PENDING, result.getStatus());
        }
    }
}

package com.regexflow.backend.Service;

import com.regexflow.backend.Dto.BankDto;
import com.regexflow.backend.Entity.Bank;
import com.regexflow.backend.Repository.BankRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BankService Tests")
class BankServiceTest {

    @Mock
    private BankRepository bankRepository;

    @InjectMocks
    private BankService bankService;

    private BankDto bankDto;
    private Bank bank;

    @BeforeEach
    void setUp() {
        bankDto = new BankDto();
        bankDto.setName("Test Bank");
        bankDto.setAddress("123 Test Street");

        bank = new Bank();
        bank.setBId(1L);
        bank.setName("Test Bank");
        bank.setAddress("123 Test Street");
    }

    @Nested
    @DisplayName("Create Bank Tests")
    class CreateBankTests {

        @Test
        @DisplayName("Should create bank successfully with valid data")
        void createBank_WithValidData_ShouldReturnBankDto() {
            // Arrange
            when(bankRepository.existsByAddress(bankDto.getAddress())).thenReturn(false);
            when(bankRepository.existsByName(bankDto.getName())).thenReturn(false);
            when(bankRepository.save(any(Bank.class))).thenReturn(bank);

            // Act
            BankDto result = bankService.createBank(bankDto);

            // Assert
            assertNotNull(result);
            assertEquals(bank.getBId(), result.getBId());
            assertEquals(bank.getName(), result.getName());
            assertEquals(bank.getAddress(), result.getAddress());

            verify(bankRepository).existsByAddress(bankDto.getAddress());
            verify(bankRepository).existsByName(bankDto.getName());
            verify(bankRepository).save(any(Bank.class));
        }

        @Test
        @DisplayName("Should throw exception when bank with same address exists")
        void createBank_WithExistingAddress_ShouldThrowException() {
            // Arrange
            when(bankRepository.existsByAddress(bankDto.getAddress())).thenReturn(true);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> bankService.createBank(bankDto));
            assertEquals("Bank already exists", exception.getMessage());

            verify(bankRepository, never()).save(any(Bank.class));
        }

        @Test
        @DisplayName("Should throw exception when bank with same name exists")
        void createBank_WithExistingName_ShouldThrowException() {
            // Arrange
            when(bankRepository.existsByAddress(bankDto.getAddress())).thenReturn(false);
            when(bankRepository.existsByName(bankDto.getName())).thenReturn(true);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> bankService.createBank(bankDto));
            assertEquals("Bank already exists", exception.getMessage());

            verify(bankRepository, never()).save(any(Bank.class));
        }

        @Test
        @DisplayName("Should set bId to null before saving")
        void createBank_ShouldSetBIdToNullBeforeSaving() {
            // Arrange
            bankDto.setBId(999L); // Set an existing ID
            when(bankRepository.existsByAddress(bankDto.getAddress())).thenReturn(false);
            when(bankRepository.existsByName(bankDto.getName())).thenReturn(false);
            when(bankRepository.save(any(Bank.class))).thenAnswer(invocation -> {
                Bank savedBank = invocation.getArgument(0);
                assertNull(savedBank.getBId()); // Verify bId is null
                savedBank.setBId(1L);
                return savedBank;
            });

            // Act
            BankDto result = bankService.createBank(bankDto);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getBId());
        }
    }

    @Nested
    @DisplayName("Get All Banks Tests")
    class GetAllBanksTests {

        @Test
        @DisplayName("Should return all banks")
        void getAllBanks_ShouldReturnAllBanks() {
            // Arrange
            Bank bank2 = new Bank();
            bank2.setBId(2L);
            bank2.setName("Second Bank");
            bank2.setAddress("456 Second Street");

            List<Bank> banks = Arrays.asList(bank, bank2);
            when(bankRepository.findAll()).thenReturn(banks);

            // Act
            List<BankDto> result = bankService.getAllBanks();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(bank.getName(), result.get(0).getName());
            assertEquals(bank2.getName(), result.get(1).getName());

            verify(bankRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no banks exist")
        void getAllBanks_WhenNoBanksExist_ShouldReturnEmptyList() {
            // Arrange
            when(bankRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<BankDto> result = bankService.getAllBanks();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(bankRepository).findAll();
        }

        @Test
        @DisplayName("Should map all bank fields correctly")
        void getAllBanks_ShouldMapAllFieldsCorrectly() {
            // Arrange
            when(bankRepository.findAll()).thenReturn(Collections.singletonList(bank));

            // Act
            List<BankDto> result = bankService.getAllBanks();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            BankDto resultDto = result.get(0);
            assertEquals(bank.getBId(), resultDto.getBId());
            assertEquals(bank.getName(), resultDto.getName());
            assertEquals(bank.getAddress(), resultDto.getAddress());
        }
    }
}

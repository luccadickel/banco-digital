package br.com.bancodigital.service;

import br.com.bancodigital.controller.request.TransferRequest;
import br.com.bancodigital.controller.response.TransferResponse;
import br.com.bancodigital.domain.Account;
import br.com.bancodigital.domain.Transfer;
import br.com.bancodigital.event.TransferCompletedEvent;
import br.com.bancodigital.exception.AccountNotFoundException;
import br.com.bancodigital.exception.InsufficientBalanceException;
import br.com.bancodigital.exception.SameAccountTransferException;
import br.com.bancodigital.mapper.TransferMapper;
import br.com.bancodigital.repository.AccountRepository;
import br.com.bancodigital.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferService")
public class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransferMapper transferMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TransferService transferService;

    private Account source;
    private Account destination;

    @BeforeEach
    void setup() {
        source = Account.builder().id(1L).balance(new BigDecimal("1000.00")).build();
        destination = Account.builder().id(2L).balance(new BigDecimal("500.00")).build();
    }

    @Test
    @DisplayName("Deve realizar transferência com sucesso quando os dados são válidos")
    void shouldTransferSuccessfully() {
        TransferRequest transferRequest = new TransferRequest(1L, 2L, new BigDecimal("100.00"), "key-001");

        when(transferRepository.findByIdempotencyKey("key-001")).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(destination));
        when(transferRepository.save(any(Transfer.class))).thenReturn(new Transfer());
        when(transferMapper.toEntity(any(), any(), any())).thenReturn(new Transfer());
        when(transferMapper.toResponse(any())).thenReturn(mock(TransferResponse.class));

        transferService.transfer(transferRequest);

        assertThat(source.getBalance()).isEqualByComparingTo("900.00");
        assertThat(destination.getBalance()).isEqualByComparingTo("600.00");
        verify(transferRepository).save(any(Transfer.class));
        verify(eventPublisher).publishEvent(any(TransferCompletedEvent.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando origem e destino são a mesma conta")
    void shouldThrowWhenSameAccount() {
        TransferRequest transferRequest = new TransferRequest(1L, 1L, new BigDecimal("100.00"), "key-002");
        when(transferRepository.findByIdempotencyKey("key-002")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transfer(transferRequest)).isInstanceOf(SameAccountTransferException.class);

        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exeção quando o saldo é insuficiente")
    void shouldThrowWhenInsufficientBalance() {
        TransferRequest transferRequest = new TransferRequest(1L, 2L, new BigDecimal("5000.00"), "key-003");

        when(transferRepository.findByIdempotencyKey("key-003")).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(destination));

        assertThatThrownBy(() -> transferService.transfer(transferRequest)).isInstanceOf(InsufficientBalanceException.class);

        assertThat(source.getBalance()).isEqualByComparingTo("1000.00");
        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exeçãoquando a conta de origem não existe")
    void shouldThrowWhenAccountNotFound() {
        TransferRequest transferRequest = new TransferRequest(1L, 2L, new BigDecimal("100.00"), "key-004");

        when(transferRepository.findByIdempotencyKey("key-004")).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transfer(transferRequest)).isInstanceOf(AccountNotFoundException.class);

        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar a transferencia existente quando a idempotency key já foi processada")
    void shouldReturnExistingTransferWhenIdempotencyKeyAlreadyProcessed() {
        TransferRequest transferRequest = new TransferRequest(1L, 2L, new BigDecimal("100.00"), "key-repeated");
        Transfer existing = new Transfer();

        when(transferRepository.findByIdempotencyKey("key-repeated")).thenReturn(Optional.of(existing));
        when(transferMapper.toResponse(existing)).thenReturn(mock(TransferResponse.class));

        transferService.transfer(transferRequest);

        verify(accountRepository, never()).findByIdForUpdate(any());
        verify(transferRepository, never()).save(any());
    }
}

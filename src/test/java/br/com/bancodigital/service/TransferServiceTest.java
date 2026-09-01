package br.com.bancodigital.service;

import br.com.bancodigital.controller.request.TransferRequest;
import br.com.bancodigital.controller.response.TransferResponse;
import br.com.bancodigital.domain.Account;
import br.com.bancodigital.domain.Transfer;
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

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    }
}

package br.com.bancodigital.service;

import br.com.bancodigital.controller.response.TransferResponse;
import br.com.bancodigital.domain.Transfer;
import br.com.bancodigital.exception.AccountNotFoundException;
import br.com.bancodigital.mapper.TransferMapper;
import br.com.bancodigital.repository.AccountRepository;
import br.com.bancodigital.repository.TransferRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService")
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private TransferMapper transferMapper;

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("Deve retornar as movimentações da conta de forma paginada")
    void shouldReturnPaginatedTransfers() {
        Long accountId = 1L;
        Transfer transfer = new Transfer();
        Page<Transfer> page = new PageImpl<>(List.of(transfer));

        when(accountRepository.existsById(accountId)).thenReturn(true);
        when(transferRepository.findByAccountId(eq(accountId), any(Pageable.class))).thenReturn(page);
        when(transferMapper.toResponse(any(Transfer.class))).thenReturn(mock(TransferResponse.class));

        Page<TransferResponse> result = accountService.listTransfers(accountId, 0, 10);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Deve lançar AccountNotFoundException quando a conta não existe")
    void shouldThrowWhenAccountNotFound() {
        when(accountRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> accountService.listTransfers(999L, 0, 10))
                .isInstanceOf(AccountNotFoundException.class);

        verify(transferRepository, never()).findByAccountId(any(), any());
    }
}

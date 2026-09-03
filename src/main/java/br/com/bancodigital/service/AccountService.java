package br.com.bancodigital.service;

import br.com.bancodigital.controller.response.TransferResponse;
import br.com.bancodigital.domain.Transfer;
import br.com.bancodigital.exception.AccountNotFoundException;
import br.com.bancodigital.mapper.TransferMapper;
import br.com.bancodigital.repository.AccountRepository;
import br.com.bancodigital.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final TransferMapper transferMapper;

    public Page<TransferResponse> listTransfers(Long accountId, int page, int size) {

        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Transfer> transfers = transferRepository.findByAccountId(accountId, pageable);

        return transfers.map(transferMapper::toResponse);
    }
}

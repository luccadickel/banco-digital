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
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final TransferMapper transferMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TransferResponse transfer(TransferRequest transferRequest) {

        var exist = transferRepository.findByIdempotencyKey(transferRequest.idempotencyKey());

        if (exist.isPresent()) {
            return transferMapper.toResponse(exist.get());
        }

        Long sourceId = transferRequest.sourceAccountId();
        Long destinationId = transferRequest.destinationAccountId();

        if (sourceId.equals(destinationId)) {
            throw new SameAccountTransferException();
        }

        Account source;
        Account destination;

        if (sourceId < destinationId) {
            source = getAccountWithLock(sourceId);
            destination = getAccountWithLock(destinationId);
        } else {
            destination = getAccountWithLock(destinationId);
            source = getAccountWithLock(sourceId);
        }

        validateSufficientBalance(source, transferRequest.amount());

        source.setBalance(source.getBalance().subtract(transferRequest.amount()));
        destination.setBalance(destination.getBalance().add(transferRequest.amount()));

        Transfer transfer = transferMapper.toEntity(transferRequest, source, destination);
        Transfer saved = transferRepository.save(transfer);

        eventPublisher.publishEvent(new TransferCompletedEvent(
                source.getId(), destination.getId(), saved.getAmount()));

        return transferMapper.toResponse(saved);
    }

    private void validateSufficientBalance(Account source, BigDecimal amount) {
        if (source.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }
    }

    private Account getAccountWithLock(Long accountId) {
        return accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }
}

package br.com.bancodigital.mapper;

import br.com.bancodigital.controller.request.TransferRequest;
import br.com.bancodigital.controller.response.TransferResponse;
import br.com.bancodigital.domain.Account;
import br.com.bancodigital.domain.Transfer;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TransferMapper {

    public Transfer toEntity(TransferRequest transferRequest, Account source, Account destination) {
        return Transfer.builder()
                .sourceAccount(source)
                .destinationAccount(destination)
                .amount(transferRequest.amount())
                .createdAt(LocalDateTime.now())
                .idempotencyKey(transferRequest.idempotencyKey())
                .build();
    }

    public TransferResponse toResponse(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getSourceAccount().getId(),
                transfer.getDestinationAccount().getId(),
                transfer.getAmount(),
                transfer.getCreatedAt(),
                transfer.getIdempotencyKey()
        );
    }
}

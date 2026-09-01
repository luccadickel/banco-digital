package br.com.bancodigital.controller.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(Long id,
                               Long sourceAccountId,
                               Long destinationAccountId,
                               BigDecimal amount,
                               LocalDateTime createdAt,
                               String idempotencyKey) {
}

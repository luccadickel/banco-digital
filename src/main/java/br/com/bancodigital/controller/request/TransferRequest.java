package br.com.bancodigital.controller.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferRequest(@NotNull Long sourceAccountId,
                              @NotNull Long destinationAccountId,
                              @NotNull @Positive BigDecimal amount,
                              @NotNull String idempotencyKey) {
}

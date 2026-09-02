package br.com.bancodigital.event;

import java.math.BigDecimal;

public record TransferCompletedEvent(Long sourceAccountId, Long destinationAccountId, BigDecimal amount) {
}

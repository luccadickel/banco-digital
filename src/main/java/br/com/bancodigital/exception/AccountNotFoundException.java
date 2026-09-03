package br.com.bancodigital.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(Long accountId) {
        super("Conta não econtrada: " + accountId);
    }
}

package br.com.bancodigital.exception;

public class SameAccountTransferException extends RuntimeException {
    public SameAccountTransferException() {
        super("Não é possível transferir para a mesma conta");
    }
}

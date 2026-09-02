package br.com.bancodigital.listener;

import br.com.bancodigital.event.TransferCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TransferNotificationListener {

    private static final Logger log= LoggerFactory.getLogger(TransferNotificationListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTrasferCompleted(TransferCompletedEvent event) {
        log.info("Notificação: transferência de {} concluída da conta {} para a conta {}",
                event.amount(), event.sourceAccountId(), event.destinationAccountId());
    }
}

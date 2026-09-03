package br.com.bancodigital.concurrency;

import br.com.bancodigital.controller.request.TransferRequest;
import br.com.bancodigital.domain.Account;
import br.com.bancodigital.domain.User;
import br.com.bancodigital.repository.AccountRepository;
import br.com.bancodigital.repository.UserRepository;
import br.com.bancodigital.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class TransferConcurrencyTest {

    @Autowired
    private TransferService transferService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Não deve haver lost update sob transferências concorrentes")
    void shouldHandleConcurrentTransfers() throws InterruptedException {

        User user1 = userRepository.save(User.builder().name("Origem").build());
        User user2 = userRepository.save(User.builder().name("Destino").build());
        Account source = accountRepository.save(
                Account.builder().user(user1).balance(new BigDecimal("1000.00")).build());
        Account destination = accountRepository.save(
                Account.builder().user(user2).balance(new BigDecimal("0.00")).build());

        int numberTransfers = 100;
        BigDecimal amountEach = new BigDecimal("10.00");

        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(numberTransfers);

        String runId = UUID.randomUUID().toString();

        for (int i = 0; i < numberTransfers; i++) {
            final int idx = i;
            executorService.submit(() -> {
                try {
                    transferService.transfer(new TransferRequest(
                            source.getId(), destination.getId(), amountEach, runId + "-" + idx));

                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Account finalSource = accountRepository.findById(source.getId()).orElseThrow();
        Account finalDestination = accountRepository.findById(destination.getId()).orElseThrow();

        assertThat(finalSource.getBalance()).isEqualByComparingTo("0.00");
        assertThat(finalDestination.getBalance()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("Não deve haver deadlock com transferencias concorrentes em direções opostas")
    void shouldNotDeadLockWithBidirectionalTransfers() throws InterruptedException {

        User user1 = userRepository.save(User.builder().name("A").build());
        User user2 = userRepository.save(User.builder().name("B").build());
        Account accountA = accountRepository.save(
                Account.builder().user(user1).balance(new BigDecimal("1000.00")).build());
        Account accountB = accountRepository.save(
                Account.builder().user(user2).balance(new BigDecimal("1000.00")).build());

        int transfersEachDirection = 50;
        BigDecimal amount = new BigDecimal("10.00");
        String runId = UUID.randomUUID().toString();

        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(transfersEachDirection * 2);

        for (int i = 0; i < transfersEachDirection; i++) {
            final int idx = i;

            executorService.submit(() -> {
                try {
                    transferService.transfer(new TransferRequest(
                            accountA.getId(), accountB.getId(), amount, runId + "-ab-" + idx));

                } finally {
                    latch.countDown();
                }
            });

            executorService.submit(() -> {
                try {
                    transferService.transfer(new TransferRequest(
                            accountB.getId(), accountA.getId(), amount, runId + "-ba-" + idx));

                } finally {
                    latch.countDown();
                }
            });
        }

        boolean finished = latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        assertThat(finished).isTrue();

        Account finalA = accountRepository.findById(accountA.getId()).orElseThrow();
        Account finalB = accountRepository.findById(accountB.getId()).orElseThrow();
        assertThat(finalA.getBalance()).isEqualByComparingTo("1000.00");
        assertThat(finalB.getBalance()).isEqualByComparingTo("1000.00");
    }
}

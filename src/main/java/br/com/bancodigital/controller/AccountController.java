package br.com.bancodigital.controller;

import br.com.bancodigital.controller.response.TransferResponse;
import br.com.bancodigital.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{accountId}/transfers")
    public Page<TransferResponse> listTransfers(@PathVariable Long accountId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return accountService.listTransfers(accountId, page, size);
    }

}

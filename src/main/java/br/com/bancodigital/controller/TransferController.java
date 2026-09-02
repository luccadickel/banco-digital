package br.com.bancodigital.controller;

import br.com.bancodigital.controller.request.TransferRequest;
import br.com.bancodigital.controller.response.TransferResponse;
import br.com.bancodigital.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse transfer(@Valid @RequestBody TransferRequest transferRequest) {
        return transferService.transfer(transferRequest);
    }
}

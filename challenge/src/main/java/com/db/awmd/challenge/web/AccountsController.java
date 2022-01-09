package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.dto.TransferObject;
import com.db.awmd.challenge.exception.BalanceTransferException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.service.AccountsService;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

  private final AccountsService accountsService;

  @Autowired
  public AccountsController(AccountsService accountsService) {
    this.accountsService = accountsService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
    log.info("Creating account {}", account);

    try {
    this.accountsService.createAccount(account);
    } catch (DuplicateAccountIdException daie) {
      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, value="/transfer")
  public ResponseEntity<Object> transferBalance(@RequestBody TransferObject transferObject) {
    log.info("Transfer of {} amount from account ID {} to account ID {}.", transferObject.getAmount(), transferObject.getAccountFromId(), transferObject.getAccountToId());

    try {
      this.accountsService.transferBalance(transferObject.getAccountFromId(), transferObject.getAccountToId(), transferObject.getAmount());
    } catch (BalanceTransferException balanceTransferException) {
      return new ResponseEntity<>(balanceTransferException.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>("Transferred "+transferObject.getAmount()+ " rupees from account "+transferObject.getAccountFromId()+" to account "+transferObject.getAccountToId(),HttpStatus.ACCEPTED);
  }

  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) {
    log.info("Retrieving account for id {}", accountId);
    return this.accountsService.getAccount(accountId);
  }

}

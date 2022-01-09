package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.BalanceTransferException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public void createAccount(Account account) throws DuplicateAccountIdException {
        Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
        if (previousAccount != null) {
            throw new DuplicateAccountIdException(
                    "Account id " + account.getAccountId() + " already exists!");
        }
    }

    @Override
    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    @Override
    public void clearAccounts() {
        accounts.clear();
    }

    /**
     * Transfers given amount from one account to other. Given that amount is a positive number.
     *
     * @param accountFromId Source account from where the money should be transferred
     * @param accountToId Target account where the money should be transferred
     * @param amount Value of money to be transferred
     * @return 'true' if transaction is completed, 'false' if transaction is not complete
     * @throws BalanceTransferException
     */
    @Override
    public synchronized boolean transferBalance(String accountFromId, String accountToId, BigDecimal amount) throws BalanceTransferException {
        boolean status;

        if (amount.compareTo(new BigDecimal(0)) < 0) {
            throw new BalanceTransferException("The amount to transfer should always be a positive number");
        }
        if (!accounts.containsKey(accountFromId)) {
            throw new BalanceTransferException("Source account " + accountFromId + " doesn't exist");
        }
        if (!accounts.containsKey(accountToId)) {
            throw new BalanceTransferException("Target account " + accountToId + " doesn't exist");
        }

        Account sourceAccount = accounts.get(accountFromId);
        Account targetAccount = accounts.get(accountToId);

        BigDecimal sourceAccountNewBalance = sourceAccount.getBalance().subtract(amount);
        BigDecimal targetAccountNewBalance = targetAccount.getBalance().add(amount);


        if (sourceAccountNewBalance.compareTo(new BigDecimal(0)) >= 0
                && targetAccountNewBalance.compareTo(new BigDecimal(0)) >= 0) {
            sourceAccount.setBalance(sourceAccountNewBalance);
            targetAccount.setBalance(targetAccountNewBalance);
            status = true;
        } else {
            throw new BalanceTransferException("We do not support overdrafts!");
        }

        return status;
    }

}

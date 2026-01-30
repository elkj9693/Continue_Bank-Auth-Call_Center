package com.entrusting.backend.user.controller;

import com.entrusting.backend.user.entity.Account;
import com.entrusting.backend.user.entity.Transaction;
import com.entrusting.backend.user.service.AccountService;
import com.entrusting.backend.user.repository.TransactionRepository;
import com.entrusting.backend.user.repository.AccountRepository;
import com.entrusting.backend.user.entity.User;
import com.entrusting.backend.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final AccountRepository accountRepository;

    public DashboardController(AccountService accountService, TransactionRepository transactionRepository,
            UserService userService, AccountRepository accountRepository) {
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.accountRepository = accountRepository;
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(@RequestParam String username) {
        try {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            List<Account> accounts = accountRepository.findByUser(user);
            BigDecimal totalBalance = accounts.stream()
                    .map(Account::getBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<Map<String, Object>> accountSummary = accounts.stream().map(acc -> {
                Map<String, Object> map = new HashMap<>();
                map.put("accountNumber", acc.getAccountNumber());
                map.put("accountName", acc.getAccountName());
                map.put("balance", acc.getBalance());
                return map;
            }).collect(Collectors.toList());

            // Recent 7 days transactions for graph
            LocalDateTime since = LocalDateTime.now().minusDays(7);
            List<Transaction> recentTransactions = new ArrayList<>();
            for (Account acc : accounts) {
                recentTransactions.addAll(transactionRepository.findRecentTransactions(acc, since));
            }

            // Sort and limit
            List<Map<String, Object>> transactionList = recentTransactions.stream()
                    .sorted(Comparator.comparing(Transaction::getTimestamp).reversed())
                    .limit(10)
                    .map(t -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", t.getId());
                        map.put("type", t.getType());
                        map.put("amount", t.getAmount());
                        map.put("description", t.getDescription());
                        map.put("timestamp", t.getTimestamp());
                        map.put("sender", t.getSender() != null ? t.getSender().getAccountNumber() : "SYSTEM");
                        map.put("receiver", t.getReceiver() != null ? t.getReceiver().getAccountNumber() : "SYSTEM");
                        return map;
                    }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("totalBalance", totalBalance);
            response.put("accounts", accountSummary);
            response.put("recentTransactions", transactionList);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

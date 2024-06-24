package com.example.joaomatos.picpay_lite_desafio_backend.transation;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.joaomatos.picpay_lite_desafio_backend.authorization.AuthorizerService;
import com.example.joaomatos.picpay_lite_desafio_backend.notification.NotificationService;
import com.example.joaomatos.picpay_lite_desafio_backend.wallet.Wallet;
import com.example.joaomatos.picpay_lite_desafio_backend.wallet.WalletRepository;
import com.example.joaomatos.picpay_lite_desafio_backend.wallet.WalletType;

@Service
public class TransactionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionService.class);
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final AuthorizerService authorizerService;
    private final NotificationService notificationService;

    public TransactionService(TransactionRepository transactionRepository,
            WalletRepository walletRepository,
            AuthorizerService authorizerService,
            NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.authorizerService = authorizerService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Transaction create(Transaction transaction) {
        LOGGER.info("Starting transaction creation for: {}", transaction);

        // Valida a transação antes de prosseguir
        validateTransaction(transaction);

        // Salva a nova transação no repositório
        var newTransaction = transactionRepository.save(transaction);

        // Processa a transferência entre as carteiras
        processTransaction(transaction);

        // Autoriza a transação
        authorizerService.authorize(transaction);

        // Notifica sobre a nova transação
        notificationService.notify(newTransaction);

        LOGGER.info("Transaction created successfully: {}", newTransaction);
        return newTransaction;
    }

    /**
     * Valida se a transação atende às regras de negócio definidas.
     */
    private void validateTransaction(Transaction transaction) {
        LOGGER.info("Validating transaction: {}", transaction);

        @SuppressWarnings("unused")
        Wallet payeeWallet = getWalletOrThrow(transaction.payee(),
                "Payee wallet not found for transaction: " + transaction);
        Wallet payerWallet = getWalletOrThrow(transaction.payer(),
                "Payer wallet not found for transaction: " + transaction);

        if (!isPayerEligible(payerWallet, transaction)) {
            throw new InvalidTransactionException(
                    "Invalid transaction - Payer is not eligible for transaction: " + transaction);
        }

        LOGGER.info("Transaction is valid.");
    }

    /**
     * Recupera uma carteira pelo ID ou lança uma exceção se não for encontrada.
     */
    private Wallet getWalletOrThrow(Long walletId, String errorMessage) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new InvalidTransactionException(errorMessage));
    }

    /**
     * Verifica se a carteira do pagador é elegível para realizar a transação.
     */
    private boolean isPayerEligible(Wallet payerWallet, Transaction transaction) {
        return isCommonWallet(payerWallet) &&
                hasSufficientBalance(payerWallet, transaction.value()) &&
                isNotSameAsPayee(payerWallet, transaction.payee());
    }

    /**
     * Verifica se a carteira é do tipo COMUM.
     */
    private boolean isCommonWallet(Wallet wallet) {
        return wallet.type() == WalletType.COMUM.getValue();
    }

    /**
     * Verifica se a carteira tem saldo suficiente para a transação.
     */
    private boolean hasSufficientBalance(Wallet wallet, BigDecimal transactionValue) {
        return wallet.balance().compareTo(transactionValue) >= 0;
    }

    /**
     * Verifica se o pagador e o recebedor não são a mesma entidade.
     */
    private boolean isNotSameAsPayee(Wallet payerWallet, Long payeeId) {
        return !payerWallet.id().equals(payeeId);
    }

    /**
     * Processa a transferência de valores entre as carteiras do pagador e do
     * recebedor.
     */
    private void processTransaction(Transaction transaction) {
        Wallet payerWallet = getWalletOrThrow(transaction.payer(),
                "Payer wallet not found for transaction: " + transaction);
        Wallet payeeWallet = getWalletOrThrow(transaction.payee(),
                "Payee wallet not found for transaction: " + transaction);

        // Debita o valor da carteira do pagador
        walletRepository.save(payerWallet.debit(transaction.value()));

        // Credita o valor na carteira do recebedor
        walletRepository.save(payeeWallet.credit(transaction.value()));

        LOGGER.info("Processed transaction between payer: {} and payee: {}",
                payerWallet.id(), payeeWallet.id());
    }

    /**
     * Retorna todas as transações do repositório.
     */
    public List<Transaction> list() {
        return transactionRepository.findAll();
    }

}

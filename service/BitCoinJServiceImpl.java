package com.coin.app.service;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import com.coin.app.model.User;
import com.coin.app.model.enums.TransactionStatus;
import com.coin.app.model.enums.TransactionType;
import com.coin.app.model.enums.UserRole;
import com.coin.app.repository.AccountRepository;
import com.coin.app.repository.TransactionRepository;
import com.coin.app.repository.UserRepository;
import com.coin.app.repository.WalletRepository;
import com.coin.app.util.Utills;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.crypto.KeyCrypterException;
import org.bitcoinj.crypto.KeyCrypterScrypt;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.spongycastle.crypto.params.KeyParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BitCoinJServiceImpl implements BitcoinJService
{
    @Autowired
    TransactionService transactionService;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    WalletService walletService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    UserRepository userRepository;

    //    private static Address forwardingAddress;
    private static WalletAppKit kit;

    private NetworkParameters params;

    @Override
    public void initialize()
    {
        BriefLogFormatter.init();

        // The available options are:
        // - MainNetParams for main bitcoin network
        // - TestNet3Params for test app with a real network
        // - RegTestParams for developing project (Developement)
        params = TestNet3Params.get();
        initializeWallet();
    }


    private void initializeWallet()
    {
        while (kit == null)
        {
            kit = new WalletAppKit(params, new File("C:\\Wallets\\"), "coinWallet"); //.replaceAll("[^a-zA-Z0-9.-]", "_") + "-" + params.getPaymentProtocolId();
            kit.startAsync();
            kit.awaitRunning();
            if(!kit.wallet().isEncrypted())
                kit.wallet().encrypt("Qwerty123");
            startCoinReceiveListener();
            System.out.println("////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
            System.out.println("////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
            System.out.println("////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
            System.out.println("////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
            System.out.println("////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
            System.out.println("////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
            System.out.println("////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
            System.out.println("////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
            System.out.println("////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
            System.out.println("\n");
            System.out.println("Wallet is initialized !!!");
            System.out.println("\n");
            System.out.println("////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
            System.out.println("////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
            System.out.println("////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
            System.out.println("////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
            System.out.println("////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
            System.out.println("////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
            System.out.println("////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
            System.out.println("////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
            System.out.println("////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
            System.out.println("////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
            System.out.println("////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");

        }
    }

    @Override
    public String getNewWalletAddress()
    {
        return kit.wallet().freshReceiveAddress().toString();
    }

    @Override
    public void updateWalletJob()
    {
        for (com.coin.app.model.Transaction transaction : transactionRepository.findByStatus(TransactionStatus.UNCONFIRMED))
        {
            for (Transaction tx : kit.wallet().getTransactionsByTime())
            {
                if (transaction.getTxId().equals(tx.getHashAsString()))
                {
                    transaction.setUpdateDate(LocalDate.now(ZoneId.of("Asia/Tehran")));
                    transaction.setUpdateTime(LocalTime.now(ZoneId.of("Asia/Tehran")));
                    if (transaction.getType().equals(TransactionType.DEPOSIT))
                        transaction.setDescription("در انتظار تایید واریز ...");
                    else if (transaction.getType().equals(TransactionType.WITHDRAWAL))
                        transaction.setDescription("در انتظار تایید برداشت ...");
                    if (!tx.isPending())
                    {
                        if (transaction.getType().equals(TransactionType.DEPOSIT))
                            transaction.setDescription("واریز " + Utills.commaSeparator(String.valueOf(Math.abs(transaction.getTotalValue()))) + " ساتوشی به حساب شما");
                        else if (transaction.getType().equals(TransactionType.WITHDRAWAL))
                            transaction.setDescription("برداشت " + Utills.commaSeparator(String.valueOf(Math.abs(transaction.getTotalValue()))) + " ساتوشی از حساب شما");

                        transaction.setStatus(TransactionStatus.CONFIRMED);
                        updateUserWallet(transaction);
                        updateAdminRealBalance();
                    }
                    transactionRepository.save(transaction);
                }
            }
        }

        int count = transactionRepository.countByType(TransactionType.DEPOSIT);
        List<Transaction> transactions = kit.wallet().getTransactionsByTime();
        if (transactions.size() != count)
            for (Transaction tx : transactions)
                saveUserTransaction(kit.wallet(), tx);
    }

    private void startCoinReceiveListener()
    {
        kit.wallet().addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener()
        {
            @Override
            public void onCoinsReceived(Wallet w, Transaction tx, Coin prevBalance, Coin newBalance)
            {
                if (tx.getPurpose().equals(Transaction.Purpose.USER_PAYMENT))
                    return;
                System.out.println("Received tx for " + tx.getValueSentToMe(w).toFriendlyString() + ": " + tx);
                com.coin.app.model.Transaction transaction = saveUserTransaction(w, tx);

                // Wait until it's made it into the block chain (may run immediately if it's already there).
                Futures.addCallback(tx.getConfidence().getDepthFuture(1), new FutureCallback<TransactionConfidence>()
                {
                    @Override
                    public void onSuccess(TransactionConfidence result)
                    {
                        System.out.println("Confirmation received.");
                    }

                    @Override
                    public void onFailure(Throwable t)
                    {
                        if (transaction != null && transaction.getStatus().equals(TransactionStatus.UNCONFIRMED))
                        {
                            transaction.setUpdateDate(LocalDate.now(ZoneId.of("Asia/Tehran")));
                            transaction.setUpdateTime(LocalTime.now(ZoneId.of("Asia/Tehran")));
                            transaction.setDescription(" عدم تایید واریز " + Utills.commaSeparator(String.valueOf(Math.abs(transaction.getTotalValue()))) + " ساتوشی به حساب شما");
                            transaction.setStatus(TransactionStatus.FAILED);
                            transactionRepository.save(transaction);
                            System.out.println("Confirmation Failure.");
                        }
                        throw new RuntimeException(t);
                    }
                });
            }
        });
    }

    private void updateAdminRealBalance()
    {
        User user = userRepository.findByUsername("Admin");
        if (user != null && user.getRole().equals(UserRole.ROLE_ADMIN))
        {
            user.getAccount().getWallet().setRealBalance(kit.wallet().getBalance().getValue() + "");
            walletRepository.save(user.getAccount().getWallet());
        }
    }

    private com.coin.app.model.Transaction saveUserTransaction(Wallet w, Transaction tx)
    {
        if (transactionRepository.findByTxId(tx.getHashAsString()) != null)
            return transactionRepository.findByTxId(tx.getHashAsString());
        com.coin.app.model.Wallet userWallet = null;
        List<TransactionOutput> outputs = tx.getOutputs();
        for (TransactionOutput out : outputs)
        {
            Address address = out.getAddressFromP2PKHScript(params);
            if (address == null)
                address = out.getAddressFromP2SH(params);
            userWallet = address == null ? null : walletRepository.findByAddress(address.toString());
            if (userWallet != null)
                break;
        }

        if (userWallet == null)
            userWallet = userRepository.findByUsername("Admin").getAccount().getWallet();

        com.coin.app.model.Transaction transaction = new com.coin.app.model.Transaction();
        transaction.setCreatedDate(LocalDate.now(ZoneId.of("Asia/Tehran")));
        transaction.setCreatedTime(LocalTime.now(ZoneId.of("Asia/Tehran")));
        transaction.setUpdateDate(LocalDate.now(ZoneId.of("Asia/Tehran")));
        transaction.setUpdateTime(LocalTime.now(ZoneId.of("Asia/Tehran")));
        transaction.setTxId(tx.getHashAsString());
        transaction.setFee(tx.getFee() == null ? null : tx.getFee().getValue() + "");
        long fee = tx.getFee() == null ? 0 : tx.getFee().getValue();
        transaction.setTotalValue(Math.abs(tx.getValue(w).getValue()) - fee);
        transaction.setStatus(TransactionStatus.UNCONFIRMED);
        if( tx.getValue(w).getValue() > 0 )
        {
            transaction.setType(TransactionType.DEPOSIT);
            transaction.setDescription("در انتظار تایید واریز ...");
        }
        else
        {
            for(TransactionOutput out : outputs)
                if(Math.abs(tx.getValue(w).getValue()) - tx.getFee().getValue() == out.getValue().getValue())
                {
                    transaction.setType(TransactionType.FORWARD);
                    transaction.setDescription("انتقال " + Utills.commaSeparator(String.valueOf(Math.abs(tx.getValue(w).getValue()) - fee)) +  " ساتوشی به آدرس "
                            + out.getAddressFromP2PKHScript(params) + " و " + Math.round((Math.abs(tx.getValue(w).getValue()) - 1000)  * 0.005) + " ساتوشی کارمزد به حساب مدیریت");
                    break;
                }
//
        }

        transaction.setAccount(accountRepository.findByWallet(userWallet));
        return transactionRepository.save(transaction);
    }

    private void updateUserWallet(com.coin.app.model.Transaction transaction)
    {
        com.coin.app.model.Wallet wallet = transaction.getAccount().getWallet();
        if (transaction.getType().equals(TransactionType.DEPOSIT))
        {
            wallet.setBalance((Long.valueOf(wallet.getBalance()) + transaction.getTotalValue()) + "");
            walletRepository.save(wallet);
        }
    }

    @Override
    public TransactionStatus forwardCoins(Long amount, String address)
    {
        Address forwardingAddress = new Address(params, address);
        try
        {
            SendRequest sendRquest = SendRequest.to(forwardingAddress, Coin.valueOf(amount));
            sendRquest.feePerKb = Coin.ZERO;
            sendRquest.aesKey = kit.wallet().getKeyCrypter().deriveKey("Qwerty123");
            Wallet.SendResult sendResult = kit.wallet().sendCoins(kit.peerGroup(), sendRquest);
            System.out.println("Sending ...");

            Long fee = sendResult.tx.getFee() == null ? 0L : sendResult.tx.getFee().value;
            Long value = Math.abs(sendResult.tx.getValue(kit.wallet()).getValue()) - fee;

            // Register a callback that is invoked when the transaction has propagated across the network.
            // This shows a second style of registering ListenableFuture callbacks, it works when you don't
            // need access to the object the future returns.
            sendResult.broadcastComplete.addListener(new Runnable()
            {
                @Override
                public void run()
                {
                    //Remove fee from admin balance
                    com.coin.app.model.Wallet adminWallet = userRepository.findByUsername("Admin").getAccount().getWallet();
                    Long adminBalance = Long.valueOf(adminWallet.getBalance()) - fee;
                    adminWallet.setBalance(adminBalance.toString());
                    walletRepository.save(adminWallet);
                    updateAdminRealBalance();
                }
            }, MoreExecutors.directExecutor());

        } catch (KeyCrypterException | InsufficientMoneyException e)
        {
            // We don't use encrypted wallets in this example - can never happen.
            System.out.println(e.getMessage());
            return TransactionStatus.FAILED;
        }

        return TransactionStatus.CONFIRMED;
    }
}

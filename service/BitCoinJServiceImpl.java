package com.coin.app.service;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.coin.app.model.Account;
import com.coin.app.model.User;
import com.coin.app.model.enums.TransactionStatus;
import com.coin.app.model.enums.TransactionType;
import com.coin.app.model.enums.UserRole;
import com.coin.app.repository.AccountRepository;
import com.coin.app.repository.TransactionRepository;
import com.coin.app.repository.UserRepository;
import com.coin.app.repository.WalletRepository;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
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


    @Async
    @Override
    public void initializeWallet()
    {
        while (kit == null)
        {
            kit = new WalletAppKit(params, new File("C:\\Wallets\\"), "coinWallet"); //.replaceAll("[^a-zA-Z0-9.-]", "_") + "-" + params.getPaymentProtocolId();
            kit.startAsync();
            kit.awaitRunning();
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
        for (com.coin.app.model.Transaction transaction : transactionRepository.findByStatusAndType(TransactionStatus.UNCONFIRMED, TransactionType.DEPOSIT))
        {
            for (Transaction tx : kit.wallet().getTransactionsByTime())
            {
                if (transaction.getTxId().equals(tx.getHashAsString()))
                {
                    transaction.setUpdateDate(new Date());
                    transaction.setDescription("Confirmed on job: " + tx.getConfidence().getBroadcastBy().size() + " confirmations");
                    if (!tx.isPending())
                    {
                        transaction.setStatus(TransactionStatus.CONFIRMED);
                        updateAdminAccount();
                    }
                    transactionRepository.save(transaction);
                }
            }
        }

        int count = transactionRepository.countByType(TransactionType.DEPOSIT);
        List<Transaction> transactions = kit.wallet().getTransactionsByTime();
        if (transactions.size() != count)
            for (Transaction tx : transactions)
                updateUserWallet(kit.wallet(), tx);
    }

    private void updateAdminAccount()
    {
        User user = userRepository.findByUsername("Admin");
        if (user != null && user.getRole().equals(UserRole.ROLE_ADMIN))
        {
            user.getAccount().getWallet().setRealBalance(kit.wallet().getBalance().getValue() + "");
            walletRepository.save(user.getAccount().getWallet());
        }
    }

    private void startCoinReceiveListener()
    {
        kit.wallet().addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener()
        {
            @Override
            public void onCoinsReceived(Wallet w, Transaction tx, Coin prevBalance, Coin newBalance)
            {
                System.out.println("Received tx for " + tx.getValueSentToMe(w).toFriendlyString() + ": " + tx);
                com.coin.app.model.Transaction transaction = updateUserWallet(w, tx);

                // Wait until it's made it into the block chain (may run immediately if it's already there).
                Futures.addCallback(tx.getConfidence().getDepthFuture(1), new FutureCallback<TransactionConfidence>()
                {
                    @Override
                    public void onSuccess(TransactionConfidence result)
                    {
                        if (transaction != null && transaction.getStatus().equals(TransactionStatus.UNCONFIRMED))
                        {
                            transaction.setUpdateDate(new Date());
                            transaction.setDescription("Confirmed on success: " + tx.getConfidence().getBroadcastBy().size() + " confirmations");
                            transaction.setStatus(TransactionStatus.CONFIRMED);
                            transactionRepository.save(transaction);
                            updateAdminAccount();
                        }
                        System.out.println("Confirmation received.");
                    }

                    @Override
                    public void onFailure(Throwable t)
                    {
                        if (transaction != null && transaction.getStatus().equals(TransactionStatus.UNCONFIRMED))
                        {
                            transaction.setUpdateDate(tx.getUpdateTime());
                            transaction.setDescription("Deposit confirmation Failed. id: " + tx.getHashAsString() + "   value: " + tx.getValue(w));
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

    private com.coin.app.model.Transaction updateUserWallet(Wallet w, Transaction tx)
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
        if (userWallet != null)
        {
            com.coin.app.model.Transaction transaction = new com.coin.app.model.Transaction();
            transaction.setCreatedDate(tx.getUpdateTime());
            transaction.setUpdateDate(tx.getUpdateTime());
            transaction.setTxId(tx.getHashAsString());
            transaction.setFee(tx.getFee() == null ? null : tx.getFee().getValue() + "");
            transaction.setTotalValue(tx.getValue(w).getValue());
            transaction.setAccount(accountRepository.findByWallet(userWallet));
            transaction.setStatus(TransactionStatus.UNCONFIRMED);
            transaction.setType(TransactionType.DEPOSIT);
            transaction.setDescription("Confirmed on received: " + tx.getConfidence().getBroadcastBy().size() + " confirmations");

            userWallet.setBalance(Long.valueOf(userWallet.getBalance()) + tx.getValue(w).getValue() + "");
            walletRepository.save(userWallet);
            return transactionRepository.save(transaction);
        }

        return null;
    }

    //For using to forward coins to user's external wallet address. Need to modify
    public void forwardCoins(Account account, String address)
    {
        Address forwardingAddress = new Address(params, address);
        Long realBalanceCoin = Coin.parseCoin(account.getWallet().getBalance()).value;
//        try
//        {
//            SendRequest sendRquest = SendRequest.emptyWallet(forwardingAddress);
//            sendRquest.feePerKb = Coin.ZERO;
//            Wallet.SendResult sendResult = kit.wallet().sendCoins(kit.peerGroup(), sendRquest);
//            System.out.println("Sending ...");
//
//            Long fee = sendResult.tx.getFee() == null ? 0L : sendResult.tx.getFee().value;
//            Long value = sendResult.tx.getValueSentFromMe(kit.wallet()).value - fee;
//
//
//            transactionService.createOrUpdateTransaction(sendResult.tx.getHashAsString(), Coin.valueOf(fee).toFriendlyString(),
//                    value, Coin.valueOf(value).toFriendlyString(), account, TransactionStatus.UNCONFIRMED, TransactionType.FORWARD);
//
//            // Update real balance of the user's wallet
//            account.getWallet().setRealBalance(Coin.valueOf(realBalanceCoin + value).toFriendlyString());
//            walletService.update(account.getWallet());
//
//            // Register a callback that is invoked when the transaction has propagated across the network.
//            // This shows a second style of registering ListenableFuture callbacks, it works when you don't
//            // need access to the object the future returns.
//            sendResult.broadcastComplete.addListener(new Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    // The wallet has changed now, it'll get auto saved shortly or when the app shuts down.
//                    System.out.println("Sent coins onwards! Transaction hash is " + sendResult.tx.getHashAsString());
//                    transactionService.createOrUpdateTransaction(sendResult.tx.getHashAsString(), Coin.valueOf(fee).toFriendlyString(),
//                            value, Coin.valueOf(value).toFriendlyString(), account, TransactionStatus.CONFIRMED, TransactionType.FORWARD);
//
//                }
//            }, MoreExecutors.directExecutor());
//        } catch (KeyCrypterException | InsufficientMoneyException e)
//        {
//            // We don't use encrypted wallets in this example - can never happen.
//            throw new RuntimeException(e);
//        }
    }
}

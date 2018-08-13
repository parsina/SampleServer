package com.coin.app.service;

import java.io.File;
import java.util.Date;

import com.coin.app.model.Account;
import com.coin.app.model.User;
import com.coin.app.model.enums.TransactionStatus;
import com.coin.app.model.enums.TransactionType;
import com.coin.app.repository.WalletRepository;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.crypto.KeyCrypterException;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BitCoinJServiceImpl implements BitcoinJService
{
    @Autowired
    TransactionService transactionService;

    @Autowired
    WalletService walletService;

    @Autowired
    WalletRepository walletRepository;

    private static Address forwardingAddress;
    private static WalletAppKit kit;
    NetworkParameters params;

    @Override
    public void initialize()
    {
        BriefLogFormatter.init();
        params = TestNet3Params.get() ;

        if(walletRepository.findByName("admin@coinnet.net") != null)
            forwardingAddress = new Address(params, walletRepository.findByName("admin@coinnet.net").getAddress());

        System.out.println("Network: " + params.getId());
        System.out.println("Forwarding address: " + forwardingAddress);
    }


    @Override
    public com.coin.app.model.Wallet initializeWallet(User user)
    {
        kit = new WalletAppKit(params, new File("C:\\Wallets\\"), user.getEmail()); //.replaceAll("[^a-zA-Z0-9.-]", "_") + "-" + params.getPaymentProtocolId();
        kit.startAsync();
        kit.awaitRunning();

        com.coin.app.model.Wallet wallet = null;
        if(user.getAccount() != null && user.getAccount().getWallet() != null)
            wallet = user.getAccount().getWallet();

        if( wallet == null)
        {
            wallet = new com.coin.app.model.Wallet();
            wallet.setCreatedDate(new Date());
            wallet.setName(user.getEmail());
            wallet.setBalance(kit.wallet().getBalance().toFriendlyString());
            wallet.setRealBalance(kit.wallet().getBalance().toFriendlyString());
            wallet.setAddress(kit.wallet().freshReceiveAddress().toString());
        }
        else if(kit.wallet().getBalance().value > 0 )
            forwardCoins(user.getAccount());
        return wallet;
    }

    public void startCoinReceiveListener(Account account)
    {
        kit.wallet().addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener()
        {
            @Override
            public void onCoinsReceived(Wallet w, Transaction tx, Coin prevBalance, Coin newBalance)
            {
                System.out.println("Received tx for " + tx.getValueSentToMe(w).toFriendlyString() + ": " + tx);
                System.out.println("Transaction will be forwarded after it confirms.");

                transactionService.createOrUpdateTransaction(tx.getHashAsString(), tx.getFee() == null ? null: tx.getFee().toFriendlyString(),
                        tx.getValue(w).value, tx.getValue(w).toFriendlyString(), account, TransactionStatus.UNCONFIRMED,
                        TransactionType.DEPOSIT);


                // Wait until it's made it into the block chain (may run immediately if it's already there).
                Futures.addCallback(tx.getConfidence().getDepthFuture(1), new FutureCallback<TransactionConfidence>()
                {
                    @Override
                    public void onSuccess(TransactionConfidence result)
                    {
                        System.out.println("Confirmation received.");

                        transactionService.createOrUpdateTransaction(tx.getHashAsString(), tx.getFee() == null ? null: tx.getFee().toFriendlyString(),
                                tx.getValue(w).value, tx.getValue(w).toFriendlyString(), account, TransactionStatus.CONFIRMED,
                                TransactionType.DEPOSIT);
                        forwardCoins(account);
                    }

                    @Override
                    public void onFailure(Throwable t)
                    {
                        System.out.println("Confirmation Failure.");
                        transactionService.createOrUpdateTransaction(tx.getHashAsString(), tx.getFee() == null ? null: tx.getFee().toFriendlyString(),
                                tx.getValue(w).value, tx.getValue(w).toFriendlyString(), account, TransactionStatus.FAILED,
                                TransactionType.DEPOSIT);
                        throw new RuntimeException(t);
                    }
                });
            }
        });

//        try
//        {
//            Thread.sleep(Long.MAX_VALUE);
//        } catch (InterruptedException ignored)
//        {
//        }
    }

    private void forwardCoins(Account account)
    {
        try
        {
            Long balanceCoin = Coin.parseCoin(account.getWallet().getBalance().split(" ")[0]).value;
            Long realBalanceCoin = Coin.parseCoin(account.getWallet().getRealBalance().split(" ")[0]).value;

            // Update balance of the user's Wallet
            account.getWallet().setBalance(Coin.valueOf(balanceCoin + kit.wallet().getBalance().value).toFriendlyString());

            SendRequest sendRquest = SendRequest.emptyWallet(forwardingAddress);
            sendRquest.feePerKb = Coin.ZERO;
            Wallet.SendResult sendResult = kit.wallet().sendCoins(kit.peerGroup(), sendRquest);
            System.out.println("Sending ...");

            Long fee = sendResult.tx.getFee() == null ? 0L : sendResult.tx.getFee().value;
            Long value = sendResult.tx.getValueSentFromMe(kit.wallet()).value - fee;


            transactionService.createOrUpdateTransaction(sendResult.tx.getHashAsString(), Coin.valueOf(fee).toFriendlyString(),
                    value, Coin.valueOf(value).toFriendlyString(), account, TransactionStatus.UNCONFIRMED, TransactionType.FORWARD);

            // Update real balance of the user's wallet
            account.getWallet().setRealBalance(Coin.valueOf(realBalanceCoin + value).toFriendlyString());
            walletService.update(account.getWallet());

            // Register a callback that is invoked when the transaction has propagated across the network.
            // This shows a second style of registering ListenableFuture callbacks, it works when you don't
            // need access to the object the future returns.
            sendResult.broadcastComplete.addListener(new Runnable()
            {
                @Override
                public void run()
                {
                    // The wallet has changed now, it'll get auto saved shortly or when the app shuts down.
                    System.out.println("Sent coins onwards! Transaction hash is " + sendResult.tx.getHashAsString());
                    transactionService.createOrUpdateTransaction(sendResult.tx.getHashAsString(), Coin.valueOf(fee).toFriendlyString(),
                            value, Coin.valueOf(value).toFriendlyString(), account, TransactionStatus.CONFIRMED, TransactionType.FORWARD);

                }
            }, MoreExecutors.directExecutor());
        } catch (KeyCrypterException | InsufficientMoneyException e)
        {
            // We don't use encrypted wallets in this example - can never happen.
            throw new RuntimeException(e);
        }
    }
}

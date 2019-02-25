package com.coin.app.service;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.coin.app.model.Bitrix;
import com.coin.app.model.BitrixTransaction;
import com.coin.app.model.enums.TransactionStatus;
import com.coin.app.model.enums.TransactionType;
import com.coin.app.repository.BitrixRepository;
import com.coin.app.repository.BitrixTransactionRepository;
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
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BitCoinJServiceImpl implements BitcoinJService
{
    @Value("${app.walletKit.directory}")
    private String walletPath;

    @Value("${app.walletKit.password}")
    private String walletPass;

    @Value("${app.walletKit.netType}")
    private String networkType;

    @Autowired
    private BitrixService bitrixService;

    @Autowired
    private BitrixRepository bitrixRepository;

    @Autowired
    private BitrixTransactionRepository bitrixTransactionRepository;


    private static WalletAppKit walletAppKit;

    private NetworkParameters networkParameters;

    @Override
    public Long getWalletBalance()
    {
        return walletAppKit.wallet().getBalance().value;
    }

    @Override
    public void initialize()
    {
        BriefLogFormatter.init();

        // The available options are:
        // - MainNetParams for main bitcoin network
        // - TestNet3Params for test app with a real network
        // - RegTestParams for developing project (Developement)

        if (networkType.trim().toLowerCase().equals("mainnetwork"))
            networkParameters = MainNetParams.get();
        else
            networkParameters = TestNet3Params.get();
        initializeWallet();
    }


    private void initializeWallet()
    {
        while (walletAppKit == null)
        {
            System.out.println("\n------------------------------------------");
            System.out.println("------------------------------------------");
            System.out.println(" >>>>>>>>  Initializing wallet  ==> Awate Running ... ");
            System.out.println("------------------------------------------");
            System.out.println("------------------------------------------\n");

            walletAppKit = new WalletAppKit(networkParameters, new File(walletPath), "coinWallet");
            walletAppKit.startAsync();
            walletAppKit.awaitRunning();
            if (!walletAppKit.wallet().isEncrypted())
                walletAppKit.wallet().encrypt(walletPass);

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
            bitrixService.initializeDataBase();
        }
    }

    @Override
    public String getNewWalletAddress()
    {
        return walletAppKit.wallet().freshReceiveAddress().toString();
    }

    public void updateAllAcountBalances()
    {
        for (BitrixTransaction transaction : bitrixTransactionRepository.findByStatus(TransactionStatus.UNCONFIRMED))
        {
            for (Transaction tx : walletAppKit.wallet().getTransactionsByTime())
            {
                if (transaction.getTxId().equals(tx.getHashAsString()))
                {
                    transaction.setUpdateDate(LocalDate.now());
                    transaction.setUpdateTime(LocalTime.now());
                    if (transaction.getType().equals(TransactionType.DEPOSIT) || transaction.getType().equals(TransactionType.WITHDRAWAL))
                        transaction.setDescription("Waiting to confirm ...");
                    if (!tx.isPending())
                    {
                        if (transaction.getType().equals(TransactionType.DEPOSIT) || transaction.getType().equals(TransactionType.WITHDRAWAL))
                            transaction.setDescription("Transaction confirmed");
                        transaction.setStatus(TransactionStatus.CONFIRMED);
                        updateUserWallet(transaction);
                        updateWalletBalance();
                    }
                    bitrixTransactionRepository.save(transaction);
                }
            }
        }

        int count = bitrixTransactionRepository.countByType(TransactionType.DEPOSIT);
        List<Transaction> transactions = walletAppKit.wallet().getTransactionsByTime();
        if (transactions.size() != count)
            for (Transaction tx : transactions)
                saveUserTransaction(walletAppKit.wallet(), tx);
    }

    private void updateAdminRealBalance()
    {
        Bitrix admin = bitrixService.getAdmin();
        admin.setDescription("Real balance: " + walletAppKit.wallet().getBalance().getValue() + "");
        bitrixRepository.save(admin);
    }

    private void updateWalletRealBalance()
    {
        Bitrix admin = bitrixService.getAdmin();
        admin.setDescription("Real balance: " + walletAppKit.wallet().getBalance().getValue() + "");
        bitrixRepository.save(admin);
    }

    private void updateWalletBalance()
    {
        Bitrix wallet = bitrixService.getWallet();
        wallet.setBalance(walletAppKit.wallet().getBalance().getValue());
        bitrixRepository.save(wallet);
    }

    @Override
    public TransactionStatus forwardCoins(Long amount, String address)
    {
        Address forwardingAddress = new Address(networkParameters, address);
        try
        {
            SendRequest sendRquest = SendRequest.to(forwardingAddress, Coin.valueOf(amount));
            sendRquest.feePerKb = Coin.ZERO;
            sendRquest.aesKey = walletAppKit.wallet().getKeyCrypter().deriveKey(walletPass);
            Wallet.SendResult sendResult = walletAppKit.wallet().sendCoins(walletAppKit.peerGroup(), sendRquest);

            Long fee = sendResult.tx.getFee() == null ? 0L : sendResult.tx.getFee().value;
            Long value = Math.abs(sendResult.tx.getValue(walletAppKit.wallet()).getValue()) - fee;

            System.out.println(" >>>>>>>>>>>>  Fee: " + fee + "          >>>>>>>>>>> Value: " + value);

            // Register a callback that is invoked when the transaction has propagated across the network.
            // This shows a second style of registering ListenableFuture callbacks, it works when you don't
            // need access to the object the future returns.
            sendResult.broadcastComplete.addListener(new Runnable()
            {
                @Override
                public void run()
                {
                    //Remove fee from admin balance
                    Bitrix admin = bitrixService.getAdmin();
                    Long adminBalance = admin.getBalance() - fee;
                    admin.setBalance(adminBalance);
                    bitrixRepository.save(admin);
                    updateWalletBalance();
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

    private void startCoinReceiveListener()
    {
        walletAppKit.wallet().addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener()
        {
            @Override
            public void onCoinsReceived(Wallet w, Transaction tx, Coin prevBalance, Coin newBalance)
            {
                if (tx.getPurpose().equals(Transaction.Purpose.USER_PAYMENT))
                    return;
                System.out.println("Received tx for " + tx.getValueSentToMe(w).toFriendlyString() + ": " + tx);
                BitrixTransaction transaction = saveUserTransaction(w, tx);

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
                            transaction.setUpdateDate(LocalDate.now());
                            transaction.setUpdateTime(LocalTime.now());
                            transaction.setDescription("Transaction not confirmed. value: " + String.valueOf(Math.abs(transaction.getTotalValue()) / 100000000));
                            transaction.setStatus(TransactionStatus.FAILED);
                            bitrixTransactionRepository.save(transaction);
                        }
                        throw new RuntimeException(t);
                    }
                });
            }
        });
    }

    private BitrixTransaction saveUserTransaction(Wallet w, Transaction tx)
    {
        if (bitrixTransactionRepository.findByTxId(tx.getHashAsString()) != null)
            return bitrixTransactionRepository.findByTxId(tx.getHashAsString());
        Bitrix user = null;
        List<TransactionOutput> outputs = tx.getOutputs();
        for (TransactionOutput out : outputs)
        {
            Address address = out.getAddressFromP2PKHScript(networkParameters);
            if (address == null)
                address = out.getAddressFromP2SH(networkParameters);
            user = address == null ? null : bitrixRepository.findByAddress(address.toString());
            if (user != null)
                break;
        }

        if (user == null)
            user = bitrixService.getWallet();

        BitrixTransaction transaction = new BitrixTransaction();
        transaction.setCreatedDate(LocalDate.now());
        transaction.setCreatedTime(LocalTime.now());
        transaction.setUpdateDate(LocalDate.now());
        transaction.setUpdateTime(LocalTime.now());
        transaction.setTxId(tx.getHashAsString());
        transaction.setStatus(TransactionStatus.UNCONFIRMED);
        if (tx.getValue(w).getValue() > 0)
        {
            transaction.setTotalValue(tx.getValue(w).getValue());
            transaction.setType(TransactionType.DEPOSIT);
            transaction.setDescription("Waiting to confirm ...");
        } else
        {
            transaction.setFee(tx.getFee() == null ? null : tx.getFee().getValue() + "");
            long fee = tx.getFee() == null ? 0 : tx.getFee().getValue();
            transaction.setTotalValue(Math.abs(tx.getValue(w).getValue()) - fee);
            for (TransactionOutput out : outputs)
                if (Math.abs(tx.getValue(w).getValue()) - tx.getFee().getValue() == out.getValue().getValue())
                {
                    transaction.setType(TransactionType.FORWARD);
                    transaction.setDescription("Transfer " + String.valueOf((Math.abs(tx.getValue(w).getValue()) - fee) / 100000000) + " BTC to "
                            + (out.getAddressFromP2PKHScript(networkParameters) == null ? out.getAddressFromP2SH(networkParameters) : out.getAddressFromP2PKHScript(networkParameters)));
                    break;
                }
        }

        transaction.setUser(user);
        return bitrixTransactionRepository.save(transaction);
    }

    private void updateUserWallet(BitrixTransaction transaction)
    {
        if (transaction.getType().equals(TransactionType.DEPOSIT))
        {
            Bitrix user = transaction.getUser();
            user.setBalance(user.getBalance() + transaction.getTotalValue());
            bitrixRepository.save(user);
        }
    }
}

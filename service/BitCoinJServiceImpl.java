package com.coin.app.service;

import java.io.File;
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
import org.bitcoinj.kits.WalletAppKit;
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

    private static WalletAppKit walletAppKit;

    private NetworkParameters networkParameters;

    @Override
    public void initialize()
    {
        BriefLogFormatter.init();

        // The available options are:
        // - MainNetParams for main bitcoin network
        // - TestNet3Params for test app with a real network
        // - RegTestParams for developing project (Developement)
        networkParameters = TestNet3Params.get();
        initializeWallet();
    }


    private void initializeWallet()
    {
        while (walletAppKit == null)
        {
            walletAppKit = new WalletAppKit(networkParameters, new File(walletPath), "coinWallet");
            walletAppKit.startAsync();
            walletAppKit.awaitRunning();
            if(!walletAppKit.wallet().isEncrypted())
                walletAppKit.wallet().encrypt(walletPass);
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
        return walletAppKit.wallet().freshReceiveAddress().toString();
    }

    private void updateAdminRealBalance()
    {
        User user = userRepository.findByRole(UserRole.ROLE_ADMIN).get(0);
        if (user != null)
        {
            user.getAccount().getWallet().setRealBalance(walletAppKit.wallet().getBalance().getValue() + "");
            walletRepository.save(user.getAccount().getWallet());
        }
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
            System.out.println("Forwarding " + Utills.commaSeparator(amount.toString()) + " Satoshi to " + address + " ...");

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
                    com.coin.app.model.Wallet adminWallet = userRepository.findByRole(UserRole.ROLE_ADMIN).get(0).getAccount().getWallet();
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

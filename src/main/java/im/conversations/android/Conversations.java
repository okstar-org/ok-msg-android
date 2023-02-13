package im.conversations.android;

import android.app.Application;
import androidx.annotation.NonNull;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import eu.siacs.conversations.xmpp.Jid;
import im.conversations.android.database.model.Account;
import im.conversations.android.repository.AccountRepository;
import im.conversations.android.xmpp.ConnectionPool;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Conversations extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Conversations.class);

    public static final SecureRandom SECURE_RANDOM = new SecureRandom();
/**
    @Override
    public void onCreate() {
        super.onCreate();
        final var accountRepository = new AccountRepository(this);
        final var accountFuture =
                accountRepository.createAccountAsync(
                        Jid.ofEscaped("random@monocles.de"), "123456");
        Futures.addCallback(
                accountFuture,
                new FutureCallback<Account>() {
                    @Override
                    public void onSuccess(final Account account) {
                        LOGGER.info("Successfully added account {}", account.address);
                    }

                    @Override
                    public void onFailure(final @NonNull Throwable throwable) {
                        LOGGER.warn("Could not add account", throwable);
                        ConnectionPool.getInstance(Conversations.this).reconfigure();
                    }
                },
                MoreExecutors.directExecutor());
    }
    **/
}

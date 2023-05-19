package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.AccountCredentials;
import at.uibk.dps.rm.entity.model.Credentials;
import at.uibk.dps.rm.entity.model.ResourceProvider;
import lombok.experimental.UtilityClass;

/**
 * Utility class to instantiate objects that are linked to the account entity.
 *
 * @author matthi-g
 */
@UtilityClass
public class TestAccountProvider {
    public static Account createAccount(long accountId, String username, String password) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setUsername(username);
        account.setPassword(password);
        account.setIsActive(true);
        return account;
    }

    public static Credentials createCredentials(long credentialsId, ResourceProvider resourceProvider) {
        Credentials credentials = new Credentials();
        credentials.setCredentialsId(credentialsId);
        credentials.setAccessKey("accesskey");
        credentials.setSecretAccessKey("secretaccesskey");
        credentials.setSessionToken("sessiontoken");
        credentials.setResourceProvider(resourceProvider);
        return credentials;
    }

    public static AccountCredentials createAccountCredentials(long accountCredentialsId, Account account,
                                                              Credentials credentials) {
        AccountCredentials accountCredentials = new AccountCredentials();
        accountCredentials.setAccountCredentialsId(accountCredentialsId);
        accountCredentials.setAccount(account);
        accountCredentials.setCredentials(credentials);
        return accountCredentials;
    }

    public static Account createAccount(long accountId) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setUsername("username");
        account.setPassword("password");
        account.setIsActive(true);
        return account;
    }
}

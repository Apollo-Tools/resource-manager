package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.dto.account.RoleEnum;
import at.uibk.dps.rm.entity.model.*;
import lombok.experimental.UtilityClass;

/**
 * Utility class to instantiate objects that are linked to the account entity.
 *
 * @author matthi-g
 */
@UtilityClass
public class TestAccountProvider {
    public static Account createAccount(long accountId, String username, String password, String roleName) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setUsername(username);
        account.setPassword(password);
        account.setIsActive(true);
        Role role = new Role();
        role.setRoleId(11L);
        role.setRole(roleName);
        account.setRole(role);
        return account;
    }

    public static Account createAccount(long accountId, String username, String password) {
        return createAccount(accountId, username, password, "default");
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

    public static Role createRoleDefault() {
        Role role = new Role();
        role.setRoleId(2L);
        role.setRole(RoleEnum.DEFAULT.getValue());
        return role;
    }
}

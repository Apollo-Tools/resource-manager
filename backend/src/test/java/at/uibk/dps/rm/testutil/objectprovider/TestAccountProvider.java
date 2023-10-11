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

    public static Role createRoleAdmin() {
        Role role = new Role();
        role.setRoleId(1L);
        role.setRole(RoleEnum.ADMIN.getValue());
        return role;
    }

    public static Role createRoleDefault() {
        Role role = new Role();
        role.setRoleId(2L);
        role.setRole(RoleEnum.DEFAULT.getValue());
        return role;
    }

    public static AccountNamespace createAccountNamespace(long id, Account account, K8sNamespace namespace) {
        AccountNamespace accountNamespace = new AccountNamespace();
        accountNamespace.setAccountNamespaceId(id);
        accountNamespace.setAccount(account);
        accountNamespace.setNamespace(namespace);
        return accountNamespace;
    }

    public static AccountNamespace createAccountNamespace(long id, long accountId, long namespaceId) {
        Account account = createAccount(accountId);
        K8sNamespace namespace = TestResourceProviderProvider.createNamespace(namespaceId);
        return createAccountNamespace(id, account, namespace);
    }
}

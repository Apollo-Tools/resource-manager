package at.uibk.dps.rm.util.misc;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

/**
 * This class is used to hash and verify passwords.
 *
 * @author matthi-g
 */
public class PasswordUtility {

    private final Argon2 argon2;

    /**
     * Create an instance.
     */
    public PasswordUtility() {
        this.argon2 = Argon2Factory.create();
    }

    /**
     * Hash the password.
     *
     * @param password the password to hash
     * @return the hashed password
     */
    public String hashPassword(char[] password) {
        String hash;
        try {
            hash = argon2.hash(2, 15, 1, password);
        } finally {
            argon2.wipeArray(password);
        }
        return hash;
    }

    /**
     * Verify if password is equal to the hash.
     *
     * @param hash the hashed password
     * @param password the password to verify
     * @return true if the password is valid, else false
     */
    public boolean verifyPassword(String hash, char[] password) {
        boolean result;
        try {
            result = argon2.verify(hash, password);
        } finally {
            argon2.wipeArray(password);
        }
        return result;
    }
}

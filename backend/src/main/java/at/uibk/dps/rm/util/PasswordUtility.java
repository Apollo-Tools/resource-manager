package at.uibk.dps.rm.util;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class PasswordUtility {

    private final Argon2 argon2;

    public PasswordUtility() {
        this.argon2 = Argon2Factory.create();
    }

    public String hashPassword(char[] password) {
        String hash;
        try {
            hash = argon2.hash(2, 15, 1, password);
        } finally {
            argon2.wipeArray(password);
        }
        return hash;
    }

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

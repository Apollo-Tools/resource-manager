package at.uibk.dps.rm.util;

import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.auth.jwt.JWTAuth;
import lombok.Getter;

/**
 * This class is used to configure a jwt authentication and use it as provider later on.
 *
 * @author matthi-g
 */
@Getter
public class JWTAuthProvider {
    private final JWTAuth jwtAuth;

    /**
     * Configure and create an instance from vertx, the algorithm, pubSecKey and expiresInMinutes.
     *
     * @param vertx the vertx instance
     * @param algorithm the encryption algorithm
     * @param pubSecKey the secret key
     * @param expiresInMinutes the time span until a generated token expires in minutes
     */
    public JWTAuthProvider(Vertx vertx, String algorithm, String pubSecKey, int expiresInMinutes) {
        jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions()
            .addPubSecKey(new PubSecKeyOptions()
                .setAlgorithm(algorithm)
                .setBuffer(pubSecKey))
            .setJWTOptions(new JWTOptions()
                .setExpiresInMinutes(expiresInMinutes)));
    }
}

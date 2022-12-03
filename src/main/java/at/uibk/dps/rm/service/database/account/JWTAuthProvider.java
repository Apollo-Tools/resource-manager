package at.uibk.dps.rm.service.database.account;

import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.auth.jwt.JWTAuth;
import lombok.Getter;

@Getter
public class JWTAuthProvider {
    private final JWTAuth jwtAuth;

    public JWTAuthProvider(Vertx vertx, String algorithm, String pubSecKey, int expiresInMinutes) {
        jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions()
            .addPubSecKey(new PubSecKeyOptions()
                .setAlgorithm(algorithm)
                .setBuffer(pubSecKey))
            .setJWTOptions(new JWTOptions()
                .setExpiresInMinutes(expiresInMinutes)));
    }
}

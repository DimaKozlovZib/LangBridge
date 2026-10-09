package LangBridge.UserService.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

@EnableConfigurationProperties(AppProperties.class)
public class KeyProvider {
    private static final Logger log = LoggerFactory.getLogger(KeyProvider.class);
    private final AppProperties props;
    private final Environment env;
    private RSAKey rsaJwk;

    public KeyProvider(AppProperties props, Environment env) {
        this.props = props;
        this.env = env;
        initializeKeys();
    }

    private void initializeKeys() {
        try {
            if (props.jwt().privateKeyPem() != null && props.jwt().publicKeyPem() != null) {
                RSAPrivateKey priv = (RSAPrivateKey) parsePrivateKey(props.jwt().privateKeyPem());
                RSAPublicKey pub = (RSAPublicKey) parsePublicKey(props.jwt().publicKeyPem());
                this.rsaJwk = new RSAKey.Builder(pub).privateKey(priv).keyID(UUID.randomUUID().toString()).build();
            } else {
                boolean dev = false;
                for (String p : env.getActiveProfiles()) {
                    if ("dev".equalsIgnoreCase(p)) { dev = true; break; }
                }
                if (!dev) {
                    throw new IllegalStateException("RSA keys must be provided via configuration in non-dev profiles");
                }
                java.security.KeyPairGenerator gen = java.security.KeyPairGenerator.getInstance("RSA");
                gen.initialize(2048);
                KeyPair pair = gen.generateKeyPair();
                RSAPublicKey pub = (RSAPublicKey) pair.getPublic();
                RSAPrivateKey priv = (RSAPrivateKey) pair.getPrivate();
                this.rsaJwk = new RSAKey.Builder(pub).privateKey(priv).keyID(UUID.randomUUID().toString()).build();
                log.warn("No RSA keys configured. Generated ephemeral key pair for dev profile.");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize RSA keys", e);
        }
    }

    private static PrivateKey parsePrivateKey(String pem) throws Exception {
        String content = pem.replaceAll("-----BEGIN (.*)-----", "").replaceAll("-----END (.*)-----", "").replaceAll("\r?\n", "");
        byte[] decoded = Base64.getDecoder().decode(content);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    private static PublicKey parsePublicKey(String pem) throws Exception {
        String content = pem.replaceAll("-----BEGIN (.*)-----", "").replaceAll("-----END (.*)-----", "").replaceAll("\r?\n", "");
        byte[] decoded = Base64.getDecoder().decode(content);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    public JwtEncoder jwtEncoder() {
        var jwkSource = new ImmutableJWKSet<SecurityContext>(new JWKSet(rsaJwk));
        return new NimbusJwtEncoder(jwkSource);
    }

    public JwtDecoder jwtDecoder() {
        try {
            return NimbusJwtDecoder.withPublicKey(rsaJwk.toRSAPublicKey()).build();
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to create JwtDecoder", e);
        }
    }

    public JWKSet jwkSet() { return new JWKSet(rsaJwk.toPublicJWK()); }
    public String getKeyId() { return rsaJwk.getKeyID(); }
}

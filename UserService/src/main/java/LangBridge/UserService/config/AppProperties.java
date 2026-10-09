package LangBridge.UserService.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private final Security security = new Security();
    private final Jwt jwt = new Jwt();

    public Security security() { return security; }
    public Jwt jwt() { return jwt; }

    public static class Security {
        @Positive
        private int bcryptStrength = 12;

        public int bcryptStrength() { return bcryptStrength; }
        public void setBcryptStrength(int bcryptStrength) { this.bcryptStrength = bcryptStrength; }
    }

    public static class Jwt {
        @NotBlank
        private String issuer = "http://localhost";
        @Positive
        private long accessTtlSeconds = 900; // 15 minutes default
        private String privateKeyPem;
        private String publicKeyPem;

        public String issuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        public long accessTtlSeconds() { return accessTtlSeconds; }
        public void setAccessTtlSeconds(long accessTtlSeconds) { this.accessTtlSeconds = accessTtlSeconds; }
        public String privateKeyPem() { return privateKeyPem; }
        public void setPrivateKeyPem(String privateKeyPem) { this.privateKeyPem = privateKeyPem; }
        public String publicKeyPem() { return publicKeyPem; }
        public void setPublicKeyPem(String publicKeyPem) { this.publicKeyPem = publicKeyPem; }
    }
}

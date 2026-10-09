package LangBridge.UserService.config;

import LangBridge.UserService.service.UserService.JwtIssuer;
import LangBridge.UserService.service.UserService.JwtToken;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class JwtIssuerImpl implements JwtIssuer {
    private final JwtEncoder encoder;
    private final AppProperties props;
    private final KeyProvider keyProvider;

    public JwtIssuerImpl(JwtEncoder encoder, AppProperties props, KeyProvider keyProvider) {
        this.encoder = encoder;
        this.props = props;
        this.keyProvider = keyProvider;
    }

    @Override
    public JwtToken issueToken(UUID userId, String email) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.jwt().accessTtlSeconds());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.jwt().issuer())
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(now)
                .expiresAt(exp)
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(keyProvider.getKeyId())
                .build();
        var params = JwtEncoderParameters.from(header, claims);
        String token = encoder.encode(params).getTokenValue();
        return new JwtToken(token, props.jwt().accessTtlSeconds(), exp);
    }
}

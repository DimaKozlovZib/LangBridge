package LangBridge.UserService.web;

import com.nimbusds.jose.jwk.JWKSet;
import LangBridge.UserService.config.KeyProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JwksController {
    private final KeyProvider keyProvider;

    public JwksController(KeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    @GetMapping("/.well-known/jwks.json")
    public Object jwks() {
        return keyProvider.jwkSet().toJSONObject();
    }
}

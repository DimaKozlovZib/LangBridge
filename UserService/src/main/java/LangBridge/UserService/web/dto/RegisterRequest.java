package LangBridge.UserService.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;
import LangBridge.UserService.web.validation.PasswordUtf8Length;

/**
 * Registration request DTO.
 */
public record RegisterRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @PasswordUtf8Length(min = 8, max = 72) String password,
        @NotBlank @Size(max = 10) String nativeLang,
        @NotBlank @Size(max = 10) String targetLang,
        Short level
) {
    /**
     * Ensures language pair differs, case-insensitively.
     */
    @AssertTrue
    @JsonIgnore
    public boolean isLanguagePairValid() {
        if (nativeLang == null || targetLang == null) return true;
        return !nativeLang.equalsIgnoreCase(targetLang);
    }
}

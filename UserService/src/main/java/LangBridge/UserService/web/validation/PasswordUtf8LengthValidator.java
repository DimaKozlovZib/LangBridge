package LangBridge.UserService.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;

public class PasswordUtf8LengthValidator implements ConstraintValidator<PasswordUtf8Length, String> {
    private int min;
    private int max;

    @Override
    public void initialize(PasswordUtf8Length constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true; // use @NotBlank for null/blank
        int bytes = value.getBytes(StandardCharsets.UTF_8).length;
        return bytes >= min && bytes <= max;
    }
}

package LangBridge.UserService.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = PasswordUtf8LengthValidator.class)
public @interface PasswordUtf8Length {
    String message() default "password length must be between {min} and {max} bytes in UTF-8";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int min();
    int max();
}

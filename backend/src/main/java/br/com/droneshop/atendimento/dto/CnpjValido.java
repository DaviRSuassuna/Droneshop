package br.com.droneshop.atendimento.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.com.droneshop.util.Documentos;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

/** CNPJ com dígitos verificadores válidos; vazio fica para o @NotBlank. */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CnpjValido.Validador.class)
public @interface CnpjValido {

    String message() default "CNPJ inválido (back)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validador implements ConstraintValidator<CnpjValido, String> {
        @Override
        public boolean isValid(String valor, ConstraintValidatorContext ctx) {
            return valor == null || valor.isBlank() || Documentos.cnpjValido(valor);
        }
    }
}

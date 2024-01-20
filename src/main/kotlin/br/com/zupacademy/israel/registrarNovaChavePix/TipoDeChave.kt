package br.com.zupacademy.israel.registrarNovaChavePix

import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class TipoDeChave {
    CPF {
        override fun valida(chave: String?, context: ConstraintValidatorContext?): Boolean {
            if (chave.isNullOrBlank()) {
                context?.messageTemplate("Cpf não deve está em branco!!")
                return false
            }
            return CPFValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }
    },
    CELULAR {
        override fun valida(chave: String?, context: ConstraintValidatorContext?): Boolean {
            if (chave.isNullOrBlank()) {
                context?.messageTemplate("Telefone não deve está em branco")
                return false
            }
            if (!chave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())) {
                context?.messageTemplate("Telefone formato inválido!!")
                return false
            }
            return true
        }
    },
    EMAIL {
        override fun valida(chave: String?, context: ConstraintValidatorContext?): Boolean {
            if (chave.isNullOrBlank()) {
                context?.messageTemplate("Email não deve está em branco!!")
                return false
            }
            val run = EmailValidator().run {
                initialize(null)
                isValid(chave, null)
            }
            return run
        }
    },
    ALEATORIA {
        override fun valida(chave: String?, context: ConstraintValidatorContext?): Boolean {
            if (chave!!.isNotBlank()) {
                context?.messageTemplate("Quando o tipo de chave é aleatório, não se espera uma chave preenchida!!")
                return false
            }
            return true
        }
    };

    abstract fun valida(chave: String?, context: ConstraintValidatorContext?): Boolean
}
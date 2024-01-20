package br.com.zupacademy.israel.removerChavePix

import br.com.zupacademy.israel.registrarNovaChavePix.validator.ValidUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class RemoverChavePix(
    @ValidUUID
    @field:NotBlank
    val pixId: String,
    @ValidUUID
    @field:NotBlank
    val idCliente: String
)
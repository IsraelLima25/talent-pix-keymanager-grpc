package br.com.zupacademy.israel.registrarNovaChavePix

import br.com.zupacademy.israel.TipoTitular
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
data class Titular(
    @field:NotBlank
    val nome: String,
    @field:NotBlank
    val documento: String,
    @field:NotNull
    val tipoTitular: TipoTitular?
) {

}

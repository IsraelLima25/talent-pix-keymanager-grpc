package br.com.zupacademy.israel.registrarNovaChavePix

import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank

@Embeddable
class ContaAssociada(
    @field:NotBlank
    val instituicao: String,
    @field:NotBlank
    val nomeTitular: String,
    @field:NotBlank
    val cpfTitular: String,
    @field:NotBlank
    val agencia: String,
    @field:NotBlank
    val numeroConta: String,
    @field:NotBlank
    val ispb: String
) {

}

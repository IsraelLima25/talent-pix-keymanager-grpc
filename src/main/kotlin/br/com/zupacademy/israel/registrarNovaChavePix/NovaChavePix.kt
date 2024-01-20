package br.com.zupacademy.israel.registrarNovaChavePix

import br.com.zupacademy.israel.TipoConta
import br.com.zupacademy.israel.TipoTitular
import br.com.zupacademy.israel.compartilhado.apiExterna.bcb.*
import br.com.zupacademy.israel.registrarNovaChavePix.validator.ValidPixKey
import br.com.zupacademy.israel.registrarNovaChavePix.validator.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class NovaChavePix(
    @ValidUUID
    @field:NotBlank
    val idCliente: String?,
    @field:NotNull
    val tipoDeChave: TipoDeChave?,
    @field:NotBlank
    @field:Size(max = 77)
    val chave: String,
    @field:NotNull
    val tipoConta: TipoConta?,
    @field:Valid
    @field:NotNull
    val titular: Titular

) {
    fun toModel(conta: ContaAssociada): ChavePix {
        return ChavePix(
            clientId = UUID.fromString(this.idCliente),
            tipoDeChave = TipoDeChave.valueOf(this.tipoDeChave!!.name),
            chave = this.chave,
            tipoConta = TipoConta.valueOf(this.tipoConta!!.name),
            conta = conta
        )
    }

    fun toCriarChavePixBCBRequest(conta: ContaAssociada): ChavePixBCBRequest {
        return ChavePixBCBRequest(
            keyType = when (tipoDeChave) {
                TipoDeChave.EMAIL -> KeyType.EMAIL
                TipoDeChave.CELULAR -> KeyType.PHONE
                TipoDeChave.CPF -> KeyType.CPF
                else -> KeyType.RANDOM
            }, key = chave, BankAccount(
                participant = conta.ispb,
                branch = conta.agencia,
                accountNumber = conta.numeroConta,
                accountType = when (tipoConta) {
                    TipoConta.CONTA_CORRENTE -> AccountType.CACC
                    else -> AccountType.SVGS
                }
            ), owner = Owner(
                type = when (titular.tipoTitular) {
                    TipoTitular.PESSOA_FISICA -> TypeOwner.NATURAL_PERSON
                    else -> TypeOwner.LEGAL_PERSON
                }, name = titular.nome, taxIdNumber = titular.documento
            )
        )
    }

}

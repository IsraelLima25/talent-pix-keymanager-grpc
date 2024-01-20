package br.com.zupacademy.israel.compartilhado.apiExterna.bcb

import br.com.zupacademy.israel.TipoConta
import br.com.zupacademy.israel.compartilhado.Instituicoes
import br.com.zupacademy.israel.consultarChavePix.ChavePixInfo
import br.com.zupacademy.israel.registrarNovaChavePix.ContaAssociada
import br.com.zupacademy.israel.registrarNovaChavePix.TipoDeChave
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime

@Client("\${bcb.pix.url}")
interface BcbClient {

    @Post("/api/v1/pix/keys", consumes = [MediaType.APPLICATION_XML], produces = [MediaType.APPLICATION_XML])
    fun criarChavePixBCB(@Body criarChavepixBCBRequest: ChavePixBCBRequest): HttpResponse<CreatePixKeyResponse>

    @Delete("/api/v1/pix/keys/{key}", consumes = [MediaType.APPLICATION_XML], produces = [MediaType.APPLICATION_XML])
    fun removerChavePixBcb(
        @PathVariable key: String,
        @Body deletePixKeyRequest: DeletePixKeyRequest
    ): HttpResponse<DeletePixKeyResponse>

    @Get(
        "/api/v1/pix/keys/{key}",
        consumes = [MediaType.APPLICATION_XML]
    )
    fun buscarPorChave(@PathVariable key: String): HttpResponse<PixKeyDetailsResponse>

}

data class ChavePixBCBRequest(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner
)

data class BankAccount(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
)

data class Owner(
    val type: TypeOwner,
    val name: String,
    val taxIdNumber: String
)

data class DeletePixKeyRequest(
    val key: String,
    val participant: String
)

data class DeletePixKeyResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
)

data class PixKeyDetailsResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
) {

    fun toModel(): ChavePixInfo {
        return ChavePixInfo(
            tipo = when (this.keyType) {
                KeyType.CPF -> TipoDeChave.CPF
                KeyType.PHONE -> TipoDeChave.CELULAR
                KeyType.EMAIL -> TipoDeChave.EMAIL
                else -> TipoDeChave.ALEATORIA
            },
            chave = this.key,
            tipoDeConta = when (this.bankAccount.accountType) {
                AccountType.CACC -> TipoConta.CONTA_CORRENTE
                AccountType.SVGS -> TipoConta.CONTA_POUPANCA
            },
            conta = ContaAssociada(
                instituicao = Instituicoes.nome(bankAccount.participant),
                nomeTitular = owner.name,
                cpfTitular = owner.taxIdNumber,
                agencia = bankAccount.branch,
                numeroConta = bankAccount.accountNumber,
                ispb = bankAccount.participant
            )
        )
    }
}

enum class AccountType {
    CACC, SVGS
}

enum class KeyType {
    CPF, CNPJ, PHONE, EMAIL, RANDOM
}

enum class TypeOwner {
    NATURAL_PERSON, LEGAL_PERSON
}
package br.com.zupacademy.israel.compartilhado.apiExterna.itau

import br.com.zupacademy.israel.registrarNovaChavePix.ContaAssociada
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client


@Client("\${itau.contas.url}")
interface ItauClient {

    @Get("/api/v1/clientes/{clienteId}/contas{?tipo}")
    fun buscarContaPorTipo(
        @PathVariable clienteId: String,
        @QueryValue tipo: String
    ): HttpResponse<DadosDaContaResponse>
}

class DadosDaContaResponse(
    val tipo: String,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
) {
    fun toModel(): ContaAssociada {
        return ContaAssociada(
            instituicao = this.instituicao.nome,
            nomeTitular = this.titular.nome,
            cpfTitular = this.titular.cpf,
            agencia = this.agencia,
            numeroConta = this.numero,
            ispb = instituicao.ispb
        )
    }
}

class TitularResponse(val id: String, val nome: String, val cpf: String)
class InstituicaoResponse(val nome: String, val ispb: String)

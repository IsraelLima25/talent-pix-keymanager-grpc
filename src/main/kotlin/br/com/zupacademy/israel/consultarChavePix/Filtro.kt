package br.com.zupacademy.israel.consultarChavePix

import br.com.zupacademy.israel.compartilhado.apiExterna.bcb.BcbClient
import br.com.zupacademy.israel.exception.ChavePixInexistenteException
import br.com.zupacademy.israel.registrarNovaChavePix.ChavePixRepository
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import java.util.*

@Introspected
sealed class Filtro {
    abstract fun filtrar(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixInfo

    @Introspected
    data class PorPixId(
        val clienteId: String,
        val pixId: String
    ) : Filtro() {

        fun pixIdAsUuid() = UUID.fromString(pixId)
        fun clienteIdAsUuid() = UUID.fromString(clienteId)

        override fun filtrar(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixInfo {

            if (pixId.isBlank() || clienteId.isBlank()) {
                throw IllegalArgumentException("Os dados não devem estar em branco")
            }

            return repository
                .findById(pixIdAsUuid())
                .filter { it.pertenceAo(clienteIdAsUuid()) }
                .map(ChavePixInfo::of)
                .orElseThrow { ChavePixInexistenteException("chave pix não encontrada") }
        }

    }

    @Introspected
    data class PorChave(val chave: String) : Filtro() {
        override fun filtrar(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixInfo {

            if (chave.isBlank() || chave.length > 77) {
                throw IllegalArgumentException("Os dados não devem estar em branco e não devem ser maior que 77 caracteres")
            }

            return repository.findByChave(chave)
                .map(ChavePixInfo::of)
                .orElseGet {
                    val response = bcbClient.buscarPorChave(chave)
                    when (response.status) {
                        HttpStatus.OK -> response.body()?.toModel()
                        else -> throw ChavePixInexistenteException("Chave pix não encontrada")
                    }
                }
        }
    }
}

package br.com.zupacademy.israel.removerChavePix

import br.com.zupacademy.israel.compartilhado.apiExterna.bcb.BcbClient
import br.com.zupacademy.israel.compartilhado.apiExterna.bcb.DeletePixKeyRequest
import br.com.zupacademy.israel.exception.ChavePixInexistenteException
import br.com.zupacademy.israel.registrarNovaChavePix.ChavePixRepository
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class RemoverChavePixService(
    val chavePixRepository: ChavePixRepository,
    val bcbClient: BcbClient
) {

    val LOGGER = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun remover(@Valid removerChavePix: RemoverChavePix): Unit {

        val chavePixUUID = UUID.fromString(removerChavePix.pixId)
        val clientIdUUID = UUID.fromString(removerChavePix.idCliente)

        val possivelKey = chavePixRepository
            .findByIdAndClientId(chavePixUUID, clientIdUUID)
            .orElseThrow {
                ChavePixInexistenteException("Chave pix não encontrada")
            }

        LOGGER.info("Solicitando remoção da chave pix no banco central")
        val deletePixKeyRequest = DeletePixKeyRequest(possivelKey.chave, possivelKey.conta.ispb)
        val response = bcbClient.removerChavePixBcb(possivelKey.chave, deletePixKeyRequest)

        if (response.status() != HttpStatus.OK) {
            throw ChavePixInexistenteException("Chave pix não encontrada")
        }

        LOGGER.info("Removendo chave da api")
        chavePixRepository.deleteById(UUID.fromString(removerChavePix.pixId))
    }
}


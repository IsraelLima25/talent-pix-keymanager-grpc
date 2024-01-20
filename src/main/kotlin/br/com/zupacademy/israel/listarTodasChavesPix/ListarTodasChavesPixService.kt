package br.com.zupacademy.israel.listarTodasChavesPix

import br.com.zupacademy.israel.registrarNovaChavePix.ChavePix
import br.com.zupacademy.israel.registrarNovaChavePix.ChavePixRepository
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class ListarTodasChavesPixService(@Inject val repository: ChavePixRepository) {

    fun listar(@Valid todasChavesPixFilter: ListarTodasChavesPixFilter): List<ChavePix> {

        val clientIdUUid = UUID.fromString(todasChavesPixFilter.clientId)
        val lista = repository.findAllByClientId(clientIdUUid)
        return lista;
    }
}

package br.com.zupacademy.israel.consultarChavePix

import br.com.zupacademy.israel.ConsultarChavePixRequest
import br.com.zupacademy.israel.ConsultarChavePixRequest.FiltroCase.*

fun ConsultarChavePixRequest.toModel(): Filtro {

    val filtro: Filtro = when (filtroCase!!) {
        PIXID -> pixId.let {
            Filtro.PorPixId(clienteId = it.clientId, pixId = it.pixId)
        }
        else -> Filtro.PorChave(chave)
    }
    return filtro
}
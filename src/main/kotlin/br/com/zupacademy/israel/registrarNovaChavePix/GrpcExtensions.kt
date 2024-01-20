package br.com.zupacademy.israel.registrarNovaChavePix

import br.com.zupacademy.israel.RegistraChavePixRequest
import br.com.zupacademy.israel.TipoChave
import br.com.zupacademy.israel.TipoConta
import br.com.zupacademy.israel.TipoTitular


fun RegistraChavePixRequest.toModel(): NovaChavePix {

    return NovaChavePix(
        idCliente = idCliente,
        tipoDeChave = when (tipoChave) {
            TipoChave.UNKNOW_TIPO_CHAVE -> null
            else -> TipoDeChave.valueOf(tipoChave.name)
        }, chave = chave, tipoConta = when (tipoConta) {
            TipoConta.UNKNOW_TIPO_CONTA -> null
            else -> TipoConta.valueOf(tipoConta.name)
        }, titular = Titular(
            nome = titular.nome,
            documento = titular.documento,
            tipoTitular = when (titular.tipoTitular) {
                TipoTitular.UNKNOW_TIPO_PESSOA -> null
                else -> TipoTitular.valueOf(titular.tipoTitular.name)
            }
        )
    )
}
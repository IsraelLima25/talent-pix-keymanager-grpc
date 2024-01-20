package br.com.zupacademy.israel.listarTodasChavesPix

import br.com.zupacademy.israel.ListarTodasChavesPixResponse
import br.com.zupacademy.israel.TipoChave
import br.com.zupacademy.israel.registrarNovaChavePix.ChavePix
import br.com.zupacademy.israel.registrarNovaChavePix.TipoDeChave
import com.google.protobuf.Timestamp
import java.time.ZoneId

class ListarTodasChavesPixResponseConvert {

    fun convert(lista: List<ChavePix>): List<ListarTodasChavesPixResponse.ChavePix> {

        return lista.map { chavePix ->
            ListarTodasChavesPixResponse.ChavePix
                .newBuilder()
                .setPixId(chavePix.id.toString())
                .setClientId(chavePix.clientId.toString())
                .setTipoDeChave(
                    when (chavePix.tipoDeChave) {
                        TipoDeChave.CPF -> TipoChave.CPF
                        TipoDeChave.CELULAR -> TipoChave.CELULAR
                        TipoDeChave.EMAIL -> TipoChave.EMAIL
                        TipoDeChave.ALEATORIA -> TipoChave.ALEATORIA
                    }
                )
                .setValorChave(chavePix.chave)
                .setTipoDeConta(chavePix.tipoConta)
                .setCriadaEm(
                    Timestamp.newBuilder()
                        .setSeconds(chavePix.criadaEm.atZone(ZoneId.of("UTC")).toInstant().epochSecond)
                        .setNanos(chavePix.criadaEm.nano).build()
                )
                .build()

        }.toList()
    }
}



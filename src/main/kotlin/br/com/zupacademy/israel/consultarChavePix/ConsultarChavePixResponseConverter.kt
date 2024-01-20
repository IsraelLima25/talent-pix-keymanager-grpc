package br.com.zupacademy.israel.consultarChavePix

import br.com.zupacademy.israel.ConsultarChavePixResponse
import br.com.zupacademy.israel.TipoChave
import br.com.zupacademy.israel.TipoConta
import com.google.protobuf.Timestamp
import java.time.ZoneId

class ConsultarChavePixResponseConverter {
    fun convert(chaveInfo: ChavePixInfo): ConsultarChavePixResponse {
        return ConsultarChavePixResponse.newBuilder()
            .setClienteId(chaveInfo.clienteId?.toString() ?: "")
            .setPixId(chaveInfo.pixId?.toString() ?: "")
            .setChave(
                ConsultarChavePixResponse.ChavePix
                    .newBuilder()
                    .setTipo(TipoChave.valueOf(chaveInfo.tipo.name))
                    .setChave(chaveInfo.chave)
                    .setConta(
                        ConsultarChavePixResponse.ChavePix.ContaInfo.newBuilder()
                            .setTipo(TipoConta.valueOf(chaveInfo.tipoDeConta.name))
                            .setInstituicao(chaveInfo.conta.instituicao)
                            .setNomeDoTitular(chaveInfo.conta.nomeTitular)
                            .setCpfDoTitular(chaveInfo.conta.cpfTitular)
                            .setAgencia(chaveInfo.conta.agencia)
                            .setNumeroDaConta(chaveInfo.conta.numeroConta)
                            .build()
                    )
                    .setCriadaEm(chaveInfo.registradaEm.let {
                        val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                        Timestamp.newBuilder()
                            .setSeconds(createdAt.epochSecond)
                            .setNanos(createdAt.nano)
                            .build()
                    })
            )
            .build()
    }
}
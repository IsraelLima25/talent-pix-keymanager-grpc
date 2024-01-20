package br.com.zupacademy.israel.registrarNovaChavePix


import br.com.zupacademy.israel.compartilhado.apiExterna.bcb.BcbClient
import br.com.zupacademy.israel.compartilhado.apiExterna.itau.ItauClient
import br.com.zupacademy.israel.exception.ChavePixExistenteException
import br.com.zupacademy.israel.exception.ClienteNaoEncontradoApiItauException
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    @Inject val repository: ChavePixRepository,
    @Inject val itauClient: ItauClient,
    @Inject val bcbClient: BcbClient
) {

    val LOGGER = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registrarNovaChavePix(@Valid novaChave: NovaChavePix): ChavePix {

        if (repository.existsByChave(novaChave.chave)) {
            throw ChavePixExistenteException("Chave Pix já existente!!")
        }

        LOGGER.info("Consultando api do itaú")
        val response = itauClient.buscarContaPorTipo(novaChave.idCliente!!, novaChave.tipoConta!!.name)
        val conta = response.body()?.toModel() ?: throw ClienteNaoEncontradoApiItauException("Cliente não encontrado no itaú")

        val chave = novaChave.toModel(conta)
        repository.save(chave)

        val chavePixBCBRequest = novaChave.toCriarChavePixBCBRequest(conta)

        LOGGER.info("Registrando chave pix no banco central")
        val bcbResponse = bcbClient.criarChavePixBCB(chavePixBCBRequest)
        if(bcbResponse.status != HttpStatus.CREATED){
            throw ChavePixExistenteException("chave pix já existe")
        }

        chave.atualizar(bcbResponse.body()!!.key)

        return chave
    }
}
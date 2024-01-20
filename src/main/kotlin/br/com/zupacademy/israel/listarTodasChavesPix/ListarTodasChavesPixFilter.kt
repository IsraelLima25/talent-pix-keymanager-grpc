package br.com.zupacademy.israel.listarTodasChavesPix

import br.com.zupacademy.israel.registrarNovaChavePix.validator.ValidUUID
import io.micronaut.core.annotation.Introspected

@Introspected
class ListarTodasChavesPixFilter(@field:ValidUUID val clientId: String)
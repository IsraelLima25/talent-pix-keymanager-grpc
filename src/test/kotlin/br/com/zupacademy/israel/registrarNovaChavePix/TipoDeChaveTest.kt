package br.com.zupacademy.israel.registrarNovaChavePix

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TipoDeChaveTest {


    @Nested
    inner class CPF() {

        @Test
        fun `deve ser invalido quando valor em branco`() {
            assertFalse(TipoDeChave.CPF.valida(chave = "", context = null))
        }

        @Test
        fun `deve ser um tipo valido`() {
            assertTrue(TipoDeChave.CPF.valida(chave = "45242085002", context = null))
        }

        @Test
        fun `deve ser invalido quando formato invalido`() {
            assertFalse(TipoDeChave.CPF.valida(chave = "452420802", context = null))
        }

        @Test
        fun `deve ser um tipo invalido`() {
            assertTrue(TipoDeChave.CPF.valida(chave = "45242085002", context = null))
        }
    }

    @Nested
    inner class CELULAR() {

        @Test
        fun `deve ser invalido quando valor em branco`() {
            assertFalse(TipoDeChave.CELULAR.valida(chave = "", context = null))
        }

        @Test
        fun `deve ser invalido quando formato invalido`() {
            assertFalse(TipoDeChave.CELULAR.valida(chave = "71983369587", context = null))
        }

        @Test
        fun `deve um tipo valido`() {
            assertTrue(TipoDeChave.CELULAR.valida(chave = "+5571983306584", context = null))
        }
    }

    @Nested
    inner class EMAIL() {

        @Test
        fun `deve ser invalido quando valor em branco`() {
            assertFalse(TipoDeChave.EMAIL.valida(chave = "", context = null))
        }

        @Test
        fun `deve ser invalido quando tipo invalido`() {
            assertFalse(TipoDeChave.EMAIL.valida(chave = "sizenando.gmail.com", context = null))
        }

        @Test
        fun `deve ser um tipo valido`() {
            assertTrue(TipoDeChave.EMAIL.valida(chave = "sizenando@gmail.com", context = null))
        }
    }

    @Nested
    inner class ALEATORIA() {

        @Test
        fun `deve ser valido quando chave aleatoria for nula ou vazia`() {
            assertTrue(TipoDeChave.ALEATORIA.valida(chave = "", context = null))
        }

        @Test
        fun `deve ser invalido quando chave possui valor`() {
            assertFalse(TipoDeChave.ALEATORIA.valida(chave = "qualquer valor", context = null))
        }
    }
}
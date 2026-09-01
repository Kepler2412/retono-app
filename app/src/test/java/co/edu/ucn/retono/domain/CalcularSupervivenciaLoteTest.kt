package co.edu.ucn.retono.domain

import app.cash.turbine.test
import co.edu.ucn.retono.domain.model.EstadoSync
import co.edu.ucn.retono.domain.model.EstadoVital
import co.edu.ucn.retono.domain.model.Monitoreo
import co.edu.ucn.retono.domain.model.ResultadoSupervivencia
import co.edu.ucn.retono.domain.model.Siembra
import co.edu.ucn.retono.domain.repository.MonitoreoRepository
import co.edu.ucn.retono.domain.repository.SiembraRepository
import co.edu.ucn.retono.domain.usecase.CalcularSupervivenciaLote
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas de la regla de negocio central del proyecto.
 *
 * Corren en la JVM sin emulador, porque la capa de dominio no depende de Android.
 * Esa es la ventaja concreta de haber separado las capas: el ciclo de
 * retroalimentación de estas pruebas es de segundos, no de minutos.
 */
class CalcularSupervivenciaLoteTest {

    private lateinit var siembraRepository: SiembraRepository
    private lateinit var monitoreoRepository: MonitoreoRepository
    private lateinit var casoDeUso: CalcularSupervivenciaLote

    private val loteId = "lote-01"

    @Before
    fun configurar() {
        siembraRepository = mockk()
        monitoreoRepository = mockk()
        casoDeUso = CalcularSupervivenciaLote(siembraRepository, monitoreoRepository)
    }

    @Test
    fun `cuenta solo el monitoreo mas reciente de cada individuo`() = runTest {
        // Un árbol visitado tres veces debe aportar una observación, no tres.
        // La primera visita lo halló vivo, la última muerto: cuenta como muerto.
        dadoUnLoteCon(
            siembras = listOf(siembra("a"), siembra("b")),
            monitoreos = listOf(
                monitoreo("m1", "a", fecha = 100, estado = EstadoVital.VIVO),
                monitoreo("m2", "a", fecha = 200, estado = EstadoVital.VIVO),
                monitoreo("m3", "a", fecha = 300, estado = EstadoVital.MUERTO),
                monitoreo("m4", "b", fecha = 300, estado = EstadoVital.VIVO)
            )
        )

        casoDeUso(loteId).test {
            val resultado = awaitItem()
            assertEquals(2, resultado.observados)
            assertEquals(1, resultado.vivos)
            assertEquals(1, resultado.muertos)
            assertEquals(0.5, resultado.tasaSupervivencia, TOLERANCIA)
            awaitComplete()
        }
    }

    @Test
    fun `excluye del denominador los individuos no encontrados`() = runTest {
        // Un individuo no hallado no es evidencia de muerte: puede haber error de
        // localización. Contarlo como muerto subestimaría la supervivencia.
        dadoUnLoteCon(
            siembras = listOf(siembra("a"), siembra("b"), siembra("c"), siembra("d")),
            monitoreos = listOf(
                monitoreo("m1", "a", fecha = 100, estado = EstadoVital.VIVO),
                monitoreo("m2", "b", fecha = 100, estado = EstadoVital.VIVO),
                monitoreo("m3", "c", fecha = 100, estado = EstadoVital.MUERTO),
                monitoreo("m4", "d", fecha = 100, estado = EstadoVital.NO_ENCONTRADO)
            )
        )

        casoDeUso(loteId).test {
            val resultado = awaitItem()
            assertEquals("El no encontrado no entra al denominador", 3, resultado.observados)
            assertEquals(1, resultado.noEncontrados)
            assertEquals(2.0 / 3.0, resultado.tasaSupervivencia, TOLERANCIA)
            awaitComplete()
        }
    }

    @Test
    fun `marca el resultado como no confiable si la cobertura es baja`() = runTest {
        // Una tasa del 100 % sobre el 20 % del lote no es un resultado: es una
        // muestra insuficiente, y reportarla sin advertencia sería engañoso.
        dadoUnLoteCon(
            siembras = (1..10).map { siembra("s$it") },
            monitoreos = listOf(
                monitoreo("m1", "s1", fecha = 100, estado = EstadoVital.VIVO),
                monitoreo("m2", "s2", fecha = 100, estado = EstadoVital.VIVO)
            )
        )

        casoDeUso(loteId).test {
            val resultado = awaitItem()
            assertEquals(1.0, resultado.tasaSupervivencia, TOLERANCIA)
            assertEquals(0.2, resultado.cobertura, TOLERANCIA)
            assertFalse("Cobertura del 20 % no puede considerarse confiable", resultado.esConfiable)
            awaitComplete()
        }
    }

    @Test
    fun `marca el resultado como confiable al superar el umbral de cobertura`() = runTest {
        dadoUnLoteCon(
            siembras = (1..10).map { siembra("s$it") },
            monitoreos = (1..9).map {
                monitoreo("m$it", "s$it", fecha = 100, estado = EstadoVital.VIVO)
            }
        )

        casoDeUso(loteId).test {
            val resultado = awaitItem()
            assertEquals(0.9, resultado.cobertura, TOLERANCIA)
            assertTrue(resultado.esConfiable)
            awaitComplete()
        }
    }

    @Test
    fun `no divide por cero cuando el lote aun no tiene monitoreos`() = runTest {
        dadoUnLoteCon(
            siembras = listOf(siembra("a"), siembra("b")),
            monitoreos = emptyList()
        )

        casoDeUso(loteId).test {
            val resultado = awaitItem()
            assertEquals(0, resultado.observados)
            assertEquals(0.0, resultado.tasaSupervivencia, TOLERANCIA)
            assertEquals(2, resultado.totalSembrado)
            awaitComplete()
        }
    }

    @Test
    fun `el umbral de cobertura confiable es del ochenta por ciento`() {
        assertEquals(0.8, ResultadoSupervivencia.UMBRAL_COBERTURA_CONFIABLE, TOLERANCIA)
    }

    // ----------------- utilidades de prueba -----------------

    private fun dadoUnLoteCon(siembras: List<Siembra>, monitoreos: List<Monitoreo>) {
        every { siembraRepository.observarSiembrasDeLote(loteId) } returns flowOf(siembras)
        every { monitoreoRepository.observarMonitoreosDeLote(loteId) } returns flowOf(monitoreos)
    }

    private fun siembra(id: String) = Siembra(
        id = id,
        loteId = loteId,
        especieId = "esp-01",
        latitud = 6.2442,
        longitud = -75.5812,
        precisionMetros = 5f,
        fechaSiembra = 0L,
        rutaFotoLocal = null,
        observaciones = "",
        estadoSync = EstadoSync.SINCRONIZADO,
        actualizadoEn = 0L
    )

    private fun monitoreo(
        id: String,
        siembraId: String,
        fecha: Long,
        estado: EstadoVital
    ) = Monitoreo(
        id = id,
        siembraId = siembraId,
        fecha = fecha,
        estadoVital = estado,
        alturaCm = 50.0,
        diametroMm = 10.0,
        observaciones = "",
        estadoSync = EstadoSync.SINCRONIZADO,
        actualizadoEn = fecha
    )

    private companion object {
        const val TOLERANCIA = 0.0001
    }
}

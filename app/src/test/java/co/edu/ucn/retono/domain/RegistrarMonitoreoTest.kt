package co.edu.ucn.retono.domain

import co.edu.ucn.retono.domain.model.EstadoVital
import co.edu.ucn.retono.domain.model.Monitoreo
import co.edu.ucn.retono.domain.repository.MonitoreoRepository
import co.edu.ucn.retono.domain.usecase.RegistrarMonitoreo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Reglas de negocio del registro de monitoreos.
 *
 * La validación importante es la coherencia entre estado vital y medidas: un
 * árbol muerto no puede tener altura. Sin esta regla, el conjunto de datos
 * acabaría con contradicciones que invalidarían cualquier análisis posterior
 * de crecimiento.
 */
class RegistrarMonitoreoTest {

    private lateinit var repository: MonitoreoRepository
    private lateinit var casoDeUso: RegistrarMonitoreo

    @Before
    fun configurar() {
        repository = mockk(relaxed = true)
        casoDeUso = RegistrarMonitoreo(repository)
    }

    @Test
    fun `registra un individuo vivo con sus medidas`() = runTest {
        val capturado = slot<Monitoreo>()
        coEvery { repository.registrarMonitoreo(capture(capturado)) } returns Unit

        val resultado = casoDeUso(
            parametros(EstadoVital.VIVO, alturaCm = 145.0, diametroMm = 22.0)
        )

        assertTrue(resultado.isSuccess)
        assertEquals(EstadoVital.VIVO, capturado.captured.estadoVital)
        assertEquals(145.0, capturado.captured.alturaCm!!, 0.001)
    }

    @Test
    fun `rechaza medidas en un individuo muerto`() = runTest {
        val resultado = casoDeUso(
            parametros(EstadoVital.MUERTO, alturaCm = 90.0, diametroMm = null)
        )

        assertTrue(resultado.isFailure)
        assertTrue(
            resultado.exceptionOrNull() is RegistrarMonitoreo.MedidasIncoherentesException
        )
        coVerify(exactly = 0) { repository.registrarMonitoreo(any()) }
    }

    @Test
    fun `rechaza medidas en un individuo no encontrado`() = runTest {
        val resultado = casoDeUso(
            parametros(EstadoVital.NO_ENCONTRADO, alturaCm = null, diametroMm = 15.0)
        )

        assertTrue(resultado.isFailure)
        coVerify(exactly = 0) { repository.registrarMonitoreo(any()) }
    }

    @Test
    fun `acepta un individuo muerto sin medidas`() = runTest {
        val resultado = casoDeUso(
            parametros(EstadoVital.MUERTO, alturaCm = null, diametroMm = null)
        )

        assertTrue(resultado.isSuccess)
        coVerify(exactly = 1) { repository.registrarMonitoreo(any()) }
    }

    @Test
    fun `rechaza una altura no positiva`() = runTest {
        val resultado = casoDeUso(
            parametros(EstadoVital.VIVO, alturaCm = 0.0, diametroMm = null)
        )

        assertTrue(resultado.isFailure)
    }

    @Test
    fun `el monitoreo nace en estado pendiente de sincronizar`() = runTest {
        val capturado = slot<Monitoreo>()
        coEvery { repository.registrarMonitoreo(capture(capturado)) } returns Unit

        casoDeUso(parametros(EstadoVital.VIVO, alturaCm = 50.0, diametroMm = null))

        assertEquals(
            co.edu.ucn.retono.domain.model.EstadoSync.PENDIENTE,
            capturado.captured.estadoSync
        )
    }

    @Test
    fun `cada monitoreo recibe un identificador distinto`() = runTest {
        val primero = casoDeUso(parametros(EstadoVital.VIVO, 50.0, null)).getOrThrow()
        val segundo = casoDeUso(parametros(EstadoVital.VIVO, 50.0, null)).getOrThrow()

        assertTrue("Los identificadores deben ser únicos", primero != segundo)
    }

    private fun parametros(
        estado: EstadoVital,
        alturaCm: Double?,
        diametroMm: Double?
    ) = RegistrarMonitoreo.Parametros(
        siembraId = "siembra-01",
        estadoVital = estado,
        alturaCm = alturaCm,
        diametroMm = diametroMm,
        observaciones = ""
    )
}

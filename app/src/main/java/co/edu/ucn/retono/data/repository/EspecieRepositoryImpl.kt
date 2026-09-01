package co.edu.ucn.retono.data.repository

import co.edu.ucn.retono.data.local.dao.EspecieDao
import co.edu.ucn.retono.domain.model.Especie
import co.edu.ucn.retono.domain.repository.EspecieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EspecieRepositoryImpl @Inject constructor(
    private val dao: EspecieDao
) : EspecieRepository {

    override fun observarEspecies(): Flow<List<Especie>> =
        dao.observarTodas().map { lista ->
            lista.map {
                Especie(
                    id = it.id,
                    nombreCientifico = it.nombreCientifico,
                    nombreComun = it.nombreComun,
                    habito = it.habito
                )
            }
        }
}

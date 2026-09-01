package co.edu.ucn.retono.data.repository

import co.edu.ucn.retono.data.local.entity.LoteEntity
import co.edu.ucn.retono.data.local.entity.MonitoreoEntity
import co.edu.ucn.retono.data.local.entity.SiembraEntity
import co.edu.ucn.retono.domain.model.EstadoSync
import co.edu.ucn.retono.domain.model.EstadoVital
import co.edu.ucn.retono.domain.model.Lote
import co.edu.ucn.retono.domain.model.Monitoreo
import co.edu.ucn.retono.domain.model.Siembra

/** Traducción entre entidades de persistencia y modelos de dominio. */

fun SiembraEntity.aDominio() = Siembra(
    id = id,
    loteId = loteId,
    especieId = especieId,
    latitud = latitud,
    longitud = longitud,
    precisionMetros = precisionMetros,
    fechaSiembra = fechaSiembra,
    rutaFotoLocal = rutaFotoLocal,
    observaciones = observaciones,
    estadoSync = EstadoSync.valueOf(estadoSync),
    actualizadoEn = actualizadoEn
)

fun Siembra.aEntidad() = SiembraEntity(
    id = id,
    loteId = loteId,
    especieId = especieId,
    latitud = latitud,
    longitud = longitud,
    precisionMetros = precisionMetros,
    fechaSiembra = fechaSiembra,
    rutaFotoLocal = rutaFotoLocal,
    observaciones = observaciones,
    estadoSync = estadoSync.name,
    actualizadoEn = actualizadoEn
)

fun MonitoreoEntity.aDominio() = Monitoreo(
    id = id,
    siembraId = siembraId,
    fecha = fecha,
    estadoVital = EstadoVital.valueOf(estadoVital),
    alturaCm = alturaCm,
    diametroMm = diametroMm,
    observaciones = observaciones,
    estadoSync = EstadoSync.valueOf(estadoSync),
    actualizadoEn = actualizadoEn
)

fun Monitoreo.aEntidad() = MonitoreoEntity(
    id = id,
    siembraId = siembraId,
    fecha = fecha,
    estadoVital = estadoVital.name,
    alturaCm = alturaCm,
    diametroMm = diametroMm,
    observaciones = observaciones,
    estadoSync = estadoSync.name,
    actualizadoEn = actualizadoEn
)

fun LoteEntity.aDominio() = Lote(
    id = id,
    viveroId = viveroId,
    nombre = nombre,
    areaHectareas = areaHectareas,
    latitudCentroide = latitudCentroide,
    longitudCentroide = longitudCentroide,
    estadoSync = EstadoSync.valueOf(estadoSync),
    actualizadoEn = actualizadoEn
)

fun Lote.aEntidad() = LoteEntity(
    id = id,
    viveroId = viveroId,
    nombre = nombre,
    areaHectareas = areaHectareas,
    latitudCentroide = latitudCentroide,
    longitudCentroide = longitudCentroide,
    estadoSync = estadoSync.name,
    actualizadoEn = actualizadoEn
)

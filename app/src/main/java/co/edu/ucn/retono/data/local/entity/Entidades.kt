package co.edu.ucn.retono.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidades de persistencia.
 *
 * Se mantienen separadas de los modelos de dominio a propósito: un cambio en el
 * esquema de la base no debe propagarse a la lógica de negocio.
 *
 * Todas las tablas sincronizables llevan tres campos de control:
 *  - `id`: UUID generado en el cliente, no autoincremental.
 *  - `estadoSync`: PENDIENTE / SINCRONIZADO / CONFLICTO.
 *  - `actualizadoEn`: marca temporal para resolver conflictos por última escritura.
 */

@Entity(tableName = "viveros")
data class ViveroEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val municipio: String,
    val responsable: String
)

@Entity(
    tableName = "lotes",
    foreignKeys = [
        ForeignKey(
            entity = ViveroEntity::class,
            parentColumns = ["id"],
            childColumns = ["viveroId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("viveroId")]
)
data class LoteEntity(
    @PrimaryKey val id: String,
    val viveroId: String,
    val nombre: String,
    val areaHectareas: Double,
    val latitudCentroide: Double,
    val longitudCentroide: Double,
    val estadoSync: String,
    val actualizadoEn: Long
)

@Entity(tableName = "especies")
data class EspecieEntity(
    @PrimaryKey val id: String,
    val nombreCientifico: String,
    val nombreComun: String,
    val habito: String
)

@Entity(
    tableName = "siembras",
    foreignKeys = [
        ForeignKey(
            entity = LoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["loteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // Índice sobre estadoSync: el SyncWorker consulta pendientes en cada
    // recuperación de red y esa consulta debe ser barata.
    indices = [Index("loteId"), Index("especieId"), Index("estadoSync")]
)
data class SiembraEntity(
    @PrimaryKey val id: String,
    val loteId: String,
    val especieId: String,
    val latitud: Double,
    val longitud: Double,
    val precisionMetros: Float,
    val fechaSiembra: Long,
    val rutaFotoLocal: String?,
    val observaciones: String,
    val estadoSync: String,
    val actualizadoEn: Long
)

@Entity(
    tableName = "monitoreos",
    foreignKeys = [
        ForeignKey(
            entity = SiembraEntity::class,
            parentColumns = ["id"],
            childColumns = ["siembraId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("siembraId"), Index("estadoSync")]
)
data class MonitoreoEntity(
    @PrimaryKey val id: String,
    val siembraId: String,
    val fecha: Long,
    val estadoVital: String,
    val alturaCm: Double?,
    val diametroMm: Double?,
    val observaciones: String,
    val estadoSync: String,
    val actualizadoEn: Long
)

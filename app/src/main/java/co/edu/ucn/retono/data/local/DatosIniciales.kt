package co.edu.ucn.retono.data.local

import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import co.edu.ucn.retono.data.local.entity.EspecieEntity
import co.edu.ucn.retono.data.local.entity.LoteEntity
import co.edu.ucn.retono.data.local.entity.ViveroEntity

/**
 * Semilla de la base de datos.
 *
 * El catálogo de especies se precarga en la primera apertura porque en campo no
 * hay red para descargarlo. Sin esto, la primera jornada de un técnico en zona
 * sin cobertura sería imposible: no tendría de dónde escoger la especie.
 *
 * Las especies corresponden a nativas usadas en restauración en Antioquia.
 */
object DatosIniciales {

    val vivero = ViveroEntity(
        id = "viv-001",
        nombre = "Vivero comunitario El Retoño",
        municipio = "Granada, Antioquia",
        responsable = "Sin asignar"
    )

    /**
     * Lote de ejemplo para que la aplicación sea usable desde el primer
     * arranque. El usuario puede editarlo o crear los suyos.
     */
    val loteDemostracion = LoteEntity(
        id = "lot-001",
        viveroId = vivero.id,
        nombre = "Lote 1 · Ronda quebrada La Honda",
        areaHectareas = 1.5,
        latitudCentroide = 6.1892,
        longitudCentroide = -75.2153,
        estadoSync = "SINCRONIZADO",
        actualizadoEn = 0L
    )

    val especies = listOf(
        EspecieEntity("esp-001", "Cordia alliodora", "Nogal cafetero", "Árbol"),
        EspecieEntity("esp-002", "Quercus humboldtii", "Roble andino", "Árbol"),
        EspecieEntity("esp-003", "Retrophyllum rospigliosii", "Pino romerón", "Árbol"),
        EspecieEntity("esp-004", "Inga edulis", "Guamo", "Árbol"),
        EspecieEntity("esp-005", "Montanoa quadrangularis", "Arboloco", "Árbol"),
        EspecieEntity("esp-006", "Tibouchina lepidota", "Siete cueros", "Árbol"),
        EspecieEntity("esp-007", "Cecropia peltata", "Yarumo", "Árbol"),
        EspecieEntity("esp-008", "Myrcia popayanensis", "Arrayán", "Arbusto"),
        EspecieEntity("esp-009", "Tithonia diversifolia", "Botón de oro", "Arbusto"),
        EspecieEntity("esp-010", "Miconia caudata", "Nigüito", "Arbusto")
    )

    /**
     * Se inserta con SQL directo desde el callback de Room.
     *
     * Ojo con la constante de conflicto: aquí se usa
     * `SQLiteDatabase.CONFLICT_IGNORE`, no `OnConflictStrategy.IGNORE`. Son
     * enumeraciones distintas con valores distintos, y confundirlas hace que
     * la semilla reemplace registros en lugar de ignorarlos.
     * No se puede usar el DAO aquí: el callback se ejecuta durante la creación
     * de la base, antes de que la instancia esté disponible.
     */
    fun sembrar(db: SupportSQLiteDatabase) {
        db.insert(
            "viveros",
            SQLiteDatabase.CONFLICT_IGNORE,
            android.content.ContentValues().apply {
                put("id", vivero.id)
                put("nombre", vivero.nombre)
                put("municipio", vivero.municipio)
                put("responsable", vivero.responsable)
            }
        )

        db.insert(
            "lotes",
            SQLiteDatabase.CONFLICT_IGNORE,
            android.content.ContentValues().apply {
                put("id", loteDemostracion.id)
                put("viveroId", loteDemostracion.viveroId)
                put("nombre", loteDemostracion.nombre)
                put("areaHectareas", loteDemostracion.areaHectareas)
                put("latitudCentroide", loteDemostracion.latitudCentroide)
                put("longitudCentroide", loteDemostracion.longitudCentroide)
                put("estadoSync", loteDemostracion.estadoSync)
                put("actualizadoEn", loteDemostracion.actualizadoEn)
            }
        )

        especies.forEach { especie ->
            db.insert(
                "especies",
                SQLiteDatabase.CONFLICT_IGNORE,
                android.content.ContentValues().apply {
                    put("id", especie.id)
                    put("nombreCientifico", especie.nombreCientifico)
                    put("nombreComun", especie.nombreComun)
                    put("habito", especie.habito)
                }
            )
        }
    }
}

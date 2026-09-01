package co.edu.ucn.retono.domain.model

/**
 * Modelos de la capa de dominio.
 *
 * Estas clases no conocen Android, Room ni Retrofit. Esa independencia es lo que
 * permite probar la lógica de negocio con JUnit puro en la JVM, sin emulador.
 */

/** Estado de un registro frente al servidor. */
enum class EstadoSync {
    /** Creado o modificado localmente; aún no viaja al servidor. */
    PENDIENTE,

    /** Confirmado por el servidor. */
    SINCRONIZADO,

    /**
     * El servidor tiene una versión más reciente del mismo registro.
     * Se expone al usuario para resolución manual: en datos que sustentan
     * verificación ante financiadores, descartar información en silencio
     * no es aceptable.
     */
    CONFLICTO
}

/** Estado vital de un individuo sembrado en el momento del monitoreo. */
enum class EstadoVital {
    VIVO,
    MUERTO,
    NO_ENCONTRADO;

    /**
     * Solo los individuos hallados cuentan como observaciones válidas.
     * Un individuo no encontrado no es evidencia de muerte: puede haber
     * error de localización, y contarlo como muerto sesgaría el indicador.
     */
    val esObservacionValida: Boolean get() = this != NO_ENCONTRADO
}

data class Vivero(
    val id: String,
    val nombre: String,
    val municipio: String,
    val responsable: String
)

data class Lote(
    val id: String,
    val viveroId: String,
    val nombre: String,
    val areaHectareas: Double,
    val latitudCentroide: Double,
    val longitudCentroide: Double,
    val estadoSync: EstadoSync,
    val actualizadoEn: Long
)

data class Especie(
    val id: String,
    val nombreCientifico: String,
    val nombreComun: String,
    val habito: String
)

/** Individuo sembrado. Es la unidad mínima de trazabilidad del sistema. */
data class Siembra(
    val id: String,
    val loteId: String,
    val especieId: String,
    val latitud: Double,
    val longitud: Double,
    val precisionMetros: Float,
    val fechaSiembra: Long,
    val rutaFotoLocal: String?,
    val observaciones: String,
    val estadoSync: EstadoSync,
    val actualizadoEn: Long
)

/** Visita de seguimiento a un individuo previamente sembrado. */
data class Monitoreo(
    val id: String,
    val siembraId: String,
    val fecha: Long,
    val estadoVital: EstadoVital,
    val alturaCm: Double?,
    val diametroMm: Double?,
    val observaciones: String,
    val estadoSync: EstadoSync,
    val actualizadoEn: Long
)

/** Resultado del cálculo de supervivencia de un lote. */
data class ResultadoSupervivencia(
    val loteId: String,
    val totalSembrado: Int,
    val observados: Int,
    val vivos: Int,
    val muertos: Int,
    val noEncontrados: Int,
    val tasaSupervivencia: Double,
    val calculadoEn: Long
) {
    /**
     * Un porcentaje alto de individuos no encontrados invalida la lectura del
     * indicador, aunque la tasa resultante parezca buena.
     */
    val cobertura: Double
        get() = if (totalSembrado == 0) 0.0 else observados.toDouble() / totalSembrado

    val esConfiable: Boolean get() = cobertura >= UMBRAL_COBERTURA_CONFIABLE

    companion object {
        const val UMBRAL_COBERTURA_CONFIABLE = 0.8
    }
}

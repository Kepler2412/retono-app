# Room genera implementaciones por reflexión sobre las entidades.
-keep class co.edu.ucn.retono.data.local.entity.** { *; }

# Los DTO se serializan por nombre de campo: ofuscarlos rompe el JSON.
-keep class co.edu.ucn.retono.data.remote.dto.** { *; }

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# SQLCipher carga bibliotecas nativas por nombre.
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }

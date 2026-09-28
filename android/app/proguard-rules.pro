-keepattributes *Annotation*, InnerClasses, Signature, Exception

-keep class com.picobeam.transfer.** { *; }

-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

-keep,allowshrinking,allowoptimization,allowobfuscation interface kotlinx.serialization.KSerializer
-keepclassmembers class kotlinx.serialization.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.picobeam.**$$serializer { *; }
-keepclassmembers class com.picobeam.transfer.** {
    *** Companion;
}
-keepclasseswithmembers class com.picobeam.transfer.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-dontwarn org.slf4j.**
-dontwarn java.lang.management.**
# kotlinx.serialization: keep the generated serializers for our @Serializable models.
-keepclassmembers class com.mohithash.caloriebank.domain.** {
    *** Companion;
    *** serializer(...);
}
-keepclasseswithmembers class com.mohithash.caloriebank.domain.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.mohithash.caloriebank.domain.**$$serializer { *; }
-dontwarn org.slf4j.**

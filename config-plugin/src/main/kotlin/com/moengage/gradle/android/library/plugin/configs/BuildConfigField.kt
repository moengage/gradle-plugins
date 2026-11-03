package com.moengage.gradle.android.library.plugin.configs

/** Different types of BuildConfig fields. */
public enum class BuildConfigType(internal val value: String) {
    STRING("String"),
    INT("int"),
    BOOLEAN("boolean")
}

/**
 * Represents a single BuildConfig field entry.
 *
 * @author Abhishek Kumar
 */
public data class BuildConfigField(
    /** Java/Kotlin type of the BuildConfig field (e.g. String, boolean). */
    val type: BuildConfigType,
    /** Name of the BuildConfig field. */
    val name: String,
    /** Value assigned to the BuildConfig field, as a string literal. */
    val value: String
)

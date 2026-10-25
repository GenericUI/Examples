package com.nexustools.genericui.examples.wizard

import com.nexustools.genericui.component.system.PreferenceElement
import com.nexustools.genericui.component.system.PreferenceObjectCodec
import com.nexustools.genericui.component.system.PreferenceSerializable
import com.nexustools.genericui.component.system.PreferenceStore

/**
 * Persisted registration profile model for the Validated Registration Wizard example
 * stored via [PreferenceStore].
 */
data class RegistrationProfile(
    val username: String = "",
    val email: String = "",
    val role: String = "Developer",
    val experienceYears: Int = 3,
    val acceptedTerms: Boolean = false
) : PreferenceSerializable {

    override fun toPreferenceMap(): Map<String, Any?> = linkedMapOf(
        "username" to username,
        "email" to email,
        "role" to role,
        "experienceYears" to experienceYears,
        "acceptedTerms" to acceptedTerms
    )

    companion object {
        const val DEFAULT_STORE_NAME: String = "RegistrationWizardExample"
        const val PROFILE_KEY: String = "registration_profile"
        val AVAILABLE_ROLES: List<String> = listOf(
            "Developer",
            "Designer",
            "Product Manager",
            "QA Engineer",
            "DevOps Architect"
        )

        init {
            registerFactory()
        }

        fun registerFactory() {
            PreferenceObjectCodec.registerObjectFactory(RegistrationProfile::class) { fields ->
                fromElementMap(fields)
            }
        }

        fun fromElementMap(fields: Map<String, PreferenceElement>): RegistrationProfile {
            val user = fields["username"]?.asString("") ?: ""
            val mail = fields["email"]?.asString("") ?: ""
            val roleVal = fields["role"]?.asString("Developer") ?: "Developer"
            val expYears = (fields["experienceYears"]?.asInt(3) ?: 3).coerceIn(0, 50)
            val terms = fields["acceptedTerms"]?.asBoolean(false) ?: false
            return RegistrationProfile(
                username = user,
                email = mail,
                role = roleVal,
                experienceYears = expYears,
                acceptedTerms = terms
            )
        }

        fun loadFrom(store: PreferenceStore): RegistrationProfile? {
            registerFactory()
            if (!store.contains(PROFILE_KEY)) return null
            return store.get(PROFILE_KEY, RegistrationProfile::class, null)
        }

        fun saveTo(store: PreferenceStore, profile: RegistrationProfile) {
            registerFactory()
            store.put(PROFILE_KEY, profile)
            store.flush()
        }
    }
}

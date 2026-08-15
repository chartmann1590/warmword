package com.charles.warmwords.app.ui.screens.findhelp

data class ProviderSearchResult(
    val name: String,
    val specialty: String,
    val websiteUrl: String,
    val phone: String? = null,
    val description: String = ""
)

object ProviderDatabase {
    val mentalHealthResources = listOf(
        ProviderSearchResult(
            name = "Psychology Today",
            specialty = "Find Therapists & Counselors",
            websiteUrl = "https://www.psychologytoday.com/us/therapists",
            description = "Search for licensed therapists by location and specialty"
        ),
        ProviderSearchResult(
            name = "Zocdoc",
            specialty = "Online Doctor Appointments",
            websiteUrl = "https://www.zocdoc.com/mental-health/therapists",
            description = "Find doctors and book appointments online"
        ),
        ProviderSearchResult(
            name = "GoodTherapy",
            specialty = "Therapist Directory",
            websiteUrl = "https://www.goodtherapy.org",
            description = "Find licensed therapists and counselors"
        ),
        ProviderSearchResult(
            name = "Open Path Collective",
            specialty = "Affordable Therapy",
            websiteUrl = "https://openpathcollective.org",
            description = "Low-cost therapy for those without insurance"
        ),
        ProviderSearchResult(
            name = "NAMI",
            specialty = "National Alliance on Mental Illness",
            websiteUrl = "https://www.nami.org/Home",
            description = "Advocacy, education, and support for mental health"
        )
    )
}

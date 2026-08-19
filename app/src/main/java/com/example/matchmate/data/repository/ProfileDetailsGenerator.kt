package com.example.matchmate.data.repository

import kotlin.random.Random

/**
 * Create the random data here to fill the Card Details of user
 */
data class ProfileDetails(
    val profession: String,
    val maritalStatus: String,
    val religion: String,
    val height: String,
    val isVerified: Boolean
)

private val PROFESSIONS = listOf(
    "Architect", "Financial Director", "Software Engineer", "Physician",
    "Teacher", "Entrepreneur", "Graphic Designer", "Lawyer",
    "Marketing Manager", "Chef", "Civil Engineer", "Pharmacist"
)

private val MARITAL_STATUSES = listOf("Never married", "Divorced", "Widowed")

private val RELIGIONS = listOf(
    "Catholic", "Hindu", "Muslim", "Protestant", "Jewish", "Buddhist", "Sikh", "Non-religious"
)

fun generateProfileDetails(seedKey: String): ProfileDetails {
    val random = Random(seedKey.hashCode().toLong())
    val heightInInches = random.nextInt(60, 76)
    val feet = heightInInches / 12
    val inches = heightInInches % 12
    return ProfileDetails(
        profession = PROFESSIONS.random(random),
        maritalStatus = MARITAL_STATUSES.random(random),
        religion = RELIGIONS.random(random),
        height = "$feet'$inches\"",
        isVerified = random.nextInt(100) < 70
    )
}

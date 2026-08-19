package com.example.matchmate.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MatchStatus {
    PENDING, ACCEPTED, DECLINED
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val gender: String,
    val location: String,
    val pictureUrl: String,
    val profession: String,
    val maritalStatus: String,
    val religion: String,
    val height: String,
    val isVerified: Boolean,
    val matchStatus: MatchStatus = MatchStatus.PENDING
) {
    val isPending: Boolean get() = matchStatus == MatchStatus.PENDING
    val isAccepted: Boolean get() = matchStatus == MatchStatus.ACCEPTED
    val isDeclined: Boolean get() = matchStatus == MatchStatus.DECLINED
}

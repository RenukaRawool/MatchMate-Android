package com.example.matchmate.ui.main

import com.example.matchmate.data.local.MatchStatus

enum class MatchFilter {
    ALL, PENDING, ACCEPTED, DECLINED;

    fun toMatchStatus(): MatchStatus? = when (this) {
        ALL -> null
        PENDING -> MatchStatus.PENDING
        ACCEPTED -> MatchStatus.ACCEPTED
        DECLINED -> MatchStatus.DECLINED
    }
}

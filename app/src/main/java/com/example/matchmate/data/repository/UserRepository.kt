package com.example.matchmate.data.repository

import androidx.lifecycle.LiveData
import com.example.matchmate.data.api.ApiService
import com.example.matchmate.data.api.RetrofitClient
import com.example.matchmate.data.local.MatchStatus
import com.example.matchmate.data.local.UserDao
import com.example.matchmate.data.local.UserEntity
import com.example.matchmate.data.model.ApiUser
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class UserRepository(
    private val userDao: UserDao,
    private val apiService: ApiService
) {
    val users: LiveData<List<UserEntity>> = userDao.getAllUsers()

    fun getUsersByStatus(status: MatchStatus): LiveData<List<UserEntity>> = userDao.getUsersByStatus(status)


    private val isLoadingNextPage = AtomicBoolean(false)

    suspend fun fetchUsersIfNeeded(): Result<Unit> {
        if (userDao.getUserCount() > 0) return Result.success(Unit)
        return fetchPage(FIRST_PAGE)
    }

    suspend fun retryFetch(): Result<Unit> = fetchPage(FIRST_PAGE)

    suspend fun loadNextPage(): Result<Unit> {
        if (!isLoadingNextPage.compareAndSet(false, true)) {
            return Result.success(Unit)
        }
        return try {
            val nextPage = (userDao.getUserCount() / RESULTS_PER_PAGE) + 1
            fetchPage(nextPage)
        } finally {
            isLoadingNextPage.set(false)
        }
    }

    private suspend fun fetchPage(page: Int): Result<Unit> {
        return try {
            val response = apiService.getRandomUsers(page = page, results = RESULTS_PER_PAGE, seed = SEED)
            if (response.isSuccessful) {
                val apiUsers = response.body()?.results.orEmpty()
                userDao.insertUsers(apiUsers.map(::toEntity))
                Result.success(Unit)
            } else {
                Result.failure(IOException("Server error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun toEntity(user: ApiUser): UserEntity {
        val details = generateProfileDetails(user.email)
        return UserEntity(
            email = user.email,
            firstName = user.name.first,
            lastName = user.name.last,
            age = user.dob.age,
            gender = user.gender.replaceFirstChar { it.uppercase() },
            location = "${user.location.city}, ${user.location.country}",
            pictureUrl = user.picture.large,
            profession = details.profession,
            maritalStatus = details.maritalStatus,
            religion = details.religion,
            height = details.height,
            isVerified = details.isVerified
        )
    }

    suspend fun updateMatchStatus(email: String, status: MatchStatus) {
        userDao.updateUserStatus(email, status)
    }

    companion object {
        private const val FIRST_PAGE = 1
        private const val RESULTS_PER_PAGE = 10
        private const val SEED = "matchmate"

        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getInstance(userDao: UserDao, apiService: ApiService = RetrofitClient.apiService): UserRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserRepository(userDao, apiService).also { INSTANCE = it }
            }
    }
}

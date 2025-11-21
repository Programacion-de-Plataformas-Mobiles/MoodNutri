package com.example.moodnutri.domain.usecases

/**
 * Base interface for all use cases.
 * @param P Parameters type
 * @param R Return type
 */
interface UseCase<in P, out R> {
    suspend operator fun invoke(params: P): R
}

/**
 * Use case without parameters
 */
interface UseCaseNoParams<out R> {
    suspend operator fun invoke(): R
}
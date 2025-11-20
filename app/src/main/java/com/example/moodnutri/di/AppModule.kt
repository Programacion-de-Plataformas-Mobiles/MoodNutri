package com.example.moodnutri.di

import android.content.Context
import androidx.room.Room
import com.example.moodnutri.data.RecipeDao
import com.example.moodnutri.data.RecipeRepository
import com.example.moodnutri.data.local.AppDatabase
import com.example.moodnutri.data.local.FavoriteRecipeDao
import com.example.moodnutri.data.network.OpenAiApiService
import com.example.moodnutri.data.network.TheMealDbApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "moodnutri_db"
        )
        .fallbackToDestructiveMigration() // Handle version increment
        .build()
    }

    @Provides
    @Singleton
    fun provideFavoriteRecipeDao(database: AppDatabase): FavoriteRecipeDao {
        return database.favoriteRecipeDao()
    }

    @Provides
    @Singleton
    fun provideRecipeDao(database: AppDatabase): RecipeDao {
        return database.recipeDao()
    }

    @Provides
    @Singleton
    fun provideRecipeRepository(
        firebaseAuth: FirebaseAuth,
        firebaseDatabase: FirebaseDatabase,
        theMealDbApi: TheMealDbApiService
    ): RecipeRepository = RecipeRepository(firebaseAuth, firebaseDatabase, theMealDbApi)

    @Provides
    @Singleton
    fun provideTheMealDbApi(): TheMealDbApiService {
        return Retrofit.Builder()
            .baseUrl("https://www.themealdb.com/api/json/v1/1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TheMealDbApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenAiApi(): OpenAiApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAiApiService::class.java)
    }
}
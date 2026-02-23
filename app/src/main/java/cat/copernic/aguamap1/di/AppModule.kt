package cat.copernic.aguamap1.di

import cat.copernic.aguamap1.data.repository.AndroidSoundRepository
import cat.copernic.aguamap1.data.repository.FirebaseAuthRepository
import cat.copernic.aguamap1.data.repository.FirebaseFountainRepository
import cat.copernic.aguamap1.data.repository.FirebaseGameRepository
import cat.copernic.aguamap1.data.repository.FirebaseRankingRepository
// Nuevos imports para categorías
import cat.copernic.aguamap1.data.repository.FirebaseCategoryRepository
import cat.copernic.aguamap1.domain.repository.CategoryRepository

import cat.copernic.aguamap1.domain.repository.AuthRepository
import cat.copernic.aguamap1.domain.repository.FountainRepository
import cat.copernic.aguamap1.domain.repository.GameRepository
import cat.copernic.aguamap1.domain.repository.SoundRepository
import cat.copernic.aguamap1.domain.repository.RankingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import android.content.Context
import cat.copernic.aguamap1.data.error.ErrorResourceProviderImpl
import cat.copernic.aguamap1.domain.error.ErrorResourceProvider
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository = FirebaseAuthRepository(auth, firestore)

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun provideFountainRepository(db: FirebaseFirestore, authRepository: AuthRepository): FountainRepository {
        return FirebaseFountainRepository(db, authRepository)
    }

    // --- NUEVO PROVEEDOR ---
    @Provides
    @Singleton
    fun provideCategoryRepository(db: FirebaseFirestore): CategoryRepository {
        return FirebaseCategoryRepository(db)
    }

    @Provides
    @Singleton
    fun provideRankingRepository(
        authRepository: AuthRepository,
        db: FirebaseFirestore,
    ): RankingRepository {
        return FirebaseRankingRepository(authRepository, db)
    }

    @Provides
    @Singleton
    fun provideGameRepository(db: FirebaseFirestore): GameRepository {
        return FirebaseGameRepository(db)
    }

    @Provides
    @Singleton
    fun provideSoundRepository(
        @ApplicationContext context: Context
    ): SoundRepository {
        return AndroidSoundRepository(context)
    }

    @Provides
    @Singleton
    fun provideErrorResourceProvider(
        @ApplicationContext context: Context
    ): ErrorResourceProvider {
        return ErrorResourceProviderImpl(context)
    }
}
package cat.copernic.aguamap1.di

// Nuevos imports para categorías

import android.content.Context
import cat.copernic.aguamap1.data.cloudinary.CloudinaryService
import cat.copernic.aguamap1.data.error.ErrorResourceProviderImpl
import cat.copernic.aguamap1.data.repository.sound.AndroidSoundRepository
import cat.copernic.aguamap1.data.repository.auth.FirebaseAuthRepository
import cat.copernic.aguamap1.data.repository.category.FirebaseCategoryRepository
import cat.copernic.aguamap1.data.repository.fountain.FirebaseFountainRepository
import cat.copernic.aguamap1.data.repository.game.FirebaseGameRepository
import cat.copernic.aguamap1.data.repository.ranking.FirebaseRankingRepository
import cat.copernic.aguamap1.data.repository.fountain.FirebaseReportRepository
import cat.copernic.aguamap1.data.repository.status.RealtimeStatusRepositoryImpl
import cat.copernic.aguamap1.domain.error.ErrorResourceProvider
import cat.copernic.aguamap1.domain.repository.auth.AuthRepository
import cat.copernic.aguamap1.domain.repository.category.CategoryRepository
import cat.copernic.aguamap1.domain.repository.fountain.FountainRepository
import cat.copernic.aguamap1.domain.repository.game.GameRepository
import cat.copernic.aguamap1.domain.repository.ranking.RankingRepository
import cat.copernic.aguamap1.domain.repository.status.RealtimeStatusRepository
import cat.copernic.aguamap1.domain.repository.fountain.ReportRepository
import cat.copernic.aguamap1.domain.repository.sound.SoundRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
    fun provideReportRepository(db: FirebaseFirestore): ReportRepository {
        return FirebaseReportRepository(db)
    }

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
    fun provideRealtimeStatusRepository(db: FirebaseDatabase): RealtimeStatusRepository {
        return RealtimeStatusRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance("https://aguamap-9ad45-default-rtdb.europe-west1.firebasedatabase.app")
    }

    @Provides
    @Singleton
    fun provideErrorResourceProvider(
        @ApplicationContext context: Context
    ): ErrorResourceProvider {
        return ErrorResourceProviderImpl(context)
    }

    @Provides
    @Singleton
    fun provideCloudinaryService(
        @ApplicationContext context: Context // Hilt te lo pasa aquí
    ): CloudinaryService {
        return CloudinaryService(context) // Y ahora se lo pasas al constructor
    }

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }
}
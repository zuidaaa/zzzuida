package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Proof-of-Thought (PoT) persistence layer.
 * Implements deterministic offline caching, domain search, and invariant verification queries.
 */
@Dao
interface ProofOfThoughtDao {

    @Query("SELECT * FROM proof_of_thought_cache ORDER BY localCachedTimestamp DESC")
    fun getAllProofOfThoughts(): Flow<List<ProofOfThoughtEntity>>

    @Query("SELECT * FROM proof_of_thought_cache WHERE domain = :domain ORDER BY localCachedTimestamp DESC")
    fun getProofsByDomain(domain: String): Flow<List<ProofOfThoughtEntity>>

    @Query("""
        SELECT * FROM proof_of_thought_cache
        WHERE title LIKE '%' || :query || '%'
           OR premiseOrHypothesis LIKE '%' || :query || '%'
           OR formalProofBody LIKE '%' || :query || '%'
           OR qedConclusion LIKE '%' || :query || '%'
           OR domain LIKE '%' || :query || '%'
        ORDER BY localCachedTimestamp DESC
    """)
    fun searchProofs(query: String): Flow<List<ProofOfThoughtEntity>>

    @Query("SELECT * FROM proof_of_thought_cache WHERE id = :id LIMIT 1")
    suspend fun getProofById(id: String): ProofOfThoughtEntity?

    @Query("SELECT * FROM proof_of_thought_cache WHERE normalizedQuery = :normalized LIMIT 1")
    suspend fun findMatchingProof(normalized: String): ProofOfThoughtEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProof(proof: ProofOfThoughtEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProofs(proofs: List<ProofOfThoughtEntity>)

    @Update
    suspend fun updateProof(proof: ProofOfThoughtEntity)

    @Query("DELETE FROM proof_of_thought_cache WHERE id = :id")
    suspend fun deleteProof(id: String)

    @Query("DELETE FROM proof_of_thought_cache")
    suspend fun clearAllProofs()

    @Query("SELECT COUNT(*) FROM proof_of_thought_cache")
    fun getProofCount(): Flow<Int>

    @Query("SELECT SUM(thinkingTokens) FROM proof_of_thought_cache")
    fun getTotalProofTokens(): Flow<Long?>
}

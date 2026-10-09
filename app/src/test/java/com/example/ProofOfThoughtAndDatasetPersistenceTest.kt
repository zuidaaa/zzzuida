package com.example

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProofOfThoughtAndDatasetPersistenceTest {

    private lateinit var db: AppDatabase
    private lateinit var potDao: ProofOfThoughtDao
    private lateinit var reasoningDao: ReasoningCacheDao
    private lateinit var repository: ChatRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        potDao = db.proofOfThoughtDao()
        reasoningDao = db.reasoningCacheDao()
        val chatDao = db.chatDao()
        val benchmarkDao = db.knowledgeAndBenchmarkDao()
        val folderDao = db.folderFoundationDao()

        repository = ChatRepository(chatDao, reasoningDao, benchmarkDao, folderDao, potDao)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun testProofOfThoughtRoomPersistenceCRUD() = runTest {
        val testProof = ProofOfThoughtEntity(
            id = "pot_test_riemann",
            title = "Riemann Zeta Analytic Continuation",
            premiseOrHypothesis = "The Riemann Xi function is entire and symmetric around Re(s) = 1/2.",
            normalizedQuery = "riemann zeta xi function analytic continuation symmetry",
            domain = "Mathematics",
            proofTechnique = "Jacobi Theta Transformation",
            formalProofBody = "Using Jacobi theta functional equation theta(1/t) = sqrt(t)*theta(t)...",
            qedConclusion = "Q.E.D. xi(s) = xi(1-s) globally.",
            verificationStatus = "VERIFIED_FORMAL",
            confidenceScore = 1.0,
            thinkingTokens = 4200,
            thinkingDurationMs = 1200L,
            modelSource = "Offline Deep Thinking Core",
            isOfflineAvailable = true
        )

        // 1. Insert proof
        potDao.insertProof(testProof)

        // 2. Query proof by ID
        val retrieved = potDao.getProofById("pot_test_riemann")
        assertNotNull(retrieved)
        assertEquals("Riemann Zeta Analytic Continuation", retrieved?.title)
        assertEquals("Mathematics", retrieved?.domain)
        assertTrue(retrieved?.isOfflineAvailable == true)

        // 3. Search query
        val searchResults = potDao.searchProofs("Jacobi Theta").first()
        assertEquals(1, searchResults.size)
        assertEquals("pot_test_riemann", searchResults[0].id)

        // 4. Delete proof
        potDao.deleteProof("pot_test_riemann")
        val afterDelete = potDao.getProofById("pot_test_riemann")
        assertNull(afterDelete)
    }

    @Test
    fun testTrainingDatasetPersistenceCRUD() = runTest {
        val testDataset = LlmDatasetEntity(
            id = "dataset_test_sft",
            name = "Calculus Derivatives SFT",
            description = "Elementary differentiation rules and epsilon-delta proofs.",
            fileFormat = "JSONL",
            entryCount = 500,
            fileSize = 128000L,
            rawContent = """{"problem": "d/dx (x^3)", "proof": "Power rule 3x^2", "answer": "3x^2"}""",
            purpose = "Supervised Fine-Tuning (SFT)",
            modelTarget = "deepseek-r1-7b"
        )

        // 1. Insert dataset
        reasoningDao.insertDataset(testDataset)

        // 2. Query dataset
        val allDatasets = reasoningDao.getAllDatasets().first()
        assertEquals(1, allDatasets.size)
        assertEquals("Calculus Derivatives SFT", allDatasets[0].name)
        assertEquals("JSONL", allDatasets[0].fileFormat)
        assertEquals(500, allDatasets[0].entryCount)

        // 3. Delete dataset
        reasoningDao.deleteDataset("dataset_test_sft")
        val afterDelete = reasoningDao.getAllDatasets().first()
        assertEquals(0, afterDelete.size)
    }
}

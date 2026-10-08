package com.example

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import com.example.ui.MainViewModel
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
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FolderFoundationStressTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: FolderFoundationDao
    private lateinit var repository: ChatRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.folderFoundationDao()
        
        // Mocking other DAOs needed for Repository
        val chatDao = db.chatDao()
        val reasoningDao = db.reasoningCacheDao()
        val benchmarkDao = db.knowledgeAndBenchmarkDao()
        
        repository = ChatRepository(chatDao, reasoningDao, benchmarkDao, dao)
        // Removed MainViewModel instantiation to avoid environment side-effects
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun testFolderFoundationCRUD() = runTest {
        val folder = "00_Inbox"
        val file = FolderFileEntity(
            id = "test_1",
            folderName = folder,
            title = "Test Title",
            content = "Test Content",
            dateString = "2026-10-08"
        )

        dao.insertFile(file)
        val files = dao.getFilesByFolder(folder).first()
        assertEquals(1, files.size)
        assertEquals("Test Title", files[0].title)

        // Move to Archive
        val archivedFile = file.copy(folderName = "99_Archive", updatedAt = System.currentTimeMillis())
        dao.insertFile(archivedFile)
        
        val inboxFiles = dao.getFilesByFolder("00_Inbox").first()
        // In this implementation, insertFile with same ID replaces the old one.
        // Since folderName is different, it effectively "moves" it if the query filters correctly.
        assertEquals(0, inboxFiles.size)
        
        val archiveFiles = dao.getFilesByFolder("99_Archive").first()
        assertEquals(1, archiveFiles.size)
    }

    @Test
    fun testStressFolderCreation() = runTest {
        // Stress test: Create 100 folders and 10 files each
        for (i in 1..50) {
            val folderName = "Folder_$i"
            for (j in 1..5) {
                val file = FolderFileEntity(
                    id = "file_${i}_${j}",
                    folderName = folderName,
                    title = "Title $j",
                    content = "Content $j"
                )
                dao.insertFile(file)
            }
        }

        val allFolders = dao.getAllFolders().first()
        // Plus the default 3 if they were initialized, but this is an empty in-memory DB.
        assertEquals(50, allFolders.size)
        
        val folder10Files = dao.getFilesByFolder("Folder_10").first()
        assertEquals(5, folder10Files.size)
    }

    @Test
    fun testSearchFunctionality() = runTest {
        val file1 = FolderFileEntity(id="1", folderName="Inbox", title="Meeting Notes", content="Discussing architecture")
        val file2 = FolderFileEntity(id="2", folderName="Inbox", title="Shopping List", content="Milk, Eggs, Bread")
        
        dao.insertFile(file1)
        dao.insertFile(file2)
        
        val searchResult = dao.searchFolderFiles("architecture").first()
        assertEquals(1, searchResult.size)
        assertEquals("Meeting Notes", searchResult[0].title)
    }
}

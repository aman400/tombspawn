package com.tombspawn.base.extenstions

import com.tombspawn.base.extensions.moveToDirectory
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileReader
import java.io.FileWriter


open class FileMoveTest {
    @get:Rule
    open var temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun moveFileTest() {
        val tempFolder = temporaryFolder.newFolder("temp_dir")
        val otherFolder = temporaryFolder.newFolder("other_dir").also {
            File(it, "file.txt").also { file ->
                file.createNewFile()
                FileWriter(file).use {
                    it.write("This is test file")
                }
            }
        }

        assertTrue(otherFolder.toPath().resolve("file.txt").toFile().exists())

        otherFolder.moveToDirectory(tempFolder)

        assertFalse(otherFolder?.toPath()?.toFile()?.exists() ?: true)

        assertFalse(otherFolder.toPath().resolve("file.txt").toFile().exists())

        val output = tempFolder.toPath()
            .resolve("other_dir")
            .resolve("file.txt")
            .toFile()

        FileReader(output).use {
            assertEquals("Content is not equal", "This is test file", FileReader(output).readText())
        }
    }

    @Test
    fun moveFolderTest() {
        val tempFolder = temporaryFolder.newFolder("temp_dir")
        val otherFolder = temporaryFolder.newFolder("other_dir").also {
            File(it, "childDir").also { folder ->
                folder.mkdir()
                File(folder, "file3.txt").also { file ->
                    file.createNewFile()
                    FileWriter(file).use {
                        it.write("This is test file3")
                    }
                }
            }
        }

        assertTrue(otherFolder.toPath().resolve("childDir").resolve("file3.txt").toFile().exists())

        otherFolder.moveToDirectory(tempFolder)

        assertFalse(otherFolder?.toPath()?.toFile()?.exists() ?: true)

        assertFalse(otherFolder.toPath().resolve("childDir").toFile().exists())

        val output = tempFolder.toPath()
            .resolve("other_dir")
            .resolve("childDir")
            .resolve("file3.txt")
            .toFile()

        FileReader(output).use {
            assertEquals("Content is not equal", "This is test file3", FileReader(output).readText())
        }
    }

    @Test
    fun moveNestedFolderTest() {
        val tempFolder = temporaryFolder.newFolder("temp_dir")
        val otherFolder = temporaryFolder.newFolder("other_dir").also {
            File(it, "childDir").also { folder ->
                folder.mkdir()
                File(folder, "nestedChild").also { nestedFolder ->
                    nestedFolder.mkdir()
                    File(nestedFolder, "file2.txt").also { file ->
                        file.createNewFile()
                        FileWriter(file).use {
                            it.write("This is test file2")
                        }
                    }
                }
            }
        }

        assertTrue(
            otherFolder.toPath()
                .resolve("childDir")
                .resolve("nestedChild")
                .resolve("file2.txt")
                .toFile().exists()
        )

        otherFolder.moveToDirectory(tempFolder)

        assertFalse(otherFolder?.toPath()?.toFile()?.exists() ?: true)

        assertFalse(otherFolder.toPath().resolve("childDir").toFile().exists())

        val output = tempFolder.toPath()
            .resolve("other_dir")
            .resolve("childDir")
            .resolve("nestedChild")
            .resolve("file2.txt")
            .toFile()

        FileReader(output).use {
            assertEquals("Content is not equal", "This is test file2", FileReader(output).readText())
        }
    }

    @Test
    fun multipleFilesTest() {
        val tempFolder = temporaryFolder.newFolder("temp_dir")
        val otherFolder = temporaryFolder.newFolder("other_dir").also {
            File(it, "file5.txt").also { file ->
                file.createNewFile()
                FileWriter(file).use {
                    it.write("This is test file5")
                }
            }
            File(it, "file6.txt").also { file ->
                file.createNewFile()
                FileWriter(file).use {
                    it.write("This is test file6")
                }
            }
            File(it, "file7.txt").also { file ->
                file.createNewFile()
                FileWriter(file).use {
                    it.write("This is test file7")
                }
            }
        }

        assertTrue(otherFolder.toPath().resolve("file6.txt").toFile().exists())

        otherFolder.moveToDirectory(tempFolder)

        assertFalse(otherFolder?.toPath()?.toFile()?.exists() ?: true)

        assertFalse(otherFolder.toPath().resolve("file6.txt").toFile().exists())

        assertEquals(3, tempFolder?.toPath()?.toFile()?.listFiles()?.firstOrNull()?.listFiles()?.size)

        val output = tempFolder.toPath()
            .resolve("other_dir")
            .resolve("file7.txt")
            .toFile()

        FileReader(output).use {
            assertEquals("Content is not equal", "This is test file7", FileReader(output).readText())
        }
    }


    @Test
    fun folderAlreadyExistsTest() {
        val tempFolder = temporaryFolder.newFolder("temp_dir", "other_dir")
        tempFolder.mkdirs()
        val otherFolder = temporaryFolder.newFolder("other_dir").also {
            File(it, "file5.txt").also { file ->
                file.createNewFile()
                FileWriter(file).use {
                    it.write("This is test file5")
                }
            }
            File(it, "file6.txt").also { file ->
                file.createNewFile()
                FileWriter(file).use {
                    it.write("This is test file6")
                }
            }
            File(it, "file7.txt").also { file ->
                file.createNewFile()
                FileWriter(file).use {
                    it.write("This is test file7")
                }
            }
        }

        assertTrue(otherFolder.toPath().resolve("file6.txt").toFile().exists())

        otherFolder.moveToDirectory(tempFolder)

        assertFalse(otherFolder?.toPath()?.toFile()?.exists() ?: true)

        assertFalse(otherFolder.toPath().resolve("file6.txt").toFile().exists())

        assertEquals(3, tempFolder?.toPath()?.toFile()?.listFiles()?.firstOrNull()?.listFiles()?.size)

        val output = tempFolder.toPath()
            .resolve("other_dir")
            .resolve("file7.txt")
            .toFile()

        FileReader(output).use {
            assertEquals("Content is not equal", "This is test file7", FileReader(output).readText())
        }
    }

    @Test
    fun emptyFolderTest() {
        val tempFolder = temporaryFolder.newFolder("temp_dir")
        tempFolder.mkdirs()
        val otherFolder = temporaryFolder.newFolder("other_dir").also {
            it.mkdirs()
        }

        assertTrue(otherFolder.toPath().toFile().exists())

        otherFolder.moveToDirectory(tempFolder)

        assertFalse(otherFolder?.toPath()?.toFile()?.exists() ?: true)

        assertFalse(otherFolder.toPath().toFile().exists())

        assertEquals(0, tempFolder?.toPath()?.toFile()?.listFiles()?.firstOrNull()?.listFiles()?.size)

        val output = tempFolder.toPath()
            .resolve("other_dir")
            .toFile()

        assertEquals(true, output.exists())
    }

}
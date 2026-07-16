package io.github.phper666.sforce.api.sdk.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {

    @Test
    void fileExtNameReturnsExtension() {
        assertEquals("jpg", FileUtils.fileExtName("/t/bbb.jpg"));
    }

    @Test
    void fileExtNameReturnsEmptyWhenNoDot() {
        assertEquals("", FileUtils.fileExtName("c:/t/bbb"));
    }

    @Test
    void fileExtNameReturnsLastSegment() {
        assertEquals("gz", FileUtils.fileExtName("archive.tar.gz"));
    }

    @Test
    void fileExtNameReturnsNullForNull() {
        assertNull(FileUtils.fileExtName(null));
    }

    @Test
    void getFileNameReturnsName() {
        assertEquals("bbb.jpg", FileUtils.getFileName("/t/bbb.jpg"));
    }

    @Test
    void getFileNameHandlesWindowsPath() {
        assertEquals("file.txt", FileUtils.getFileName("c:\\dir\\file.txt"));
    }

    @Test
    void getFileNameReturnsLastSegment() {
        assertEquals("bbb", FileUtils.getFileName("c:/t/bbb"));
    }

    @Test
    void getFileNameReturnsNullForNull() {
        assertNull(FileUtils.getFileName(null));
    }

    @Test
    void getFileNameReturnsEmptyForEmpty() {
        assertEquals("", FileUtils.getFileName(""));
    }

    @Test
    void getFileNameRemovesTrailingSlash() {
        assertEquals("bbb", FileUtils.getFileName("/t/bbb/"));
    }

    @Test
    void fileMainNameReturnsNameWithoutExtension() {
        assertEquals("bbb", FileUtils.fileMainName("/t/bbb.jpg"));
    }

    @Test
    void fileMainNameReturnsFullNameIfNoExtension() {
        assertEquals("bbb", FileUtils.fileMainName("c:/t/bbb"));
    }

    @Test
    void fileMainNameHandlesNull() {
        assertNull(FileUtils.fileMainName(null));
    }

    @Test
    void fileMainNameReturnsEmptyForEmpty() {
        assertEquals("", FileUtils.fileMainName(""));
    }

    @Test
    void isFileSeparatorRecognizesForwardSlash() {
        assertTrue(FileUtils.isFileSeparator('/'));
    }

    @Test
    void isFileSeparatorRecognizesBackslash() {
        assertTrue(FileUtils.isFileSeparator('\\'));
    }

    @Test
    void isFileSeparatorReturnsFalseForOther() {
        assertFalse(FileUtils.isFileSeparator('a'));
    }
}

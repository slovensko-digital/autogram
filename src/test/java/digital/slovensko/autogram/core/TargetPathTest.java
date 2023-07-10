package digital.slovensko.autogram.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.google.common.jimfs.Jimfs;

public class TargetPathTest {
    // @Rule
    // public MockitoRule rule =
    // MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    /**
     * Used in GUI mode with single file
     * or used in CLI mode without target eg. `--cli -s /test/virtual/source.pdf`
     * 
     * @throws IOException
     */
    @Test
    public void testSingleFileNoTarget() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix());
        var sourceFile = fs.getPath("/test/virtual/source.pdf");
        Files.createDirectories(sourceFile.getParent());
        Files.createFile(sourceFile);

        var targetPath = new TargetPath(null, sourceFile, false, false, Files.isDirectory(sourceFile), fs);
        var target = targetPath.getSaveFilePath(sourceFile);

        assertEqualPath("/test/virtual/source_signed.pdf", target);
    }

    /**
     * Used in GUI mode with single file
     * or used in CLI mode without target eg. `--cli -s source.pdf` on path
     * `/test/virtual/`
     * 
     * @throws IOException
     */
    @Test
    public void testSingleFileNoTargetNoParent() throws IOException {
        var config = com.google.common.jimfs.Configuration.unix().toBuilder().setWorkingDirectory("/test/virtual/")
                .build();
        FileSystem fs = Jimfs.newFileSystem(config);
        Files.createDirectories(fs.getPath("/test/virtual/"));
        var sourceFile = fs.getPath("source.pdf");
        Files.createFile(sourceFile);

        var targetPath = new TargetPath(null, sourceFile, false, false, Files.isDirectory(sourceFile), fs);
        var target = targetPath.getSaveFilePath(sourceFile);

        assertEqualPath("/test/virtual/source_signed.pdf", target);
    }

    /**
     * Used in GUI mode with single file and no target when generated target file
     * exits
     * or used in CLI mode without target eg. `--cli -s /test/virtual/source.pdf`
     * 
     * @throws IOException
     */
    @Test
    public void testSingleFileNoTargetFileExists() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix());
        var sourceFile = fs.getPath("/test/virtual/source.pdf");
        Files.createDirectories(sourceFile.getParent());
        Files.createFile(sourceFile);
        Files.createFile(fs.getPath("/test/virtual/source_signed.pdf"));

        var targetPath = new TargetPath(null, sourceFile, false, false, Files.isDirectory(sourceFile), fs);
        var target = targetPath.getSaveFilePath(sourceFile);

        assertEqualPath("/test/virtual/source_signed (1).pdf", target);
    }

    /**
     * Used in GUI mode with single file
     * or used in CLI mode without target eg. `--cli -s /test/virtual/source.pdf`
     * 
     * @throws IOException
     */
    @Test
    public void testSingleFileNoTargetAsice() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix());
        var sourceFile = fs.getPath("/test/virtual/source.xml");
        Files.createDirectories(sourceFile.getParent());
        Files.createFile(sourceFile);

        var targetPath = new TargetPath(null, sourceFile, false, false, Files.isDirectory(sourceFile), fs);
        var target = targetPath.getSaveFilePath(sourceFile);

        assertEqualPath("/test/virtual/source_signed.asice", target);
    }

    /**
     * Used in CLI mode eg. `--cli -s /test/virtual/source.pdf -t
     * /test/virtual/target.pdf`
     * 
     * @throws IOException
     */
    @Test
    public void testSingleFileWithTarget() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix());
        var sourceFile = fs.getPath("/test/virtual/source.pdf");
        Files.createDirectories(sourceFile.getParent());
        Files.createFile(sourceFile);

        var targetPath = new TargetPath("/test/virtual/other/target.pdf", sourceFile, false, false, fs);
        var target = targetPath.getSaveFilePath(sourceFile);

        assertEqualPath("/test/virtual/other/target.pdf", target);
    }

    /**
     * `--cli -s /test/virtual/ -t /test/virtual/target/`
     */
    @Test
    public void testDirectoryWithTarget() throws IOException {

        FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix());
        var sourceDirectory = fs.getPath("/test/virtual/");
        Files.createDirectories(sourceDirectory);

        var source1 = fs.getPath("/test/virtual/source", "source1.pdf");
        var source2 = fs.getPath("/test/virtual/source", "source2.pdf");

        var targetPath = new TargetPath("/test/virtual/target/", sourceDirectory, false, false, fs);
        var target1 = targetPath.getSaveFilePath(source1);
        var target2 = targetPath.getSaveFilePath(source2);

        assertEqualPath("/test/virtual/target/source1_signed.pdf", target1);
        assertEqualPath("/test/virtual/target/source2_signed.pdf", target2);
    }

    /**
     * `--cli -s /test/virtual/`
     */
    @Test
    public void testDirectoryNoTarget() throws IOException {

        FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix());
        var sourceDirectory = fs.getPath("/test/virtual/source/");
        Files.createDirectories(sourceDirectory);

        var source1 = fs.getPath("/test/virtual/source", "source1.pdf");
        var source2 = fs.getPath("/test/virtual/source", "source2.pdf");

        var targetPath = new TargetPath(null, sourceDirectory, false, false, fs);
        var target1 = targetPath.getSaveFilePath(source1);
        var target2 = targetPath.getSaveFilePath(source2);

        assertEqualPath("/test/virtual/source_signed/source1_signed.pdf", target1);
        assertEqualPath("/test/virtual/source_signed/source2_signed.pdf", target2);
    }

    /**
     * `--cli -s /test/virtual/`
     */
    @Test
    public void testDirectoryNoTargetNoParent() throws IOException {

        var config = com.google.common.jimfs.Configuration.unix().toBuilder().setWorkingDirectory("/test/virtual/")
                .build();
        FileSystem fs = Jimfs.newFileSystem(config);
        var sourceDirectory = fs.getPath("source/");
        Files.createDirectories(sourceDirectory);

        var source1 = fs.getPath("source", "source1.pdf");
        var source2 = fs.getPath("source", "source2.pdf");

        var targetPath = new TargetPath(null, sourceDirectory, false, false, fs);
        var target1 = targetPath.getSaveFilePath(source1);
        var target2 = targetPath.getSaveFilePath(source2);

        assertEqualPath("/test/virtual/source_signed/source1_signed.pdf", target1);
        assertEqualPath("/test/virtual/source_signed/source2_signed.pdf", target2);
    }

    @Test
    public void testMkdirIfDirNotExists() throws IllegalArgumentException,
            IllegalAccessException, IOException {

        FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix());
        var sourceDirectory = fs.getPath("/test/virtual/source");
        Files.createDirectories(sourceDirectory);

        var targetPath = new TargetPath(null, sourceDirectory, false, false, fs);
        targetPath.mkdirIfDir();

        assertTrue(Files.exists(fs.getPath("/test/virtual/source_signed")));
    }

    @Test()
    public void testTargetDirExits() throws IllegalArgumentException,
            IllegalAccessException, IOException {

        FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix());
        var sourceDirectory = fs.getPath("/test/virtual/");
        Files.createDirectories(sourceDirectory);
        Files.createDirectories(fs.getPath("/test/output"));

        assertThrows(digital.slovensko.autogram.core.errors.TargetAlreadyExistsException.class, () -> {
            var targetPath = new TargetPath("/test/output", sourceDirectory, false, false, fs);
        });
    }

    @Test()
    public void testTargetFileExits() throws IllegalArgumentException,
            IllegalAccessException, IOException {

        FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix());
        var sourceFile = fs.getPath("/test/virtual/source.pdf");
        Files.createDirectories(sourceFile.getParent());
        Files.createFile(sourceFile);
        Files.createFile(fs.getPath("/test/output.pdf"));

        assertThrows(digital.slovensko.autogram.core.errors.TargetAlreadyExistsException.class, () -> {
            var targetPath = new TargetPath("/test/output.pdf", sourceFile, false, false, fs);
        });
    }

    @Test()
    public void testMkdirIfDirExistsForce() throws IllegalArgumentException,
            IllegalAccessException, IOException {

        FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix());
        var sourceDirectory = fs.getPath("/test/virtual/");
        Files.createDirectories(sourceDirectory);
        Files.createDirectories(fs.getPath("/test/output"));

        var targetPath = new TargetPath("/test/output", sourceDirectory, true, false, fs);
        targetPath.mkdirIfDir();
    }

    @Test()
    public void testMkdirIfDirExistsParents() throws IllegalArgumentException,
            IllegalAccessException, IOException {

        FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix());
        var sourceDirectory = fs.getPath("/test/virtual/");
        Files.createDirectories(sourceDirectory);
        var targetPath = new TargetPath("/test/output/parent/directories", sourceDirectory, false, true, fs);
        targetPath.mkdirIfDir();

    }

    @Test()
    public void testMultipleFilesIntoDir() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix());
        Files.createDirectories(fs.getPath("/test/virtual/"));

        var targetPath = new TargetPath("/test/target/", null, false, false, true, fs);
        targetPath.mkdirIfDir();

        IntStream.range(0, 5).forEach(i -> {
            try {
                var sourceFile = fs.getPath("/test/virtual/source" + i + ".pdf");

                Files.createFile(sourceFile);
                var savePath = targetPath.getSaveFilePath(sourceFile);
                assertEqualPath("/test/target/source" + i + "_signed.pdf", savePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test()
    public void testMultipleFilesIntoDirExists() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix());
        Files.createDirectories(fs.getPath("/test/virtual/"));
        Files.createDirectories(fs.getPath("/test/target/"));

        var targetPath = new TargetPath("/test/target/", null, false, false, true, fs);
        targetPath.mkdirIfDir();

        IntStream.range(0, 5).forEach(i -> {
            try {
                var sourceFile = fs.getPath("/test/virtual/source" + i + ".pdf");

                Files.createFile(sourceFile);
                var savePath = targetPath.getSaveFilePath(sourceFile);
                assertEqualPath("/test/target (1)/source" + i + "_signed.pdf", savePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /* Assert helpers */

    private void assertEqualPath(FileSystem fs, String expected, String actual) {
        assertEqualPath(fs.getPath(expected), fs.getPath(actual));
    }

    private void assertEqualPath(String expected, Path actual) {
        assertEqualPath(actual.getFileSystem().getPath(expected), actual);
    }

    private void assertEqualPath(Path expected, Path actual) {
        assertEquals(expected.normalize().toString(), actual.normalize().toString());
    }

}

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

import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;

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

        var targetPath = new TargetPath(null, sourceFile, false, false, Files.isDirectory(sourceFile), fs, true);
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

        var targetPath = new TargetPath(null, sourceFile, false, false, Files.isDirectory(sourceFile), fs, true);
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

        var targetPath = new TargetPath(null, sourceFile, false, false, Files.isDirectory(sourceFile), fs, true);
        var target = targetPath.getSaveFilePath(sourceFile);

        assertEqualPath("/test/virtual/source_signed (1).pdf", target);
    }

    @Test
    public void testSingleFileNoTargetUsesActualSignedFileType() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix());
        var sourceFile = fs.getPath("/test/virtual/source.pdf");
        Files.createDirectories(sourceFile.getParent());
        Files.createFile(sourceFile);

        var targetPath = new TargetPath(null, sourceFile, false, false, Files.isDirectory(sourceFile), fs, false);
        var target = targetPath.getSaveFilePath(sourceFile, signedDocument(MimeTypeEnum.PDF));

        assertEqualPath("/test/virtual/source_signed.pdf", target);
    }

    @Test
    public void testSingleFileNoTargetUsesAsiceForSignedContainer() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix());
        var sourceFile = fs.getPath("/test/virtual/source.pdf");
        Files.createDirectories(sourceFile.getParent());
        Files.createFile(sourceFile);

        var targetPath = new TargetPath(null, sourceFile, false, false, Files.isDirectory(sourceFile), fs, true);
        var target = targetPath.getSaveFilePath(sourceFile, signedDocument(MimeTypeEnum.ASICE));

        assertEqualPath("/test/virtual/source_signed.asice", target);
    }

    /**
     * Re-signing an enveloping CAdES (CMS) document keeps it a CMS, so it must not be saved
     * with the ASiC-E extension (issue #772).
     */
    @Test
    public void testSingleFileNoTargetKeepsSourceExtensionForSignedCms() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix());
        var sourceFile = fs.getPath("/test/virtual/source.pdf");
        Files.createDirectories(sourceFile.getParent());
        Files.createFile(sourceFile);

        var targetPath = new TargetPath(null, sourceFile, false, false, Files.isDirectory(sourceFile), fs, true);
        var target = targetPath.getSaveFilePath(sourceFile, signedDocument(MimeTypeEnum.PKCS7));

        assertEqualPath("/test/virtual/source_signed.pdf", target);
    }

    @Test
    public void testSingleFileNoTargetUsesP7mForSignedCmsWithoutSourceExtension() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix());
        var sourceFile = fs.getPath("/test/virtual/source");
        Files.createDirectories(sourceFile.getParent());
        Files.createFile(sourceFile);

        var targetPath = new TargetPath(null, sourceFile, false, false, Files.isDirectory(sourceFile), fs, true);
        var target = targetPath.getSaveFilePath(sourceFile, signedDocument(MimeTypeEnum.PKCS7));

        assertEqualPath("/test/virtual/source_signed.p7m", target);
    }

    @Test
    public void testSingleFileNoTargetKeepsSourceExtensionForSignedXml() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix());
        var sourceFile = fs.getPath("/test/virtual/source.xml");
        Files.createDirectories(sourceFile.getParent());
        Files.createFile(sourceFile);

        var targetPath = new TargetPath(null, sourceFile, false, false, Files.isDirectory(sourceFile), fs, true);
        var target = targetPath.getSaveFilePath(sourceFile, signedDocument(MimeTypeEnum.XML));

        assertEqualPath("/test/virtual/source_signed.xml", target);
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

        var targetPath = new TargetPath(null, sourceFile, false, false, Files.isDirectory(sourceFile), fs, true);
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

        var targetPath = new TargetPath("/test/virtual/other/target.pdf", sourceFile, false, false, fs, true);
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

        var targetPath = new TargetPath("/test/virtual/target/", sourceDirectory, false, false, fs, true);
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

        var targetPath = new TargetPath(null, sourceDirectory, false, false, fs, true);
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

        var targetPath = new TargetPath(null, sourceDirectory, false, false, fs, true);
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

        var targetPath = new TargetPath(null, sourceDirectory, false, false, fs, true);
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
            new TargetPath("/test/output", sourceDirectory, false, false, fs, true);
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
            new TargetPath("/test/output.pdf", sourceFile, false, false, fs, true);
        });
    }

    @Test()
    public void testMkdirIfDirExistsForce() throws IllegalArgumentException,
            IllegalAccessException, IOException {

        FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix());
        var sourceDirectory = fs.getPath("/test/virtual/");
        Files.createDirectories(sourceDirectory);
        Files.createDirectories(fs.getPath("/test/output"));

        var targetPath = new TargetPath("/test/output", sourceDirectory, true, false, fs, true);
        targetPath.mkdirIfDir();
    }

    @Test()
    public void testMkdirIfDirExistsParents() throws IllegalArgumentException,
            IllegalAccessException, IOException {

        FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix());
        var sourceDirectory = fs.getPath("/test/virtual/");
        Files.createDirectories(sourceDirectory);
        var targetPath = new TargetPath("/test/output/parent/directories", sourceDirectory, false, true, fs, true);
        targetPath.mkdirIfDir();

    }

    @Test()
    public void testMultipleFilesIntoDir() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix());
        Files.createDirectories(fs.getPath("/test/virtual/"));

        var targetPath = new TargetPath("/test/target/", null, false, false, true, fs, true);
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

        var targetPath = new TargetPath("/test/target/", null, false, false, true, fs, true);
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

    private static DSSDocument signedDocument(MimeType mimeType) {
        return new InMemoryDocument(new byte[0], "signed", mimeType);
    }

    /* Assert helpers */

    private void assertEqualPath(String expected, Path actual) {
        assertEqualPath(actual.getFileSystem().getPath(expected), actual);
    }

    private void assertEqualPath(Path expected, Path actual) {
        assertEquals(expected.normalize().toString(), actual.normalize().toString());
    }

}

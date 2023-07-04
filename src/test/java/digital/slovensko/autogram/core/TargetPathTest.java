package digital.slovensko.autogram.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class TargetPathTest {
    /**
     * Used in GUI mode with single file
     * or used in CLI mode without target eg. `--cli -s /test/virtual/source.pdf`
     */
    @Test
    public void testSingleFileNoTarget() {
        var sourceFileParent = mock(File.class);
        when(sourceFileParent.getPath()).thenReturn("/test/virtual");
        when(sourceFileParent.getName()).thenReturn("virtual");
        when(sourceFileParent.getParent()).thenReturn("/test");
        when(sourceFileParent.isDirectory()).thenReturn(true);
        when(sourceFileParent.isFile()).thenReturn(false);

        var sourceFile = mock(File.class);
        when(sourceFile.getPath()).thenReturn("/test/virtual/source.pdf");
        when(sourceFile.getName()).thenReturn("source.pdf");
        when(sourceFile.getParent()).thenReturn("/test/virtual");
        when(sourceFile.isDirectory()).thenReturn(false);
        when(sourceFile.isFile()).thenReturn(true);
        when(sourceFile.getParentFile()).thenReturn(sourceFileParent);

        var targetPath = new TargetPath(null, sourceFile, false);
        var target = targetPath.getSaveFilePath(sourceFile);

        assertEqualPath("/test/virtual/source_signed.pdf", target.getPath());
    }


    /**
     * Used in GUI mode with single file
     * or used in CLI mode without target eg. `--cli -s /test/virtual/source.pdf`
     */
    @Test
    public void testSingleFileNoTargetAsice() {
        var sourceFileParent = mock(File.class);
        when(sourceFileParent.getPath()).thenReturn("/test/virtual");
        when(sourceFileParent.getName()).thenReturn("virtual");
        when(sourceFileParent.getParent()).thenReturn("/test");
        when(sourceFileParent.isDirectory()).thenReturn(true);
        when(sourceFileParent.isFile()).thenReturn(false);

        var sourceFile = mock(File.class);
        when(sourceFile.getPath()).thenReturn("/test/virtual/source.xml");
        when(sourceFile.getName()).thenReturn("source.xml");
        when(sourceFile.getParent()).thenReturn("/test/virtual");
        when(sourceFile.isDirectory()).thenReturn(false);
        when(sourceFile.isFile()).thenReturn(true);
        when(sourceFile.getParentFile()).thenReturn(sourceFileParent);

        var targetPath = new TargetPath(null, sourceFile, false);
        var target = targetPath.getSaveFilePath(sourceFile);

        assertEqualPath("/test/virtual/source_signed.asice", target.getPath());
    }


    /**
     * Used in CLI mode eg. `--cli -s /test/virtual/source.pdf -t
     * /test/virtual/target.pdf`
     */
    @Test
    public void testSingleFileWithTarget() {
        var sourceFileParent = mock(File.class);
        when(sourceFileParent.getPath()).thenReturn("/test/virtual");
        when(sourceFileParent.getName()).thenReturn("virtual");
        when(sourceFileParent.getParent()).thenReturn("/test");
        when(sourceFileParent.isDirectory()).thenReturn(true);
        when(sourceFileParent.isFile()).thenReturn(false);

        var sourceFile = mock(File.class);
        when(sourceFile.getPath()).thenReturn("/test/virtual/source.pdf");
        when(sourceFile.getName()).thenReturn("source.pdf");
        when(sourceFile.getParent()).thenReturn("/test/virtual");
        when(sourceFile.isDirectory()).thenReturn(false);
        when(sourceFile.isFile()).thenReturn(true);
        when(sourceFile.getParentFile()).thenReturn(sourceFileParent);

        var targetPath = new TargetPath("/test/virtual/other/target.pdf", sourceFile, false);
        var target = targetPath.getSaveFilePath(sourceFile);

        assertEqualPath("/test/virtual/other/target.pdf", target.getPath());
    }

    @Test
    public void testDirectoryWithTarget() {
        var sourceDirectory = mock(File.class);
        when(sourceDirectory.getPath()).thenReturn("/test/virtual/");
        when(sourceDirectory.getName()).thenReturn("virtual");
        when(sourceDirectory.getParent()).thenReturn("/test");
        when(sourceDirectory.isDirectory()).thenReturn(true);
        when(sourceDirectory.isFile()).thenReturn(false);

        var source1 = mock(File.class);
        when(source1.getPath()).thenReturn("/test/virtual/source1.pdf");
        when(source1.getName()).thenReturn("source1.pdf");
        when(source1.getParent()).thenReturn("/test/virtual");
        when(source1.isDirectory()).thenReturn(false);
        when(source1.isFile()).thenReturn(true);

        var source2 = mock(File.class);
        when(source2.getPath()).thenReturn("/test/virtual/source2.pdf");
        when(source2.getName()).thenReturn("source2.pdf");
        when(source2.getParent()).thenReturn("/test/virtual");
        when(source2.isDirectory()).thenReturn(false);
        when(source2.isFile()).thenReturn(true);

        var targetPath = new TargetPath("/test/virtual/target/", sourceDirectory, false);
        var target1 = targetPath.getSaveFilePath(source1);
        var target2 = targetPath.getSaveFilePath(source2);

        assertEqualPath("/test/virtual/target/source1_signed.pdf", target1.getPath());

        assertEqualPath("/test/virtual/target/source2_signed.pdf", target2.getPath());
    }

    @Test
    public void testDirectoryNoTarget() {
        var sourceDirectoryParent = mock(File.class);
        when(sourceDirectoryParent.getPath()).thenReturn("/test/virtual/");
        when(sourceDirectoryParent.getName()).thenReturn("virtual");
        when(sourceDirectoryParent.getParent()).thenReturn("/test");
        when(sourceDirectoryParent.isDirectory()).thenReturn(true);
        when(sourceDirectoryParent.isFile()).thenReturn(false);

        var sourceDirectory = mock(File.class);
        when(sourceDirectory.getPath()).thenReturn("/test/virtual/source");
        when(sourceDirectory.getName()).thenReturn("source");
        when(sourceDirectory.getParent()).thenReturn("/test/virtual");
        when(sourceDirectory.isDirectory()).thenReturn(true);
        when(sourceDirectory.isFile()).thenReturn(false);
        when(sourceDirectory.getParentFile()).thenReturn(sourceDirectoryParent);

        var source1 = mock(File.class);
        when(source1.getPath()).thenReturn("/test/virtual/source/source1.pdf");
        when(source1.getName()).thenReturn("source1.pdf");
        when(source1.getParent()).thenReturn("/test/virtual/source");
        when(source1.isDirectory()).thenReturn(false);
        when(source1.isFile()).thenReturn(true);

        var source2 = mock(File.class);
        when(source2.getPath()).thenReturn("/test/virtual/source/source2.pdf");
        when(source2.getName()).thenReturn("source2.pdf");
        when(source2.getParent()).thenReturn("/test/virtual/source");
        when(source2.isDirectory()).thenReturn(false);
        when(source2.isFile()).thenReturn(true);

        var targetPath = new TargetPath(null, sourceDirectory, false);
        var target1 = targetPath.getSaveFilePath(source1);
        var target2 = targetPath.getSaveFilePath(source2);

        assertEqualPath("/test/virtual/source_signed/source1_signed.pdf", target1.getPath());
        assertEqualPath("/test/virtual/source_signed/source2_signed.pdf", target2.getPath());
    }

    private void assertEqualPath(String expected, String actual) {
        assertEquals(Paths.get(expected).normalize().toString(), Paths.get(actual).normalize().toString());
    }
}

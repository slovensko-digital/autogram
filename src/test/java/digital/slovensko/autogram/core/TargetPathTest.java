package digital.slovensko.autogram.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.io.File;

import org.junit.jupiter.api.Test;

public class TargetPathTest {
    /**
     * Used in GUI mode with single file
     * or used in CLI mode without target eg. `--cli -s /test/virtual/source.pdf`
     */
    @Test
    public void testSingleFileNoTarget() {
        var sourceFile = mock(File.class);
        when(sourceFile.getPath()).thenReturn("/test/virtual/source.pdf");
        when(sourceFile.getName()).thenReturn("source.pdf");
        when(sourceFile.getParent()).thenReturn("/test/virtual");
        when(sourceFile.isDirectory()).thenReturn(false);
        when(sourceFile.isFile()).thenReturn(true);

        var targetDirectory = mock(File.class);
        when(targetDirectory.getPath()).thenReturn("/test/virtual");
        when(targetDirectory.getName()).thenReturn("virtual");
        when(targetDirectory.isDirectory()).thenReturn(true);
        when(targetDirectory.isFile()).thenReturn(false);
        when(targetDirectory.exists()).thenReturn(true);

        var targetPath = TargetPath.buildForTest(sourceFile, false, true, targetDirectory, null);
        var target = targetPath.getSaveFilePath(sourceFile);

        assertEquals("/test/virtual/source_signed.pdf", target.getPath());
    }

    /**
     * Used in CLI mode eg. `--cli -s /test/virtual/source.pdf -t
     * /test/virtual/target.pdf`
     */
    @Test
    public void testSingleFileWithTarget() {
        var sourceFile = mock(File.class);
        when(sourceFile.getPath()).thenReturn("/test/virtual/source.pdf");
        when(sourceFile.getName()).thenReturn("source.pdf");
        when(sourceFile.getParent()).thenReturn("/test/virtual");
        when(sourceFile.isDirectory()).thenReturn(false);
        when(sourceFile.isFile()).thenReturn(true);

        var targetDirectory = mock(File.class);
        when(targetDirectory.getPath()).thenReturn("/test/virtual/");
        when(targetDirectory.getName()).thenReturn("virtual");
        when(targetDirectory.isDirectory()).thenReturn(false);
        when(targetDirectory.isFile()).thenReturn(true);
        when(targetDirectory.exists()).thenReturn(false);
        when(targetDirectory.mkdir()).thenReturn(false);

        var targetPath = TargetPath.buildForTest(sourceFile, false, false, targetDirectory, "target.pdf");
        var target = targetPath.getSaveFilePath(sourceFile);

        assertEquals("/test/virtual/target.pdf", target.getPath());
    }

    @Test
    public void testDirectoryWithTarget() {
        var sourceFile = mock(File.class);
        when(sourceFile.getPath()).thenReturn("/test/virtual/");
        when(sourceFile.getName()).thenReturn("virtual");
        when(sourceFile.getParent()).thenReturn("/test/virtual");
        when(sourceFile.isDirectory()).thenReturn(false);
        when(sourceFile.isFile()).thenReturn(true);

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

        var targetFile = mock(File.class);
        when(targetFile.getPath()).thenReturn("/test/virtual/target/");
        when(targetFile.getName()).thenReturn("target");
        when(targetFile.isDirectory()).thenReturn(false);
        when(targetFile.isFile()).thenReturn(true);
        when(targetFile.exists()).thenReturn(false);
        when(targetFile.mkdir()).thenReturn(false);

        var targetPath = TargetPath.buildForTest(sourceFile, false, true, targetFile, null);
        var target1 = targetPath.getSaveFilePath(source1);
        var target2 = targetPath.getSaveFilePath(source2);

        assertEquals("/test/virtual/target/source1_signed.pdf", target1.getPath());
        assertEquals("/test/virtual/target/source2_signed.pdf", target2.getPath());
    }

    @Test
    public void testDirectoryNoTarget() {
        var sourceFile = mock(File.class);
        when(sourceFile.getPath()).thenReturn("/test/virtual/source");
        when(sourceFile.getName()).thenReturn("source");
        when(sourceFile.getParent()).thenReturn("/test/virtual");
        when(sourceFile.isDirectory()).thenReturn(true);
        when(sourceFile.isFile()).thenReturn(false);

        var targetDirectory = mock(File.class);
        when(targetDirectory.getPath()).thenReturn("/test/virtual/source/");
        when(targetDirectory.getName()).thenReturn("source");
        when(targetDirectory.isDirectory()).thenReturn(false);
        when(targetDirectory.isFile()).thenReturn(true);
        when(targetDirectory.exists()).thenReturn(false);
        when(targetDirectory.mkdir()).thenReturn(false);

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

        var targetPath = TargetPath.buildForTest(sourceFile, false, true, targetDirectory, null);
        var target1 = targetPath.getSaveFilePath(source1);
        var target2 = targetPath.getSaveFilePath(source2);

        assertEquals("/test/virtual/source/source1_signed.pdf", target1.getPath());
        assertEquals("/test/virtual/source/source2_signed.pdf", target2.getPath());
    }
}

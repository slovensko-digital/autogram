package digital.slovensko.autogram.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;

public class TargetPathTest {

    /**
     * Used in GUI mode with single file
     * or used in CLI mode without target eg. `--cli -s /test/virtual/source.pdf`
     */
    @Test
    public void testSingleFileNoTarget() {
        var sourceFile = mockSourceFile();

        var targetPath = new TargetPath(null, sourceFile, false, false);
        var target = targetPath.getSaveFilePath(sourceFile);

        assertEqualPath("/test/virtual/source_signed.pdf", target.getPath());
    }

    /**
     * Used in GUI mode with single file and no target when generated target file exits
     * or used in CLI mode without target eg. `--cli -s /test/virtual/source.pdf`
     */
    @Test
    public void testSingleFileNoTargetFileExists() {
        var sourceFile = mockSourceFile();

        var targetPath = spy(new TargetPath(null, sourceFile, false, false));
        

        var method = ReflectionUtils.findMethod(TargetPath.class, "_generateUniqueNameGetNewTargetFile").orElseThrow();
        method.setAccessible(true);
        
        var target = targetPath.getSaveFilePath(sourceFile);

        assertEqualPath("/test/virtual/source_signed (1).pdf", target.getPath());
    }

    /**
     * Used in GUI mode with single file
     * or used in CLI mode without target eg. `--cli -s /test/virtual/source.pdf`
     */
    @Test
    public void testSingleFileNoTargetAsice() {
        var sourceFile = mockSourceFile();
        when(sourceFile.getPath()).thenReturn("/test/virtual/source.xml");
        when(sourceFile.getName()).thenReturn("source.xml");

        var targetPath = new TargetPath(null, sourceFile, false, false);
        var target = targetPath.getSaveFilePath(sourceFile);

        assertEqualPath("/test/virtual/source_signed.asice", target.getPath());
    }

    /**
     * Used in CLI mode eg. `--cli -s /test/virtual/source.pdf -t
     * /test/virtual/target.pdf`
     */
    @Test
    public void testSingleFileWithTarget() {
        var sourceFile = mockSourceFile();

        var targetPath = new TargetPath("/test/virtual/other/target.pdf", sourceFile, false, false);
        var target = targetPath.getSaveFilePath(sourceFile);

        assertEqualPath("/test/virtual/other/target.pdf", target.getPath());
    }

    @Test
    public void testDirectoryWithTarget() {

        var sourceDirectory = mockSourceDirectory();

        var source1 = mockSourceFile("/test/virtual/source", "source1.pdf");
        var source2 = mockSourceFile("/test/virtual/source", "source2.pdf");

        var targetPath = new TargetPath("/test/virtual/target/", sourceDirectory, false, false);
        var target1 = targetPath.getSaveFilePath(source1);
        var target2 = targetPath.getSaveFilePath(source2);

        assertEqualPath("/test/virtual/target/source1_signed.pdf", target1.getPath());
        assertEqualPath("/test/virtual/target/source2_signed.pdf", target2.getPath());
    }

    @Test
    public void testDirectoryNoTarget() {
        var sourceDirectory = mockSourceDirectory();

        var source1 = mockSourceFile("/test/virtual/source", "source1.pdf");
        var source2 = mockSourceFile("/test/virtual/source", "source2.pdf");

        var targetPath = new TargetPath(null, sourceDirectory, false, false);
        var target1 = targetPath.getSaveFilePath(source1);
        var target2 = targetPath.getSaveFilePath(source2);

        assertEqualPath("/test/virtual/source_signed/source1_signed.pdf", target1.getPath());
        assertEqualPath("/test/virtual/source_signed/source2_signed.pdf", target2.getPath());
    }

    @Test
    public void testMkdirIfDirNotExists() throws IllegalArgumentException, IllegalAccessException {
        var sourceDirectory = mockSourceDirectory();

        var targetDirectory = mockTargetDirectory();
        when(targetDirectory.exists()).thenReturn(false);

        var targetPath = new TargetPath(null, sourceDirectory, false, false);

        Field field = ReflectionUtils.findFields(TargetPath.class, f -> f.getName().equals("targetDirectory"),
                ReflectionUtils.HierarchyTraversalMode.TOP_DOWN).get(0);
        field.setAccessible(true);
        field.set(targetPath, targetDirectory);

        targetPath.mkdirIfDir();

        verify(targetDirectory, times(1)).mkdir();
        verify(targetDirectory, times(0)).mkdirs();
    }

    @Test
    public void testMkdirIfDirExists() throws IllegalArgumentException, IllegalAccessException {
        var sourceDirectory = mockSourceDirectory();

        var targetDirectory = mockTargetDirectory();
        when(targetDirectory.exists()).thenReturn(true);

        var targetPath = new TargetPath(null, sourceDirectory, false, false);

        Field field = ReflectionUtils.findFields(TargetPath.class, f -> f.getName().equals("targetDirectory"),
                ReflectionUtils.HierarchyTraversalMode.TOP_DOWN).get(0);
        field.setAccessible(true);
        field.set(targetPath, targetDirectory);

        targetPath.mkdirIfDir();

        verify(targetDirectory, times(0)).mkdir();
        verify(targetDirectory, times(0)).mkdirs();
    }

    /* Mock helpers */

    /**
     * @return mock for `/test/virtual/source.pdf`
     */
    private File mockSourceFile() {
        return mockSourceFile("/test/virtual", "source.pdf");
    }

    private File mockSourceFile(String parentPath, String name) {
        var sourceFileParent = mock(File.class);
        when(sourceFileParent.getPath()).thenReturn("/test/virtual");
        when(sourceFileParent.getName()).thenReturn("virtual");
        when(sourceFileParent.getParent()).thenReturn("/test");
        when(sourceFileParent.isDirectory()).thenReturn(true);
        when(sourceFileParent.isFile()).thenReturn(false);

        var sourceFile = mock(File.class);
        when(sourceFile.getPath()).thenReturn(Paths.get(parentPath, name).toString());
        when(sourceFile.getName()).thenReturn(name);
        when(sourceFile.getParent()).thenReturn(parentPath);
        when(sourceFile.isDirectory()).thenReturn(false);
        when(sourceFile.isFile()).thenReturn(true);
        when(sourceFile.getParentFile()).thenReturn(sourceFileParent);

        return sourceFile;
    }

    /**
     * 
     * @return mock for `/test/virtual/source`
     */
    private File mockSourceDirectory() {
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

        return sourceDirectory;
    }

    /**
     * 
     * @return mock for `/test/virtual/source_signed`
     */
    private File mockTargetDirectory() {
        var targetDirectory = mock(File.class);
        when(targetDirectory.getPath()).thenReturn("/test/virtual/source_signed");
        when(targetDirectory.getName()).thenReturn("source_signed");
        when(targetDirectory.getParent()).thenReturn("/test/virtual");
        when(targetDirectory.isDirectory()).thenReturn(true);
        when(targetDirectory.isFile()).thenReturn(false);
        when(targetDirectory.exists()).thenReturn(false);
        when(targetDirectory.mkdir()).thenReturn(true);
        when(targetDirectory.mkdirs()).thenReturn(true);

        return targetDirectory;
    }

    /* Assert helpers */

    private void assertEqualPath(String expected, String actual) {
        assertEquals(Paths.get(expected).normalize().toString(), Paths.get(actual).normalize().toString());
    }

}

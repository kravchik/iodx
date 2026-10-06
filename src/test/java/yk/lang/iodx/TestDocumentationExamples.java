package yk.lang.iodx;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestDocumentationExamples {
    private static final Pattern IODX_BLOCK = Pattern.compile("(?ms)^```iodx[ \\t]*\\r?\\n(.*?)^```[ \\t]*$");

    @Test
    public void testAllIodxBlocksAreValidDocuments() throws IOException {
        assertValidIodxBlocks("README.md", 4);
        assertValidIodxBlocks("serialization.md", 2);
        assertValidIodxBlocks("why-another.md", 2);
    }

    private static void assertValidIodxBlocks(String path, int expectedBlockCount) throws IOException {
        String markdown = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        Matcher matcher = IODX_BLOCK.matcher(markdown);
        int blockNumber = 0;

        while (matcher.find()) {
            blockNumber++;
            try {
                assertFalse(path + ": IODX block " + blockNumber + " should contain data",
                    Iodx.readIodxEntities(matcher.group(1)).isEmpty());
            } catch (RuntimeException e) {
                AssertionError error = new AssertionError(path + ": invalid IODX block " + blockNumber);
                error.initCause(e);
                throw error;
            }
        }

        assertEquals(path + ": unexpected number of executable IODX examples",
            expectedBlockCount, blockNumber);
    }
}

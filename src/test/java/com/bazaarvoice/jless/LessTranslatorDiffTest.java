package com.bazaarvoice.jless;

import com.bazaarvoice.jless.ast.Node;
import difflib.DiffUtils;
import difflib.Patch;
import org.apache.commons.io.IOUtils;
import org.parboiled.support.ParsingResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Test
public class LessTranslatorDiffTest extends LessTranslatorParsingTest {

    @Override
    protected void runTestFor(String fileName) {
        ParsingResult<Node> result = parseLess(fileName);
        diffOutput(fileName, result);
    }

    private void diffOutput(String fileName, ParsingResult<Node> parsingResult) {
        InputStream referenceStream = getClass().getResourceAsStream("/java/" + fileName + ".css");
        List<String> referenceLines = null;

        try {
            //noinspection unchecked
            referenceLines = IOUtils.readLines(referenceStream, "UTF-8");
        } catch (IOException e) {
            TestUtils.getLog().println("Unable to read " + fileName + ".css");
            e.printStackTrace();
        }

        List<String> outputLines = Arrays.asList(printResult(parsingResult).split("\n"));

        Patch diff = DiffUtils.diff(referenceLines, outputLines);

        if (!diff.getDeltas().isEmpty()) {
            TestUtils.getLog().println("Reference output diff:");
            List<String> diffOutput = DiffUtils.generateUnifiedDiff(fileName + ".css", fileName + ".css", referenceLines, diff, 3);
            for (String diffOutputLine : diffOutput) {
                TestUtils.getLog().println(diffOutputLine);
            }
        }

        Assert.assertEquals(diff.getDeltas().size(), 0);
    }
}

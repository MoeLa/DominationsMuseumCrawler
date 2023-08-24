package bhg.sucks.helper;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import bhg.sucks.model.KeepRule;

public class DebugHelper {

    public static String DEBUG_MODE_KEY = "debug_mode";

    /**
     * Log the artefact to keep and the matching rule.
     */
    public void logKeepArtefact(OcrHelper.Data data, KeepRule keepRule) {
        try (FileOutputStream fOut = new FileOutputStream("artefactRuleMatches.txt", true);
             OutputStreamWriter osw = new OutputStreamWriter(fOut)
        ) {
            osw.write(String.format("Scanned: %s\nMatched Rule: %s%s\t\nMandatory: %s\t\nOptional: %s",
                            data.getSkills(),
                            keepRule.getName(),
                            keepRule.getAmountMatches().name(),
                            keepRule.getMandatorySkillsOfCategory(),
                            keepRule.getOptionalSkillsOfCategory()
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}

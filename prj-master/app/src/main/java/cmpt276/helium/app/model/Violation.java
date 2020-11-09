package cmpt276.helium.app.model;

import static cmpt276.helium.app.Utils.unquote;

/*
    Model class for Violation that simply holds whether or not its critical or repeat,
    and its full description
 */
public class Violation {

    private boolean isCritical;
    private String description;
    private boolean isRepeat;

    public Violation(String[] violationData) {
        isCritical = unquote(violationData[1]).equals("Critical");
        description = unquote(violationData[2]);
        isRepeat = unquote(violationData[3]).equals("Repeat");
    }

    public boolean isCritical() {
        return isCritical;
    }

    public String getDescription() {
        return description;
    }
}

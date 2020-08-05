package info.ponciano.lab.knowdip.reasoner.automatic;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PiRegex {

    public static final String word = "\\w";
    public static final String startLine = "^";
    public static final String endLine = "$";
    public static final String endString = "\\z";
    public static final String startString = "\\A";
    public static String notWord = "\\W";
    public static final String whiteCharacter = "\\s";

    /**
     * Returns a literal pattern <code>String</code> for the specified
     * <code>String</code>.
     *
     * <p>
     * This method produces a <code>String</code> that can be used to create a
     * <code>Pattern</code> that would match the string <code>s</code> as if it
     * were a literal pattern.</p> Metacharacters or escape sequences in the
     * input sequence will be given no special meaning.
     *
     * @param exp The string to be literalized
     * @return A literal string replacement
     */
    public static String quote(String exp) {
        return Pattern.quote(exp);
    }

    private final Pattern pattern;
    private final String source;

    /**
     * Contructor
     *
     * @param expression expression could be use.
     * @param source source to apply the regular expression Exemple to apply
     * [AbfTz] expression at the source l'Amour:
     * <ul>
     * <li>PiRegex regex= new PiRegex("([AbfTz])","l'Amour");</li>
     * <li>regex.getGroupe(); <strong>get result = "A"</strong>
     * </ul>
     */
    public PiRegex(String expression, String source) {
        this.source = source;
        pattern = Pattern.compile(expression);
    }

    /**
     * Get all group matched
     *
     * @param duplicate True for allowing to add dupplicate string as results,
     * false to have only unique results values.
     * @return List contained each group matched
     */
    public List<String> getGroup(boolean duplicate) {
        List<String> res = new ArrayList<String>();
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            final String group = matcher.group();
            if (duplicate || !res.contains(group)) {
                res.add(group);
            }
        }
        return res;
    }

    /**
     * Gets the input subsequence captured by the given group for each match operation.
     * 
     * <p>Group zero denotes the entire pattern.</p>
     *
     * @param i number of the specific group. 
     * @return each specific group matched.
     */
    public List<String> getGroup(int i) {
        List<String> res = new ArrayList<String>();
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            res.add(matcher.group(i));
        }
        return res;
    }

    /**
     * Get the first matching
     * @return String matched or  an empty string if it not match
     */
    public String getFirst() {
        ArrayList<String> res = new ArrayList<String>();
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            res.add(matcher.group());
        }
        if (res.size() > 0) {
            return res.get(0);
        } else {
            return "";
        }
    }

}

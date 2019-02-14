package gr.teohaik.sonaranalyzer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static gr.teohaik.sonaranalyzer.Analyzer.client;
import static gr.teohaik.sonaranalyzer.Constants.PORT;
import java.io.IOException;
import java.util.Iterator;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import static gr.teohaik.sonaranalyzer.Constants.SONAR_HOST;
import static gr.teohaik.sonaranalyzer.Constants.SONAR_HOST_WITH_PORT;

/**
 *
 */
public class RuleAnalyzer {

    static Client client = ClientBuilder.newClient();

    static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String... args) throws IOException {

        String[] ruleNames = Constants.ruleKeys;
        System.out.println("RuleKey \t Debt_minutes");
        for (String ruleKey : ruleNames) {
           // System.out.println("searching for "+ruleKey);
            JsonNode root = initClient(ruleKey);

            String debt = getRuleDebt(root, ruleKey);
            System.out.println(ruleKey + "\t" + debt);
        }
    }

    public static JsonNode initClient(String ruleKey) throws IOException {
        String url = SONAR_HOST_WITH_PORT + "/api/rules/search?rule_key=" + ruleKey;
        WebTarget targetUnderTest = client.target(url);
        String jsonResponse = targetUnderTest.request().get(String.class);
        JsonNode root = objectMapper.readTree(jsonResponse);
        return root;
    }

    public static String getRuleName(String ruleKey) throws IOException {
        JsonNode root = initClient(ruleKey);
        String ruleName = "N/A";
        Iterator<JsonNode> rulesNodes = root.path("rules").elements();
        while (rulesNodes.hasNext()) {
            JsonNode next = rulesNodes.next();
            ruleName = next.get("name").asText();

        }
        return ruleName;
    }

    public static String getRuleDebt(JsonNode root, String ruleKey) throws IOException {
        String descr = "";
        String ruleDebt = "0";
        Iterator<JsonNode> rulesNodes = root.path("rules").elements();
        while (rulesNodes.hasNext()) {
            JsonNode next = rulesNodes.next();
            String type = next.get("defaultRemFnType").asText();
            if(type.equals("CONSTANT_ISSUE")){
                ruleDebt = next.get("defaultRemFnBaseEffort").asText();
            }
            else if (type.equals("LINEAR")){
                ruleDebt = next.get("defaultRemFnGapMultiplier").asText();
            }
            else if (type.equals("LINEAR_OFFSET")){
                ruleDebt = next.get("remFnBaseEffort").asText();
            }
            descr = type + "\t";
        }
        ruleDebt = ruleDebt.replaceAll("min", "");
        descr += ruleDebt;
        return descr;
    }

}

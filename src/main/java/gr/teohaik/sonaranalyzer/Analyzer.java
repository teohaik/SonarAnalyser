package gr.teohaik.sonaranalyzer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static gr.teohaik.sonaranalyzer.Constants.PORT;
import static gr.teohaik.sonaranalyzer.Constants.SONAR_HOST;
import gr.teohaik.sonaranalyzer.auth.Authenticator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import static gr.teohaik.sonaranalyzer.Constants.SONAR_HOST_WITH_PORT;

/**
 *
 */
public class Analyzer {

    static Client client = ClientBuilder.newClient();

    static ObjectMapper objectMapper = new ObjectMapper();

  //  static String SEVERITY = "MINOR";
  //  static String SEVERITY = "MAJOR";
  //  static String SEVERITY = "CRITICAL";
    static String SEVERITY = "BLOCKER";

    public static void main(String... args) throws IOException {

        
        
        List<String> projects = getAnalyzedProjectNames(); // Arrays.asList("curl");//

        Map<String, Integer> issueFrequencies = new HashMap<>();

        Map<String, Set<String>> filesPerIssue = new HashMap<>();

        Map<String, Integer> issueDebts = new HashMap<>();

        for (String project : projects) {
            System.out.println("Analyzing project " + project);
            try {
                handleProject(project, issueFrequencies, filesPerIssue, SEVERITY);
            } catch (Exception e) {
                System.err.println("Exception on project " + project + " but Analysis will continue ");
                System.out.println(e.getMessage());
            }
        }
        printRuleFrequencies(issueFrequencies, filesPerIssue);
    }

    private static void handleProject(String project, Map<String, Integer> issueFrequencies, Map<String, Set<String>> filesPerIssue, String severity) throws IOException {

        int currentPage = 1;
        int pageSize = 100;

        WebTarget targetUnderTest = client.target(getIssuesUrl(project, severity, currentPage, pageSize));
        String jsonResponse = targetUnderTest.request().get(String.class);
        JsonNode root = objectMapper.readTree(jsonResponse);

        int total = root.get("total").asInt();
        int page = root.get("p").asInt();

        int limit = total / pageSize;

        while (currentPage < limit) {
            System.out.println("page " + currentPage + " of " + limit);
            Iterator<JsonNode> issuesNodes = root.path("issues").elements();
            while (issuesNodes.hasNext()) {
                JsonNode issue = issuesNodes.next();

                String ruleKey = issue.get("rule").asText();
                String fileName = issue.get("component").asText();

                increaseFrequency(ruleKey, issueFrequencies);
                handleIssueFile(ruleKey, fileName, filesPerIssue);
            }

            currentPage++;
            targetUnderTest = client.target(getIssuesUrl(project, SEVERITY, currentPage, pageSize));
            jsonResponse = targetUnderTest.request().get(String.class);
            root = objectMapper.readTree(jsonResponse);
        }
    }

    private static void increaseFrequency(String key, Map<String, Integer> freqs) {
        if (freqs.containsKey(key)) {
            int oldFreq = freqs.get(key);
            int newFreq = oldFreq + 1;
            freqs.put(key, newFreq);
        } else {
            freqs.put(key, 1);
        }
    }

    private static void handleIssueFile(String key, String fileName, Map<String, Set<String>> filesPerIssue) {
        if (filesPerIssue.containsKey(key)) {
            filesPerIssue.get(key).add(fileName);
        } else {
            Set<String> fileSet = new HashSet();
            fileSet.add(fileName);
            filesPerIssue.put(key, fileSet);
        }
    }

    private static List<String> getAnalyzedProjectNames() throws IOException {
        List<String> projects = new ArrayList<>();

        Client client = ClientBuilder.newClient().register(new Authenticator("admin", "admin"));

        WebTarget targetUnderTest = client.target(getProjectsUrl());

        String jsonResponse = targetUnderTest.request().get(String.class);

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode root = objectMapper.readTree(jsonResponse);

        Iterator<JsonNode> projectsNode = root.path("components").elements();

        projectsNode.forEachRemaining(pe -> {
            String projectKey = pe.get("key").asText();
            projects.add(projectKey);
        });
        return projects;
    }

    private static String getIssuesUrl(String project, String severity, int page, int pageSize) {

        return SONAR_HOST_WITH_PORT+"/api/issues/search"
                + "?severities=" + severity
                + "&s=SEVERITY"
                + "&asc=false"
                + "&facets=rules"
                + "&ps=" + pageSize
                + "&p=" + page
                + "&componentKeys=" + project;

    }

    private static String getProjectsUrl() {
        return SONAR_HOST_WITH_PORT+"/api/projects/search";
    }

    private static void printRuleFrequencies(Map<String, Integer> issueFrequencies, Map<String, Set<String>> filesPerIssue) throws IOException {

        System.out.println("Report for SONAR DB issues with severity " + SEVERITY);
        System.out.println("Rule_Key\tRuleName\tOccurences\tFilesFound");
        issueFrequencies.keySet().forEach(ruleKey -> {
            String ruleName = "";
            try {
                ruleName = RuleAnalyzer.getRuleName(ruleKey);
            } catch (IOException ex) {
                Logger.getLogger(Analyzer.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println(ruleKey + "\t" + ruleName + "\t" + issueFrequencies.get(ruleKey) + "\t" + filesPerIssue.get(ruleKey).size());
        });

    }

}

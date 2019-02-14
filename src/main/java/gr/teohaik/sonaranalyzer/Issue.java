
package gr.teohaik.sonaranalyzer;

import java.util.Objects;

/**
 *
 */
public class Issue {

    String key;
    String rule;
    String severity;
    String project;
    double effortTime;
    double debt;
    String type;
    
    String hash;

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.rule);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Issue other = (Issue) obj;
        if (!Objects.equals(this.rule, other.rule)) {
            return false;
        }
        return true;
    }

 
    
}

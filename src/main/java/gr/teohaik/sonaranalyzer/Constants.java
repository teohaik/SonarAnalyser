
package gr.teohaik.sonaranalyzer;

/**
 *
 */
public class Constants {
    
    public static final String SONAR_HOST = "http://se.uom.gr";
    
    public static final String PORT = "9666";
    
    public static final String SONAR_HOST_WITH_PORT = SONAR_HOST + ":"+ PORT;
    
    
    public static final String[] ruleKeys = {"c:UndocumentedApi",	"c:ClassName",	"c:BooleanEqualityComparison",
        "c:TooLongLine",	"c:FileName",	"c:StringLiteralDuplicated",	"c:TabCharacter",
        "c:MissingNewLineAtEndOfFile",	"c:MagicNumber",	"c:TooManyLinesOfCodeInFunction",
        "c:MissingCurlyBraces",	"common-c:InsufficientLineCoverage",	"c:TooManyParameters",
        "c:NestedStatements",	"c:UselessParentheses",	"c:TooManyLinesOfCodeInFile",	"c:SwitchLastCaseIsDefault",
        "c:FileComplexity",	"common-c:InsufficientCommentDensity",	"c:TooManyStatementsPerLine",	"c:FixmeTagPresence",	
        "c:MethodName",	"c:FunctionComplexity",	"c:CollapsibleIfCandidate",	"c:FunctionName",	"common-c:DuplicatedBlocks",
        "c:FunctionCognitiveComplexity",
        "c:CommentedCode",	"c:HardcodedIp",	"c:UseCorrectInclude",	"c:FileHeader",	"c:ReservedNames"};

}

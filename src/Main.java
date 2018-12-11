import com.elements.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newDefaultInstance();
        try {
            DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
            for (String arg : args) {
                String testsFolderLocation = arg.split("=")[1];
                File testsFolder = new File(testsFolderLocation);

                for (File testFile : testsFolder.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        if (name.endsWith(".xml"))
                            return true;
                        else
                            return false;
                    }
                })) {
                    if (!testFile.canRead()) {
                        System.err.println("Unable to read file : " + testFile.getAbsolutePath());
                    } else {
                        Document testDocument = documentBuilder.parse(testFile);
                        List<Test> testList = new ArrayList<>();
                        Element testsTagElement = testDocument.getDocumentElement();
                        NodeList testsChildNodes = testsTagElement.getChildNodes();
                        int numberOfTestsElementChildren = testsChildNodes.getLength();

                        for (int i = 0; i < numberOfTestsElementChildren; i++) {
                            Node testTagElementNode = testsChildNodes.item(i);

                            if (testTagElementNode instanceof Element) {
                                Test test = new Test();
                                test.id = testTagElementNode.getAttributes().getNamedItem("id").getNodeValue();
                                test.inputFile = new File(testTagElementNode.getAttributes().getNamedItem("inputFile").getNodeValue());
                                test.summary = ((Element) testTagElementNode).getElementsByTagName("summary").item(0).getTextContent();
                                test.description = ((Element) testTagElementNode).getElementsByTagName("description").item(0).getTextContent();

                                Node preconditionNode = ((Element) testTagElementNode).getElementsByTagName("precondition").item(0);
                                Node stepsNode = ((Element) testTagElementNode).getElementsByTagName("steps").item(0);

                                NodeList preconditionChildNodes = preconditionNode.getChildNodes(); // Returns list of child nodes under Precondition tag
                                Precondition precondition = new Precondition();
                                List<Action> preconditionActions = new ArrayList<>();
                                NodeList stepsChildNodes = stepsNode.getChildNodes();// Returns list of child nodes under Steps tag i.e. step tags
                                Steps steps = new Steps();
                                List<Step> stepList = new ArrayList<>();
                                for (int j = 0; j < preconditionChildNodes.getLength(); j++) {
                                    Node actionNode = preconditionChildNodes.item(j);
                                    Action actionRefined = getAction(actionNode);
                                    if ( actionRefined!= null) {
                                        preconditionActions.add(actionRefined);
                                    }
                                }

                                Precondition precondition1 = new Precondition();
                                precondition1.action = preconditionActions;


                                test.precondition = precondition1;

                                for (int j = 0; j < stepsChildNodes.getLength(); j++) {
                                    Node stepNode = stepsChildNodes.item(j);
                                    Step refinedStep = getStep(stepNode);
                                    if(refinedStep != null) {
                                        stepList.add(refinedStep);
                                    }
                                }
                                test.steps = stepList;

                                testList.add(test);
                            }
                        }
                        System.out.println(testList.get(0).inputFile);
                        System.out.println(testList.get(0).summary);
                        System.out.println(testList.get(0).description);
                        System.out.println(testList.get(0).id);
                        System.out.println(testList.get(0).precondition.action.size());
                        System.out.println(testList.get(0).steps.get(0).actions.size());
                        System.out.println(testList.get(0).steps.get(0).expectations.size());



                    }
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static Step getStep(Node stepNode) {
        Step step = new Step();
        List<Action> actionList = new ArrayList<>();
        List<Expectation> expectationList = new ArrayList<>();

        if (stepNode instanceof Element) {

            NodeList actionsUnderStepNodeList = ((Element) stepNode).getElementsByTagName("action");

            for (int i = 0; i < actionsUnderStepNodeList.getLength(); i++) {
                Node actionNode = actionsUnderStepNodeList.item(i);
                Action refinedAction = getAction(actionNode);

                if(refinedAction != null) {
                    actionList.add(refinedAction);
                }
            }
            step.actions = actionList;

            NodeList expecationsUnderStepNodeList = ((Element) stepNode).getElementsByTagName("expectation");

            for (int i = 0; i < expecationsUnderStepNodeList.getLength(); i++) {
                Node expectationNode = expecationsUnderStepNodeList.item(i);
                Expectation refinedExpecation = getExpectation(expectationNode);

                if (refinedExpecation != null) {
                    expectationList.add(refinedExpecation);
                }
            }

            step.expectations = expectationList;
        } else {
            return null;
        }
        return step;
    }

    private static Expectation getExpectation(Node expectationNode) {
        Expectation expectation = new Expectation();
        if (expectationNode instanceof Element) {
            for (int k = 0; k < expectationNode.getAttributes().getLength(); k++) {
                Node attributesNode = expectationNode.getAttributes().item(k);
                switch (attributesNode.getNodeName().toLowerCase()) {

                    case "verify":
                        expectation.verify = attributesNode.getNodeValue().toLowerCase();
                        break;
                    case "value":
                        expectation.value = attributesNode.getNodeValue().toLowerCase();
                        break;
                }

                if (attributesNode.getNodeName().toLowerCase().endsWith("id")) {
                    expectation.elementId = attributesNode.getNodeValue();
                } else if (attributesNode.getNodeName().toLowerCase().endsWith("tagname")) {
                    expectation.elementTagName = attributesNode.getNodeValue();
                } else if (attributesNode.getNodeName().toLowerCase().endsWith("xpath")) {
                    expectation.elementXPath = attributesNode.getNodeValue();
                } else if (attributesNode.getNodeName().toLowerCase().endsWith("classname")) {
                    expectation.elementClassName = attributesNode.getNodeValue();
                } else if (attributesNode.getNodeName().toLowerCase().endsWith("cssselector")) {
                    expectation.elementCssSelector = attributesNode.getNodeValue();
                } else if (attributesNode.getNodeName().toLowerCase().endsWith("name")) {
                    expectation.elementName = attributesNode.getNodeValue();
                }
            }
        } else {
            return null;
        }
        return expectation;
    }

    private static Action getAction(Node actionNode) {
        Action action = new Action();
        if (actionNode instanceof Element) {
            for (int k = 0; k < actionNode.getAttributes().getLength(); k++) {
                Node attributesNode = actionNode.getAttributes().item(k);
                switch (attributesNode.getNodeName().toLowerCase()) {

                    case "action":
                        action.action = attributesNode.getNodeValue().toLowerCase();
                        break;
                    case "value":
                        action.value = attributesNode.getNodeValue().toLowerCase();
                        break;
                }

                if (attributesNode.getNodeName().toLowerCase().endsWith("id")) {
                    action.elementId = attributesNode.getNodeValue();
                } else if (attributesNode.getNodeName().toLowerCase().endsWith("tagname")) {
                    action.elementTagName = attributesNode.getNodeValue();
                } else if (attributesNode.getNodeName().toLowerCase().endsWith("xpath")) {
                    action.elementXPath = attributesNode.getNodeValue();
                } else if (attributesNode.getNodeName().toLowerCase().endsWith("classname")) {
                    action.elementClassName = attributesNode.getNodeValue();
                } else if (attributesNode.getNodeName().toLowerCase().endsWith("cssselector")) {
                    action.elementCssSelector = attributesNode.getNodeValue();
                } else if (attributesNode.getNodeName().toLowerCase().endsWith("name")) {
                    action.elementName = attributesNode.getNodeValue();
                }
            }
        } else {
            return null;
        }
        return action;
    }
}
package com.elements;

public class Expectation {
    public String description;
    public String verify;
    public String value;
    public String elementName;
    public String elementId;
    public String elementCssSelector;
    public String elementXPath;
    public String elementTagName;
    public String elementClassName;

    @Override
    public String toString() {
        return description;
    }
}

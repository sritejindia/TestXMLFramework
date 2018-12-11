package com.elements;

public class Action {

    public String description;
    public String action;
    public String elementName;
    public String elementId;
    public String elementCssSelector;
    public String elementXPath;
    public String elementTagName;
    public String elementClassName;
    public String value;

    @Override
    public String toString() {
        return description;
    }
}

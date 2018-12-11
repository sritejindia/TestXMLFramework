package com.elements;

import java.io.File;
import java.util.List;

public class Test {
    public String id;
    public File inputFile;
    public String summary;
    public String description;
    public Precondition precondition;
    public List<Step> steps;

    @Override
    public String toString() {
        return summary;
    }
}

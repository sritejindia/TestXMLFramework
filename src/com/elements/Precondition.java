package com.elements;

import java.util.List;

public class Precondition {
    String description;
    public List<Action> action;

    @Override
    public String toString() {
        return action.toString();
    }
}

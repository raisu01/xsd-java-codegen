package com.ifall.xsdcodegen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Structure intermédiaire représentant un élément XSD (simple ou complexe),
 * produite par le parsing et consommée par le générateur de code.
 */
public class ElementDef {

    private final String name;
    private String simpleType;
    private boolean complex;
    private boolean list;
    private final List<ElementDef> children = new ArrayList<>();

    public ElementDef(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getSimpleType() {
        return simpleType;
    }

    public void setSimpleType(String simpleType) {
        this.simpleType = simpleType;
    }

    public boolean isComplex() {
        return complex;
    }

    public void setComplex(boolean complex) {
        this.complex = complex;
    }

    public boolean isList() {
        return list;
    }

    public void setList(boolean list) {
        this.list = list;
    }

    public List<ElementDef> getChildren() {
        return children;
    }
}

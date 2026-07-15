package com.ifall.xsdcodegen.parser;

import com.ifall.xsdcodegen.model.ElementDef;

import java.io.File;
import java.util.Map;

/**
 * Démo autonome pour valider le parsing (tâche Membre 2), indépendante
 * du générateur ou du runtime développés par les autres membres.
 */
public class ParserDemo {

    public static void main(String[] args) throws Exception {
        File xsd = new File("src/main/resources/example.xsd");
        Map<String, ElementDef> elements = new XsdParser().parse(xsd);

        for (ElementDef def : elements.values()) {
            System.out.println(describe(def));
        }
    }

    private static String describe(ElementDef def) {
        StringBuilder sb = new StringBuilder(def.getName());
        if (def.isComplex()) {
            sb.append(" (complexe)");
            for (ElementDef child : def.getChildren()) {
                sb.append("\n  -> ").append(child.getName());
                if (child.isList()) {
                    sb.append(" [liste]");
                }
            }
        } else {
            sb.append(" (simple: ").append(def.getSimpleType()).append(")");
        }
        return sb.toString();
    }
}

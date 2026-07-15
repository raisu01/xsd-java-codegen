package com.ifall.xsdcodegen.generator;

import com.ifall.xsdcodegen.model.ElementDef;
import com.ifall.xsdcodegen.parser.XsdParser;

import java.io.File;
import java.util.Map;

/**
 * Démo autonome pour valider la génération de code (tâche Membre 3),
 * enchaîne parsing (Membre 2) -> génération (Membre 3).
 */
public class GeneratorDemo {

    public static void main(String[] args) throws Exception {
        File xsd = new File("src/main/resources/example.xsd");
        Map<String, ElementDef> elements = new XsdParser().parse(xsd);

        File outputDir = new File("generated");
        new JavaCodeGenerator().generate(elements, outputDir);

        System.out.println("Classes generees dans : " + outputDir.getAbsolutePath());
    }
}
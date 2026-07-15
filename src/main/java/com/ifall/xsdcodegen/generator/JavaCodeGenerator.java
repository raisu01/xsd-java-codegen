package com.ifall.xsdcodegen.generator;

import com.ifall.xsdcodegen.model.ElementDef;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Transforme la structure intermédiaire (ElementDef) produite par le parser
 * du Membre 2 en fichiers .java, écrits sur disque dans un dossier
 * "generated/" (avec l'arborescence de packages complète).
 *
 * Règles de correspondance appliquées (voir docs/Repartition_Taches) :
 *  - xs:element avec xs:complexType         -> une classe Java
 *  - xs:element avec type simple (xs:string) -> attribut + getter/setter
 *  - xs:sequence                             -> ordre des attributs
 *  - maxOccurs="unbounded"                   -> List<T> avec un seul getter
 *    (pas de setter, on ajoute via getXxx().add(...))
 *
 * Les classes générées héritent de AbstractXmlElement (Membre 4), afin
 * d'exposer une méthode save() compatible avec l'exemple imposé :
 *
 *   Bibliotheque biblio = new Bibliotheque();
 *   biblio.getLivres().add(livre);
 *   biblio.save("output/bibliotheque.xml");
 */
public class JavaCodeGenerator {

    private static final String GENERATED_PACKAGE = "com.ifall.xsdcodegen.generated";

    /**
     * Génère un fichier .java pour chaque élément complexe du registre,
     * dans outputDir/com/ifall/xsdcodegen/generated/.
     */
    public void generate(Map<String, ElementDef> registry, File outputDir) throws IOException {
        for (ElementDef def : registry.values()) {
            if (def.isComplex()) {
                String classCode = generateClass(def);
                writeToFile(outputDir, def.getName(), classCode);
            }
        }
    }

    // --- Génération du code d'une classe ---------------------------------

    private String generateClass(ElementDef def) {
        String className = capitalize(def.getName());
        boolean needsListImports = def.getChildren().stream().anyMatch(ElementDef::isList);

        StringBuilder fields = new StringBuilder();
        StringBuilder accessors = new StringBuilder();

        for (ElementDef child : def.getChildren()) {
            String childName = child.getName();
            String childClassName = capitalize(childName);

            if (child.isList()) {
                String fieldName = pluralize(childName);
                String getterName = "get" + capitalize(fieldName);
                fields.append("    private List<").append(childClassName).append("> ")
                        .append(fieldName).append(" = new ArrayList<>();\n\n");
                accessors.append("    public List<").append(childClassName).append("> ")
                        .append(getterName).append("() {\n")
                        .append("        return ").append(fieldName).append(";\n")
                        .append("    }\n\n");
            } else if (child.isComplex()) {
                // Élément complexe imbriqué (pas une liste) : référence directe
                fields.append("    private ").append(childClassName).append(" ")
                        .append(childName).append(";\n\n");
                accessors.append(getterSetterBlock(childClassName, childName));
            } else {
                String javaType = mapSimpleType(child.getSimpleType());
                fields.append("    private ").append(javaType).append(" ")
                        .append(childName).append(";\n\n");
                accessors.append(getterSetterBlock(javaType, childName));
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(GENERATED_PACKAGE).append(";\n\n");
        sb.append("import com.ifall.xsdcodegen.runtime.AbstractXmlElement;\n");
        if (needsListImports) {
            sb.append("import java.util.ArrayList;\n");
            sb.append("import java.util.List;\n");
        }
        sb.append("\n");
        sb.append("/**\n")
                .append(" * Classe generee automatiquement a partir du schema XSD (element \"")
                .append(def.getName()).append("\").\n")
                .append(" * Generee par JavaCodeGenerator (Membre 3) - ne pas modifier a la main,\n")
                .append(" * regenerer plutot depuis le XSD.\n")
                .append(" */\n");
        sb.append("public class ").append(className).append(" extends AbstractXmlElement {\n\n");
        sb.append(fields);
        sb.append("    public ").append(className).append("() {\n")
                .append("        super(\"").append(def.getName()).append("\");\n")
                .append("    }\n\n");
        sb.append(accessors);
        sb.append("}\n");
        return sb.toString();
    }

    private String getterSetterBlock(String type, String fieldName) {
        String cap = capitalize(fieldName);
        StringBuilder sb = new StringBuilder();
        sb.append("    public ").append(type).append(" get").append(cap).append("() {\n")
                .append("        return ").append(fieldName).append(";\n")
                .append("    }\n\n");
        sb.append("    public void set").append(cap).append("(").append(type).append(" ")
                .append(fieldName).append(") {\n")
                .append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n")
                .append("    }\n\n");
        return sb.toString();
    }

    // --- Écriture sur disque -----------------------------------------------

    private void writeToFile(File outputDir, String elementName, String content) throws IOException {
        File pkgDir = new File(outputDir, GENERATED_PACKAGE.replace('.', '/'));
        if (!pkgDir.exists() && !pkgDir.mkdirs()) {
            throw new IOException("Impossible de creer le dossier : " + pkgDir);
        }
        File file = new File(pkgDir, capitalize(elementName) + ".java");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }

    // --- Helpers -------------------------------------------------------------

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private String pluralize(String s) {
        if (s == null || s.isEmpty() || s.endsWith("s")) {
            return s;
        }
        return s + "s";
    }

    /**
     * Correspondance simple types XSD -> types Java.
     * Seul xs:string est utilisé dans l'exemple fourni ; les autres cas
     * sont couverts par prudence mais restent hors périmètre du projet.
     */
    private String mapSimpleType(String xsdType) {
        if (xsdType == null) {
            return "String";
        }
        switch (xsdType) {
            case "xs:string":
                return "String";
            case "xs:int":
            case "xs:integer":
                return "Integer";
            case "xs:boolean":
                return "Boolean";
            case "xs:double":
                return "Double";
            case "xs:float":
                return "Float";
            case "xs:long":
                return "Long";
            default:
                return "String";
        }
    }
}
package com.ifall.xsdcodegen.runtime;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Classe de base commune à toutes les classes générées par le Membre 3
 * (JavaCodeGenerator).
 *
 * ATTENTION : ceci est un PLACEHOLDER écrit par le Membre 3 uniquement pour
 * pouvoir compiler et tester le générateur de façon autonome, en attendant
 * l'implémentation réelle de la sérialisation par le Membre 4.
 *
 * implémentation du Membre 4 : sérialisation Java -> XML par réflexion.
 *
 * Principe : chaque instance d'une sous-classe (ex. Bibliotheque, Livre)
 * connaît son nom de balise XML (elementName, fixé par le constructeur
 * généré, ex. super("livre")). save() parcourt récursivement, via
 * java.lang.reflect, les champs *déclarés* de la sous-classe concrète
 * (getClass().getDeclaredFields() ne remonte pas à AbstractXmlElement,
 * donc le champ elementName lui-même n'est jamais sérialisé) :
 *
 *  - un champ List<?> d'éléments AbstractXmlElement  -> répétition de
 *    balises, une par élément de la liste (ex. plusieurs <livre>...</livre>
 *    dans <bibliotheque>) ;
 *  - un champ qui est lui-même un AbstractXmlElement (imbrication simple,
 *    non-liste) -> une balise imbriquée récursive ;
 *  - un champ "simple" (String, Integer, Boolean, ...) -> une balise
 *    feuille contenant sa valeur textuelle (échappée).
 *
 * Champs null : ignorés (pas de balise vide générée
 */

public abstract class AbstractXmlElement {
    protected final String elementName;

    protected AbstractXmlElement(String elementName) {
        this.elementName = elementName;
    }

    public String getElementName() {
        return elementName;
    }


      /**
     * Sérialise cet objet (et ses enfants) en XML vers le fichier indiqué.
     * TODO (Membre 4) : implémentation réelle de la sérialisation.
     */


    /**
     * Sérialise cet objet (et ses enfants) en XML, vers un chemin par
     * défaut "output/<elementName>.xml". Correspond à l'appel
     * biblio.save() de l'exemple imposé.
     */




    public void save() {
        save("output/" + elementName + ".xml");
    }

    /**
     * Sérialise cet objet (et ses enfants) en XML vers le fichier indiqué.
     * Crée les dossiers parents si nécessaire.
     */
    public void save(String path) {
        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new RuntimeException("Impossible de créer le dossier : " + parent);
        }
 
        StringBuilder xml = new StringBuilder();
        serialize(this, 0, xml);
 
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            writer.print(xml);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'écriture du fichier XML : " + path, e);
        }
    }



    // --- Sérialisation récursive par réflexion --------------------------
 
    private static void serialize(AbstractXmlElement element, int depth, StringBuilder sb) {
        String indent = indent(depth);
        String tag = element.getElementName();
 
        sb.append(indent).append('<').append(tag).append(">\n");
 
        for (Field field : element.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(element);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Impossible de lire le champ " + field.getName(), e);
            }
            if (value == null) {
                continue;
            }
 
            if (value instanceof List<?>) {
                for (Object item : (List<?>) value) {
                    writeValue(field.getName(), item, depth + 1, sb);
                }
            } else {
                writeValue(field.getName(), value, depth + 1, sb);
            }
        }
 
        sb.append(indent).append("</").append(tag).append(">\n");
    }



    /**
     * Écrit une valeur (élément de liste ou champ simple) comme balise.
     * Le nom de balise d'un AbstractXmlElement vient de getElementName()
     * (fiable, indépendant du nom du champ Java, ex. "livres" -> <livre>).
     * Pour un type simple, le nom de champ Java sert de nom de balise
     * (ex. "titre" -> <titre>).
     */
    private static void writeValue(String fieldName, Object value, int depth, StringBuilder sb) {
        if (value instanceof AbstractXmlElement) {
            serialize((AbstractXmlElement) value, depth, sb);
        } else {
            String indent = indent(depth);
            sb.append(indent).append('<').append(fieldName).append('>')
                    .append(escapeXml(String.valueOf(value)))
                    .append("</").append(fieldName).append(">\n");
        }
    }
 
    private static String indent(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("    ");
        }
        return sb.toString();
    }
 
    private static String escapeXml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
 

}

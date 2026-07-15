package com.ifall.xsdcodegen.runtime;
/**
 * Classe de base commune à toutes les classes générées par le Membre 3
 * (JavaCodeGenerator).
 *
 * ATTENTION : ceci est un PLACEHOLDER écrit par le Membre 3 uniquement pour
 * pouvoir compiler et tester le générateur de façon autonome, en attendant
 * l'implémentation réelle de la sérialisation par le Membre 4.
 *
 * Le Membre 4 doit reprendre cette classe et implémenter save() :
 *  - soit via réflexion (java.lang.reflect) pour parcourir les champs
 *    (attributs simples -> balises, List<T> -> répétition de balises,
 *    objets complexes -> imbrication),
 *  - soit en coordination avec le Membre 3 si le générateur doit produire
 *    du code d'écriture XML explicite dans chaque classe.
 *
 * Contrat à ne pas casser (utilisé par les classes générées) :
 *  - un constructeur protégé prenant le nom de balise XML (elementName)
 *  - une méthode publique save(String path)
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
    public void save(String path) {
        throw new UnsupportedOperationException(
                "save() n'est pas encore implémenté : c'est la tâche du Membre 4 "
                        + "(mécanisme de sérialisation Java -> XML).");
    }
}

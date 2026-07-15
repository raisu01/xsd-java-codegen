package com.ifall.xsdcodegen.parser;

import com.ifall.xsdcodegen.model.ElementDef;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lit un fichier XSD et construit la structure intermédiaire (ElementDef)
 * décrivant les éléments (simples ou complexes), leurs enfants et leurs
 * références.
 *
 */
public class XsdParser {

    public Map<String, ElementDef> parse(File xsdFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document doc = factory.newDocumentBuilder().parse(xsdFile);
        Element schema = doc.getDocumentElement();

        Map<String, ElementDef> registry = new LinkedHashMap<>();

        // Passe 1 : enregistrer tous les xs:element déclarés au premier niveau
        for (Element el : childElements(schema, "element")) {
            String name = el.getAttribute("name");
            registry.put(name, new ElementDef(name));
        }

        // Passe 2 : résoudre le contenu (type simple, ou séquence d'enfants/refs)
        for (Element el : childElements(schema, "element")) {
            String name = el.getAttribute("name");
            resolveContent(registry, el, registry.get(name));
        }

        return registry;
    }

    /**
     * Remplit un ElementDef à partir de son élément DOM : type simple si pas
     * de complexType, sinon liste d'enfants résolus (via ref ou déclaration
     * imbriquée). Appelée à la fois pour les éléments de premier niveau et,
     * récursivement, pour les éléments imbriqués sans ref.
     */
    private void resolveContent(Map<String, ElementDef> registry, Element el, ElementDef def) {
        Element complexType = firstChildElement(el, "complexType");
        if (complexType != null) {
            def.setComplex(true);
            Element sequence = firstChildElement(complexType, "sequence");
            if (sequence != null) {
                for (Element childEl : childElements(sequence, "element")) {
                    def.getChildren().add(resolveChild(registry, childEl));
                }
            }
        } else {
            String type = el.getAttribute("type");
            if (!type.isEmpty()) {
                def.setSimpleType(type);
            }
        }
    }

    private ElementDef resolveChild(Map<String, ElementDef> registry, Element childEl) {
        String ref = childEl.getAttribute("ref");
        String name = ref.isEmpty() ? childEl.getAttribute("name") : ref;

        ElementDef childDef = registry.get(name);
        boolean alreadyResolved = childDef != null;
        if (childDef == null) {
            childDef = new ElementDef(name);
            registry.put(name, childDef);
        }
        if ("unbounded".equals(childEl.getAttribute("maxOccurs"))) {
            childDef.setList(true);
        }
        // Déclaration imbriquée (pas de ref) : il faut résoudre son propre
        // contenu ici, car elle n'est pas un enfant direct de <xs:schema> et
        // ne sera donc jamais traitée par la boucle de premier niveau.
        if (ref.isEmpty() && !alreadyResolved) {
            resolveContent(registry, childEl, childDef);
        }
        return childDef;
    }

    private boolean isElement(Node node, String localName) {
        return node.getNodeType() == Node.ELEMENT_NODE && localName.equals(node.getLocalName());
    }

    private Element firstChildElement(Element parent, String localName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (isElement(n, localName)) {
                return (Element) n;
            }
        }
        return null;
    }

    private List<Element> childElements(Element parent, String localName) {
        List<Element> result = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (isElement(n, localName)) {
                result.add((Element) n);
            }
        }
        return result;
    }
}

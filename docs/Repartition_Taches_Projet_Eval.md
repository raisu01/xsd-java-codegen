# Répartition des tâches — Générateur de code Java à partir de schémas XML

**Projet :** Mini-projet — Génération de classes Java depuis un XSD, avec sérialisation en XML
**Équipe :** 5 membres

---

## Vue d'ensemble du projet

Le projet comporte 4 grands blocs :
1. Définition des règles de correspondance XSD ↔ Java
2. Parsing du fichier XSD
3. Génération des classes Java
4. Modèle Java "runtime" (classes de base + sérialisation) et tests

---

## Membre 1 — Analyse & règles de correspondance (Chef de projet / Analyste)

**Rôle :** Poser les fondations conceptuelles du projet et coordonner l'équipe.

- Étudier l'exemple XSD fourni et lister tous les concepts XSD utilisés (`xs:element`, `xs:complexType`, `xs:sequence`, `ref`, `maxOccurs`, types simples comme `xs:string`)
- Rédiger le tableau de correspondance XSD → Java, par exemple :
  | Concept XSD | Concept Java |
  |---|---|
  | `xs:element` avec `xs:complexType` | classe Java |
  | `xs:element` avec type simple (`xs:string`) | attribut Java (String) + getter/setter |
  | `xs:sequence` | ordre des attributs dans la classe |
  | `maxOccurs="unbounded"` | `List<T>` avec getter (ex. `getLivres()`) |
  | `ref="x"` | référence à un autre élément déjà défini |
- Documenter les limites volontaires (pas de gestion des `xs:choice`, `xs:attribute`, restrictions, namespaces avancés, etc.)
- Rédiger l'introduction et la conclusion du rapport final
- Coordonner l'intégration des modules des autres membres

**Livrable :** `regles_correspondance.md` (ou section du rapport)

---

## Membre 2 — Parsing du fichier XSD

**Rôle :** Lire et interpréter le fichier XSD en mémoire.

- Choisir une librairie de parsing XML (ex. `javax.xml.parsers` DOM, ou JAXP)
- Développer une classe `XsdParser` qui :
  - Lit le fichier `.xsd` fourni
  - Extrait la liste des éléments (`xs:element`), avec leur nom, leur type (simple ou complexe), et leurs enfants (`xs:sequence`)
  - Gère les références (`ref="livre"`) en les reliant à la définition correspondante
  - Détecte les `maxOccurs="unbounded"` pour marquer les éléments comme des listes
- Construire une structure de données intermédiaire (ex. classes `ElementDef`, `ComplexTypeDef`) représentant le schéma de façon exploitable par le générateur

**Livrable :** `XsdParser.java` + classes modèles intermédiaires (`ElementDef`, `ComplexTypeDef`)

**Dépendance :** transmet sa structure de données au Membre 3

---

## Membre 3 — Générateur de classes Java

**Rôle :** Transformer la structure XSD parsée en fichiers `.java`.

- Développer une classe `JavaCodeGenerator` qui, à partir des `ElementDef`/`ComplexTypeDef` du Membre 2 :
  - Génère une classe Java par type complexe (ex. `Bibliotheque.java`, `Livre.java`)
  - Génère les attributs avec le bon type (`String`, ou `List<Livre>` si `maxOccurs="unbounded"`)
  - Génère les getters/setters standards (`getTitre()`, `setTitre()`, `getLivres()`)
  - Écrit les fichiers `.java` générés sur disque dans un dossier `generated/`
- Utiliser soit de la génération de texte simple (concaténation/StringBuilder), soit un moteur de template (ex. StringTemplate, Freemarker) si le temps le permet

**Livrable :** `JavaCodeGenerator.java` + dossier `generated/` contenant les `.java` produits

**Dépendance :** reçoit la structure du Membre 2, doit produire des classes compatibles avec le mécanisme `save()` du Membre 4

---

## Membre 4 — Mécanisme de sérialisation (méthode `save()`)

**Rôle :** Permettre aux classes générées de s'auto-sérialiser en XML.

- Concevoir la classe de base ou l'interface commune utilisée par les classes générées (ex. `AbstractXmlElement` avec méthode `save()`)
- Implémenter la sérialisation Java → XML :
  - Utiliser la réflexion Java (`java.lang.reflect`) pour parcourir les attributs de l'objet, **ou**
  - Prévoir dans le code généré (avec le Membre 3) des appels explicites d'écriture XML
- Gérer correctement :
  - Les attributs simples (`<titre>...</titre>`)
  - Les listes d'objets (répétition de balises `<livre>` pour chaque élément de la liste)
  - L'imbrication des balises (respect de la hiérarchie `bibliotheque > livre > titre/auteur/editeur`)
- S'assurer que le fichier XML produit respecte l'indentation et la structure de l'exemple fourni

**Livrable :** `AbstractXmlElement.java` (ou équivalent) + logique de sérialisation

**Dépendance :** travaille en binôme rapproché avec le Membre 3 pour aligner l'interface des classes générées

---

## Membre 5 — Tests, validation & documentation finale

**Rôle :** Vérifier que le pipeline complet fonctionne et rédiger le rapport.

- Écrire une classe `Main`/`Test` reproduisant l'exemple imposé :
  ```java
  Livre livre = new Livre();
  livre.setTitre("toto");
  livre.setAuteur("titi");
  livre.setEditeur("tutu");
  Bibliotheque biblio = new Bibliotheque();
  biblio.getLivres().add(livre);
  biblio.save();
  ```
- Tester avec plusieurs livres (comme dans le XML d'exemple à 3 `<livre>`) pour valider la gestion des listes
- Comparer le XML généré avec le XML attendu (diff manuel ou automatisé)
- Recenser les bugs/limitations rencontrés et les transmettre aux membres concernés
- Rédiger la documentation finale : mode d'emploi, captures d'écran des résultats, structure du code, difficultés rencontrées
- Préparer le support de soutenance/démonstration si nécessaire

**Livrable :** `TestMain.java`, rapport de tests, documentation finale (README)

---

## Planning suggéré

| Étape | Membres impliqués | Dépend de |
|---|---|---|
| 1. Règles de correspondance | M1 | — |
| 2. Parsing XSD | M2 | M1 (règles validées) |
| 3. Génération des classes | M3 | M2 |
| 4. Sérialisation `save()` | M4 | M3 (en parallèle, coordination continue) |
| 5. Tests & documentation | M5 | M3 + M4 |

**Conseil :** M3 et M4 doivent se coordonner tôt sur l'interface commune des classes générées (nom de la méthode `save()`, structure des attributs), afin d'éviter un blocage en fin de projet.

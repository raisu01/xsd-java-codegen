# Tâche 2 — Parsing du fichier XSD

**Membre :** M2
**Branche :** `m2`
**Fichiers :**
- `src/main/java/com/ifall/xsdcodegen/model/ElementDef.java`
- `src/main/java/com/ifall/xsdcodegen/parser/XsdParser.java`
- `src/main/java/com/ifall/xsdcodegen/parser/ParserDemo.java`

---

## 1. Rôle de la tâche

Le rôle de M2 est de **lire un fichier `.xsd` et de le transformer en une
structure de données Java exploitable**, sans écrire de logique de
génération de code ni de sérialisation — ce n'est pas son périmètre.

Concrètement, à partir d'un fichier XSD comme `example.xsd` :

```xml
<xs:element name="bibliotheque">
  <xs:complexType>
    <xs:sequence>
      <xs:element ref="livre" maxOccurs="unbounded"/>
    </xs:sequence>
  </xs:complexType>
</xs:element>
<xs:element name="livre">
  <xs:complexType>
    <xs:sequence>
      <xs:element ref="titre"/>
      <xs:element ref="auteur"/>
      <xs:element ref="editeur"/>
    </xs:sequence>
  </xs:complexType>
</xs:element>
<xs:element name="titre" type="xs:string"/>
<xs:element name="auteur" type="xs:string"/>
<xs:element name="editeur" type="xs:string"/>
```

M2 doit produire une représentation en mémoire qui dit, pour chaque
élément :
- son **nom** (`bibliotheque`, `livre`, `titre`...)
- s'il est **simple** (a un `type="xs:string"`) ou **complexe** (a un
  `complexType`/`sequence`)
- la **liste ordonnée de ses enfants**, avec les `ref` déjà résolus
  vers la bonne définition
- si un enfant doit être traité comme une **liste** (`maxOccurs="unbounded"`)

Cette représentation est ensuite transmise à M3, qui s'en sert pour
générer les fichiers `.java` (une classe par élément complexe, un
attribut `List<Livre>` ou `String` par enfant selon le cas).

**Hors périmètre volontaire** (voir `docs/Repartition_Taches_Projet_Eval.md`) :
`xs:choice`, `xs:attribute`, restrictions, namespaces avancés — le sujet
(`docs/Mini_Projet_Eval.pdf`) demande explicitement de ne pas aller
dans les concepts XSD trop avancés.

---

## 2. Interactions du code

```
example.xsd
    │
    ▼
XsdParser.parse(File)
    │  construit et remplit
    ▼
Map<String, ElementDef>   ◄── "registre" : nom d'élément → définition
    │
    │  chaque ElementDef complexe référence directement
    │  ses ElementDef enfants (pas de chaîne de caractères,
    │  pas de deuxième lookup nécessaire côté consommateur)
    ▼
consommé par M3 (JavaCodeGenerator, pas encore développé)
```

- **`ElementDef`** est une classe passive (pas de logique), un simple
  porte-données. Elle représente un unique élément XSD, qu'il soit
  racine, complexe imbriqué, ou simple.
- **`XsdParser`** est la seule classe qui contient de la logique. Elle
  ne renvoie jamais de `String` en guise de référence : les enfants
  d'un `ElementDef` complexe sont directement des objets `ElementDef`
  déjà résolus. Celui qui consomme la structure (M3) n'a donc pas
  besoin de refaire un lookup par nom.
- **`ParserDemo`** est un point d'entrée `main()` indépendant, utilisé
  uniquement pour valider visuellement le résultat du parsing. Il ne
  dépend d'aucun code des autres membres (pas de `Bibliotheque`, pas de
  `save()`) — il peut tourner seul, même si M3/M4/M5 n'ont encore rien
  livré.

Design choisi : le sujet suggère deux classes (`ElementDef`,
`ComplexTypeDef`). Une seule classe `ElementDef` a été utilisée à la
place, avec un flag `complex` — elle porte à la fois les infos "type
simple" et "type complexe", ce qui simplifie l'API sans perdre
d'information, vu le nombre limité de cas à couvrir.

---

## 3. Le code, en détail

### 3.1 `ElementDef` — le modèle

```java
public class ElementDef {
    private final String name;
    private String simpleType;      // ex. "xs:string", null si complexe
    private boolean complex;        // true si l'élément a un complexType
    private boolean list;           // true si cet élément doit être une List<T>
    private final List<ElementDef> children = new ArrayList<>();
    ...
}
```

Points importants :

- **`list` est une propriété de l'objet `ElementDef` lui-même**, pas de
  la relation parent → enfant. Dans `example.xsd`, `livre` n'est
  référencé qu'à un seul endroit (`maxOccurs="unbounded"` dans
  `bibliotheque`), donc marquer directement l'objet `ElementDef` de
  `livre` comme "liste" fonctionne. **Limite connue** : si un même
  élément était référencé à deux endroits différents avec des
  `maxOccurs` différents, ce flag serait partagé à tort entre les deux
  usages. Ce cas n'existe pas dans le XSD fourni par le sujet, donc ce
  raccourci est volontaire et documenté ici pour transmission à
  l'équipe.
- `simpleType` et `complex`/`children` sont mutuellement exclusifs en
  pratique : un élément est soit simple (a un `simpleType`), soit
  complexe (a des `children`), jamais les deux — c'est le parseur qui
  garantit cette invariant, pas la classe elle-même.

### 3.2 `XsdParser` — la logique de lecture

`parse(File xsdFile)` fonctionne en **deux passes** sur les enfants
directs de la racine `<xs:schema>` :

**Passe 1 — enregistrement des noms**

```java
for (Element el : childElements(schema, "element")) {
    String name = el.getAttribute("name");
    registry.put(name, new ElementDef(name));
}
```

Chaque `xs:element` de premier niveau est enregistré dans une
`Map<String, ElementDef>` (`registry`), avec un objet vide au départ.
**Pourquoi une passe séparée ?** Pour que les `ref="..."` puissent être
résolus dans n'importe quel ordre, y compris vers un élément déclaré
*après* celui qui le référence dans le fichier XSD (l'ordre des
`xs:element` dans le XSD n'a pas à correspondre à l'ordre de
référencement).

**Passe 2 — remplissage du contenu**

Pour chaque élément, on regarde s'il a un `xs:complexType` enfant :

- **Si oui** → élément complexe. On cherche le `xs:sequence` à
  l'intérieur, et pour chaque `xs:element` de cette séquence, on appelle
  `resolveChild(...)` pour obtenir (ou créer) l'`ElementDef` cible.
- **Si non** → élément simple. On lit l'attribut `type` (ex.
  `xs:string`) et on le stocke tel quel.

**`resolveChild`** gère la résolution des références :

```java
String ref = childEl.getAttribute("ref");
String name = ref.isEmpty() ? childEl.getAttribute("name") : ref;
ElementDef childDef = registry.get(name);
if (childDef == null) {
    childDef = new ElementDef(name);
    registry.put(name, childDef);
}
if ("unbounded".equals(childEl.getAttribute("maxOccurs"))) {
    childDef.setList(true);
}
return childDef;
```

- Si l'enfant utilise `ref="livre"`, on va chercher `livre` dans le
  registre (déjà créé en passe 1).
- Si l'enfant est déclaré inline avec `name="..."` (cas non utilisé
  dans `example.xsd` mais géré par robustesse), on le crée à la volée.
- Le `maxOccurs="unbounded"` est lu **sur la balise de référence**
  elle-même (`childEl`), pas sur la définition — c'est la bonne source
  d'information, puisque dans le XSD c'est l'usage qui porte la
  cardinalité, pas la déclaration du type.

**Utilitaires privés** (`isElement`, `firstChildElement`,
`childElements`) : le DOM standard (`NodeList`) mélange les nœuds
texte (retours à la ligne, indentation) avec les nœuds éléments. Ces
trois méthodes filtrent pour ne garder que les `Element` dont le nom
local correspond (ex. `"element"`, `"sequence"`, `"complexType"`),
en ignorant le namespace `xs:` grâce à `setNamespaceAware(true)` +
`getLocalName()`.

### 3.3 `ParserDemo` — validation autonome

Lance `XsdParser` sur `src/main/resources/example.xsd` et affiche
chaque élément trouvé, avec ses enfants et le flag `[liste]` si
présent. Sortie obtenue et vérifiée :

```
bibliotheque (complexe)
  -> livre [liste]
livre (complexe)
  -> titre
  -> auteur
  -> editeur
titre (simple: xs:string)
auteur (simple: xs:string)
editeur (simple: xs:string)
```

Ce résultat confirme que la structure produite correspond exactement à
ce qu'attend le générateur (M3) : `Bibliotheque` doit avoir une
`List<Livre>`, `Livre` doit avoir trois attributs `String`.

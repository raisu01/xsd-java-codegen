# Tâche 5 — Tests, validation & documentation finale

**Membre :** M5
**Branche :** `m5`
**Fichier :** `src/main/java/com/ifall/xsdcodegen/test/TestMain.java`
**Dépend de :** M2 (parsing) + M3 (génération) + M4 (sérialisation), déjà
mergés/disponibles au moment de l'écriture de ce document.

---

## 1. Rôle de la tâche

M5 ne développe aucune nouvelle brique du pipeline : son rôle est de
**vérifier que le pipeline complet fonctionne**, en l'utilisant
exactement comme le ferait un utilisateur final — via les classes
générées (`Bibliotheque`, `Livre`), sans jamais appeler `XsdParser`
ou `JavaCodeGenerator` directement.

Deux scénarios sont couverts, tous deux tirés du sujet
(`docs/Mini_Projet_Eval.pdf`) :
1. **L'exemple imposé** (page 1) : un livre, `save()` sans argument.
2. **L'exemple à 3 `<livre>`** (page 2) : valide que
   `maxOccurs="unbounded"` (donc `List<Livre>`) fonctionne pour
   plusieurs éléments, pas seulement un.

## 2. Interactions du code

```
GeneratorDemo (M3)             ← étape préalable, hors de TestMain
    │  écrit generated/.../Bibliotheque.java, Livre.java
    ▼
TestMain (M5)
    │  new Bibliotheque(), new Livre(), setters, getLivres().add(...)
    ▼
AbstractXmlElement.save()  (M4, héritée par Bibliotheque/Livre)
    │  sérialise par réflexion
    ▼
output/*.xml
    │
    ▼
TestMain relit le fichier et vérifie son contenu
```

`TestMain` ne dépend **que de l'API publique** exposée par les classes
générées (constructeurs, getters/setters, `save()`) — exactement
l'API que M1 a fixée dans `regles_correspondance.md` (section 3) et
que M3/M4 ont respectée. `generated/` n'étant pas versionné (voir
`.gitignore`), il faut lancer `GeneratorDemo` avant de pouvoir
compiler `TestMain` (voir `README.md`, section "Compiler et
exécuter").

## 3. Le code, en détail

### 3.1 `exempleImpose()`

Reproduit **littéralement** le code Java donné en page 1 du sujet :

```java
Livre livre = new Livre();
livre.setTitre("toto");
livre.setAuteur("titi");
livre.setEditeur("tutu");

Bibliotheque biblio = new Bibliotheque();
biblio.getLivres().add(livre);

biblio.save(); // écrit output/bibliotheque.xml (chemin par défaut)
```

`save()` sans argument existe côté `AbstractXmlElement` (M4) :
`save() { save("output/" + elementName + ".xml"); }`, avec
`elementName = "bibliotheque"` fixé par le constructeur généré
(`super("bibliotheque")`). Le fichier produit est donc relu depuis
`output/bibliotheque.xml`, et le test vérifie par `String.contains`
que les balises et valeurs attendues (`<titre>toto</titre>`, etc.)
sont bien présentes.

**Pourquoi `contains` et pas un diff exact caractère-par-caractère ?**
Voir section 4 — l'indentation produite (4 espaces) diffère de celle
de l'exemple illustratif du sujet (2 espaces), sans que ça remette en
cause la structure. Un diff strict aurait échoué pour une raison
purement cosmétique.

### 3.2 `exempleTroisLivres()`

Reproduit l'exemple XML à 3 `<livre>` (page 2 du sujet) en créant 3
`Livre` dans une boucle, ajoutés à la même `Bibliotheque` :

```java
for (int i = 1; i <= 3; i++) {
    Livre livre = new Livre();
    livre.setTitre("titre " + i);
    ...
    biblio.getLivres().add(livre);
}
biblio.save("output/bibliotheque_3_livres.xml");
```

Le test vérifie :
- que les 3 triplets `<titre>`/`<auteur>`/`<editeur>` attendus sont
  bien présents dans le fichier produit
- que le nombre de balises `<livre>` ouvrantes est exactement 3
  (`produit.lines().filter(...).count()`), pour détecter un bug de
  génération de liste qui dupliquerait ou en perdrait

## 4. Comparaison avec le XML attendu — résultat

Sortie réelle obtenue en exécutant le pipeline complet (voir
`README.md` pour les commandes) :

```xml
<bibliotheque>
    <livre>
        <titre>toto</titre>
        <auteur>titi</auteur>
        <editeur>tutu</editeur>
    </livre>
</bibliotheque>
```

```xml
<bibliotheque>
    <livre>
        <titre>titre 1</titre>
        <auteur>auteur 1</auteur>
        <editeur>editeur 1</editeur>
    </livre>
    <livre>
        <titre>titre 2</titre>
        <auteur>auteur 2</auteur>
        <editeur>editeur 2</editeur>
    </livre>
    <livre>
        <titre>titre 3</titre>
        <auteur>auteur 3</auteur>
        <editeur>editeur 3</editeur>
    </livre>
</bibliotheque>
```

**Structure identique** à l'exemple du sujet (imbrication
`bibliotheque > livre > titre/auteur/editeur`, répétition d'une
balise `<livre>` par élément de la liste).

**Écart connu, cosmétique** : l'indentation produite par
`AbstractXmlElement.serialize()` (M4) est de 4 espaces par niveau,
contre 2 espaces dans l'exemple du sujet. Aucune balise, aucun
attribut, aucune valeur ne diffère — uniquement le nombre d'espaces
d'indentation. Signalé pour transmission à M4 si une correspondance
strictement caractère-par-caractère devait être exigée un jour ; à ce
stade, jugé non-bloquant pour la validation fonctionnelle.

## 5. Bugs/limitations recensés (transmis aux membres concernés)

- **M2** : limite documentée dans `docs/Tache2_Parsing_XSD.md` sur les
  XSD imbriqués sans `ref` — corrigée depuis (voir ce document,
  section 4).
- **M4** : indentation à 4 espaces vs 2 espaces dans l'exemple du
  sujet (voir section 4 ci-dessus) — cosmétique, non-bloquant.
- Aucun autre bug fonctionnel constaté : les 2 scénarios testés
  passent intégralement.

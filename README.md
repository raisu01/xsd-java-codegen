# xsd-java-codegen

Générateur de classes Java à partir d'un schéma XSD, avec sérialisation
automatique en XML (`save()`). Mini-projet d'équipe (5 membres).

## Objectif

Permettre de manipuler et produire du XML sans écrire de XML : on part
d'un schéma XSD, on génère les classes Java correspondantes (avec
getters/setters), et ces classes savent s'auto-sérialiser en XML.

Exemple visé (voir `docs/Mini_Projet_Eval.pdf`) :

```java
Livre livre = new Livre();
livre.setTitre("toto");
livre.setAuteur("titi");
livre.setEditeur("tutu");

Bibliotheque biblio = new Bibliotheque();
biblio.getLivres().add(livre);
biblio.save(); // écrit output/bibliotheque.xml
```

## État du projet

Pipeline complet et fonctionnel : parsing du XSD (M2), génération des
classes Java (M3), sérialisation `save()` par réflexion (M4) et tests
de bout en bout (M5). Voir `docs/Repartition_Taches_Projet_Eval.md`
pour le détail par membre et `regles_correspondance.md` pour les
règles de correspondance XSD ↔ Java (M1).

## Structure du projet

```
docs/                                    Sujet, répartition des tâches
regles_correspondance.md                 Règles de correspondance XSD ↔ Java (M1)
src/main/resources/example.xsd           Schéma XSD fourni en exemple
src/main/java/com/ifall/xsdcodegen/
  model/ElementDef.java                  Structure intermédiaire (M2)
  parser/XsdParser.java                  Parsing du XSD (M2)
  parser/ParserDemo.java                 Démo de validation du parsing (M2)
  generator/JavaCodeGenerator.java       Génération des .java (M3)
  generator/GeneratorDemo.java           Démo : parsing -> génération (M3)
  runtime/AbstractXmlElement.java        Sérialisation save() par réflexion (M4)
  test/TestMain.java                     Reproduit l'exemple imposé (M5)
generated/                               Classes Java générées (non versionné, régénéré à chaque build)
output/                                  XML produit par TestMain (non versionné)
```

## Compiler et exécuter (sans Maven)

Le code généré (`generated/`) n'est pas versionné : il faut d'abord le
produire à partir du XSD avant de pouvoir compiler `TestMain`, qui en
dépend directement.

```bash
# 1. Compiler tout sauf TestMain (qui dépend des classes pas encore générées)
find src/main/java/com/ifall/xsdcodegen/{model,parser,generator,runtime} -name "*.java" \
  | xargs javac -d target/classes

# 2. Générer les classes Java à partir du XSD (parsing + génération)
java -cp target/classes com.ifall.xsdcodegen.generator.GeneratorDemo

# 3. Compiler les classes générées + TestMain, puis rejouer l'exemple imposé
find generated src/main/java/com/ifall/xsdcodegen/test -name "*.java" \
  | xargs javac -cp target/classes -d target/classes
java -cp target/classes com.ifall.xsdcodegen.test.TestMain
```

`TestMain` reproduit l'exemple imposé par le sujet (1 livre, `save()`
par défaut) et l'exemple à 3 `<livre>` (validation de la gestion des
listes), et vérifie que le XML produit contient bien les valeurs
attendues.

## Répartition des tâches

Voir `docs/Repartition_Taches_Projet_Eval.md` pour le détail par membre
et les branches de travail (`m1` à `m5`). Chaque membre est responsable
de sa tâche et développe uniquement sur sa branche.

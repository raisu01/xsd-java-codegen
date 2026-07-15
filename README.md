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
biblio.save("output/bibliotheque.xml");
```

## État du projet

Base de départ uniquement : le squelette Maven et le sujet sont en
place, mais **aucun module métier n'est encore implémenté**. Chaque
membre développe sa partie sur sa propre branche (`m1` à `m5`), selon
la répartition ci-dessous.

## Structure du projet

```
docs/                                   Sujet, répartition des tâches
src/main/resources/example.xsd          Schéma XSD fourni en exemple
src/main/java/com/ifall/xsdcodegen/     À créer par chaque membre (voir répartition)
```

## Répartition des tâches

Voir `docs/Repartition_Taches_Projet_Eval.md` pour le détail par membre
et les branches de travail (`m1` à `m5`). Chaque membre est responsable
de sa tâche et développe uniquement sur sa branche.

# Comment lancer le projet

Guide pratique, pas à pas, pour compiler et exécuter le pipeline
complet (parsing XSD → génération Java → sérialisation XML). Pour la
vue d'ensemble du projet, voir `README.md`.

## Prérequis

- JDK 11 ou supérieur (`java -version`, `javac -version`)
- Aucune dépendance externe : tout est fait avec les bibliothèques
  standard du JDK (`javax.xml.parsers`, `java.lang.reflect`)

## Commande unique

```bash
./run.sh          # bash / Git Bash
```
```powershell
.\run.ps1          # PowerShell
```

Chaque script enchaîne automatiquement les 4 étapes ci-dessous (nettoyage,
compilation, génération, tests) et s'arrête au premier échec. Utile pour
lancer rapidement ; le détail étape par étape reste utile pour comprendre
ce qui se passe ou dépanner un échec.

## Pourquoi 2 étapes de compilation

`generated/` (les classes `Bibliotheque.java`/`Livre.java`) **n'est
pas versionné dans git** — il est régénéré à chaque build à partir de
`src/main/resources/example.xsd`. Or `TestMain.java` importe
directement ces classes générées. Il faut donc :
1. compiler tout sauf `TestMain`,
2. générer les classes à partir du XSD,
3. compiler `TestMain` + les classes générées ensemble,
4. lancer `TestMain`.

## Commandes (juste javac + java, sans find)

Une seule commande `javac` par étape de compilation, grâce à
`-sourcepath` : on ne donne que le fichier "point d'entrée"
(celui qui a `main()`), `javac` va chercher lui-même les autres
fichiers dont il dépend (`XsdParser`, `JavaCodeGenerator`,
`AbstractXmlElement`...) dans les dossiers indiqués.

**Séparateur `-sourcepath` : `;` sous Windows, `:` sous Linux/macOS**
(défini par le JDK/l'OS, pas par le shell — donc identique en bash ou
en PowerShell sur une même machine). Exemples ci-dessous pour Windows.

```bash
# 1. javac : compiler (point d'entrée = GeneratorDemo, javac résout le reste)
javac -sourcepath src/main/java -d target/classes \
  src/main/java/com/ifall/xsdcodegen/generator/GeneratorDemo.java

# 2. java : exécuter -> génère Bibliotheque.java / Livre.java dans generated/
java -cp target/classes com.ifall.xsdcodegen.generator.GeneratorDemo

# 3. javac : compiler TestMain (dépend des classes générées à l'étape 2)
javac -sourcepath "src/main/java;generated" -d target/classes \
  src/main/java/com/ifall/xsdcodegen/test/TestMain.java

# 4. java : exécuter les tests
java -cp target/classes com.ifall.xsdcodegen.test.TestMain
```

Identique en PowerShell (juste remplacer le `\` de continuation de
ligne par `` ` ``).

## Sortie attendue

```
Classes generees dans : <chemin>\generated
Exemple impose : OK (output/bibliotheque.xml)
Exemple 3 livres : OK (output/bibliotheque_3_livres.xml)
Tous les tests sont passes.
```

Deux fichiers XML sont produits dans `output/` :
- `bibliotheque.xml` — l'exemple imposé du sujet (1 livre : toto/titi/tutu)
- `bibliotheque_3_livres.xml` — l'exemple à 3 `<livre>` du sujet

## Autres démos utiles (indépendantes de TestMain)

Chaque membre a une démo autonome pour valider sa propre partie sans
dépendre des autres :

```bash
# Valider uniquement le parsing (M2), sur example.xsd
java -cp target/classes com.ifall.xsdcodegen.parser.ParserDemo

# Valider uniquement la génération (M3), enchaîne parsing -> génération
java -cp target/classes com.ifall.xsdcodegen.generator.GeneratorDemo
```

## Nettoyer

`generated/`, `output/` et `target/` sont ignorés par git
(`.gitignore`) — on peut les supprimer sans risque pour repartir d'un
état propre :

```bash
rm -rf target generated output
```

## En cas d'erreur

- `package com.ifall.xsdcodegen.generated does not exist` en
  compilant `TestMain` → l'étape 2 (génération) n'a pas été lancée,
  ou `target/classes` a été supprimé entre les étapes.
- `NoClassDefFoundError` en lançant une commande `java -cp ...` →
  vérifier que le dossier `target/classes` contient bien les `.class`
  attendus (`find target/classes -name "*.class"`).

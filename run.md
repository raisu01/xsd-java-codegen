# Comment lancer le projet

Guide pratique, pas à pas, pour compiler et exécuter le pipeline
complet (parsing XSD → génération Java → sérialisation XML). Pour la
vue d'ensemble du projet, voir `README.md`.

## Prérequis

- JDK 11 ou supérieur (`java -version`, `javac -version`)
- Aucune dépendance externe : tout est fait avec les bibliothèques
  standard du JDK (`javax.xml.parsers`, `java.lang.reflect`)

## Pourquoi 2 étapes de compilation

`generated/` (les classes `Bibliotheque.java`/`Livre.java`) **n'est
pas versionné dans git** — il est régénéré à chaque build à partir de
`src/main/resources/example.xsd`. Or `TestMain.java` importe
directement ces classes générées. Il faut donc :
1. compiler tout sauf `TestMain`,
2. générer les classes à partir du XSD,
3. compiler `TestMain` + les classes générées ensemble,
4. lancer `TestMain`.

## Commandes (bash / Git Bash)

```bash
# 1. Compiler le parsing (M2), la génération (M3) et le runtime (M4)
find src/main/java/com/ifall/xsdcodegen/{model,parser,generator,runtime} -name "*.java" \
  | xargs javac -d target/classes

# 2. Générer Bibliotheque.java / Livre.java à partir de example.xsd
java -cp target/classes com.ifall.xsdcodegen.generator.GeneratorDemo

# 3. Compiler les classes générées + TestMain (M5)
find generated src/main/java/com/ifall/xsdcodegen/test -name "*.java" \
  | xargs javac -cp target/classes -d target/classes

# 4. Lancer les tests (reproduit l'exemple imposé du sujet)
java -cp target/classes com.ifall.xsdcodegen.test.TestMain
```

## Commandes (PowerShell)

```powershell
# 1. Compiler M2 + M3 + M4
Get-ChildItem -Recurse -Include *.java `
  src\main\java\com\ifall\xsdcodegen\model,
  src\main\java\com\ifall\xsdcodegen\parser,
  src\main\java\com\ifall\xsdcodegen\generator,
  src\main\java\com\ifall\xsdcodegen\runtime |
  ForEach-Object { $_.FullName } | Set-Content sources.txt
javac -d target/classes "@sources.txt"

# 2. Générer les classes
java -cp target/classes com.ifall.xsdcodegen.generator.GeneratorDemo

# 3. Compiler les classes générées + TestMain
Get-ChildItem -Recurse -Include *.java generated, src\main\java\com\ifall\xsdcodegen\test |
  ForEach-Object { $_.FullName } | Set-Content sources2.txt
javac -cp target/classes -d target/classes "@sources2.txt"

# 4. Lancer les tests
java -cp target/classes com.ifall.xsdcodegen.test.TestMain
```

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

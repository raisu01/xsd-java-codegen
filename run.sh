#!/usr/bin/env bash
# Compile et lance le pipeline complet en une seule commande.
# Voir run.md pour le détail étape par étape et le dépannage.
set -e
cd "$(dirname "$0")"

rm -rf target generated output

javac -sourcepath src/main/java -d target/classes \
  src/main/java/com/ifall/xsdcodegen/generator/GeneratorDemo.java

java -cp target/classes com.ifall.xsdcodegen.generator.GeneratorDemo

javac -sourcepath "src/main/java;generated" -d target/classes \
  src/main/java/com/ifall/xsdcodegen/test/TestMain.java

java -cp target/classes com.ifall.xsdcodegen.test.TestMain

#!/usr/bin/env bash
# Compile et lance le pipeline complet en une seule commande.
# Voir run.md pour le détail étape par étape et le dépannage.
set -e
cd "$(dirname "$0")"

rm -rf target generated output

find src/main/java/com/ifall/xsdcodegen/{model,parser,generator,runtime} -name "*.java" \
  | xargs javac -d target/classes

java -cp target/classes com.ifall.xsdcodegen.generator.GeneratorDemo

find generated src/main/java/com/ifall/xsdcodegen/test -name "*.java" \
  | xargs javac -cp target/classes -d target/classes

java -cp target/classes com.ifall.xsdcodegen.test.TestMain

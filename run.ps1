# Compile et lance le pipeline complet en une seule commande.
# Voir run.md pour le detail etape par etape et le depannage.
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Remove-Item -Recurse -Force target, generated, output -ErrorAction SilentlyContinue

javac -sourcepath src/main/java -d target/classes `
  src/main/java/com/ifall/xsdcodegen/generator/GeneratorDemo.java
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

java -cp target/classes com.ifall.xsdcodegen.generator.GeneratorDemo
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

javac -sourcepath "src/main/java;generated" -d target/classes `
  src/main/java/com/ifall/xsdcodegen/test/TestMain.java
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

java -cp target/classes com.ifall.xsdcodegen.test.TestMain
exit $LASTEXITCODE

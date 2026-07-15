# Compile et lance le pipeline complet en une seule commande.
# Voir run.md pour le detail etape par etape et le depannage.
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Remove-Item -Recurse -Force target, generated, output -ErrorAction SilentlyContinue

$sources1 = Get-ChildItem -Recurse -Include *.java `
    src\main\java\com\ifall\xsdcodegen\model,
    src\main\java\com\ifall\xsdcodegen\parser,
    src\main\java\com\ifall\xsdcodegen\generator,
    src\main\java\com\ifall\xsdcodegen\runtime |
  ForEach-Object { $_.FullName }
javac -d target/classes $sources1
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

java -cp target/classes com.ifall.xsdcodegen.generator.GeneratorDemo
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$sources2 = Get-ChildItem -Recurse -Include *.java generated, src\main\java\com\ifall\xsdcodegen\test |
  ForEach-Object { $_.FullName }
javac -cp target/classes -d target/classes $sources2
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

java -cp target/classes com.ifall.xsdcodegen.test.TestMain
exit $LASTEXITCODE

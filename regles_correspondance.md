# Règles de correspondance XSD ↔ Java

**Auteur :** Membre 1 — Analyse & coordination
**Projet :** Générateur de code Java à partir de schémas XML

---

## 1. Introduction

Ce document pose les fondations conceptuelles du projet : il définit comment chaque
concept d'un schéma XSD se traduit en un concept Java équivalent. Ces règles servent
de contrat entre le Membre 2 (parsing), le Membre 3 (génération) et le Membre 4
(sérialisation) : tout le monde s'appuie sur la même correspondance.

L'exemple de référence utilisé tout au long du projet est le schéma `bibliotheque.xsd`
(une bibliothèque contenant une liste de livres, chaque livre ayant un titre, un auteur
et un éditeur).

---

## 2. Concepts XSD recensés dans l'exemple fourni

En étudiant `bibliotheque.xsd`, on relève les concepts suivants (volontairement limités
aux besoins du projet — voir section 4) :

- `xs:schema` — la racine, définit le document
- `xs:element` top-level avec `xs:complexType` + `xs:sequence` — un élément "structuré"
  qui contient d'autres éléments dans un ordre donné
- `xs:element` top-level avec `type="xs:string"` — un élément "simple", une feuille
  qui contient juste du texte
- `ref="x"` — référence à un élément déjà déclaré ailleurs dans le schéma
- `maxOccurs="unbounded"` — l'élément référencé peut apparaître plusieurs fois
- Types simples XSD : `xs:string` (les seuls utilisés dans l'exemple, mais on prévoit
  `xs:int`, `xs:decimal`, `xs:boolean`, `xs:date` pour généraliser)

---

## 3. Tableau de correspondance XSD → Java

| Concept XSD | Concept Java | Exemple |
|---|---|---|
| `xs:schema` | package Java | `package modele;` |
| `xs:element` top-level + `complexType`/`sequence` | classe Java (nom = nom de l'élément, 1ère lettre en majuscule) | `bibliotheque` → `Bibliotheque`, `livre` → `Livre` |
| `xs:sequence` | ordre de déclaration des attributs dans la classe | l'ordre `titre, auteur, editeur` dans le XSD = l'ordre des champs Java |
| `xs:element ref="x"` (x = type simple) | attribut Java + getter/setter | `ref="titre"` → `private String titre;` + `getTitre()`/`setTitre()` |
| `xs:element ref="x" maxOccurs="unbounded"` (x = type complexe) | `List<X>` + getter uniquement (pas de setter, on ajoute via la liste) | `ref="livre" maxOccurs="unbounded"` → `List<Livre> livres` + `getLivres()` |
| `xs:element ref="x"` (x = type complexe, occurrence unique) | attribut de type `X` + getter/setter | (non présent dans l'exemple, mais prévu) |
| `xs:string` | `String` | `titre` → `String` |
| `xs:int` / `xs:integer` | `int` | — |
| `xs:decimal` | `double` | — |
| `xs:boolean` | `boolean` | — |
| `xs:date` | `java.time.LocalDate` | — |

**Correspondance nom de balise ↔ nom de champ :** on utilise le même nom (en minuscule)
des deux côtés — le champ Java `titre` correspond directement à la balise `<titre>`.
Ça évite d'avoir besoin d'annotations ou de fichier de mapping séparé : la
sérialisation par réflexion (Membre 4) peut retrouver le nom de balise directement
depuis le nom du champ.

---

## 4. Limites volontaires

Pour rester dans le périmètre du mini-projet (« n'allez pas dans les concepts trop
avancés d'XSD »), on ne gère **pas** :

- `xs:choice` (alternative entre plusieurs éléments)
- `xs:attribute` (attributs XML, ex. `<livre id="1">`)
- Les restrictions de types (`xs:restriction`, `xs:pattern`, `xs:enumeration`, etc.)
- Les namespaces XML avancés (préfixes multiples, `xmlns` personnalisés)
- L'héritage de types (`xs:extension`, `xs:complexContent`)
- `minOccurs` variable (on suppose 1 par défaut, sauf `maxOccurs="unbounded"` → liste)
- Les types complexes anonymes imbriqués (dans notre modèle, chaque type complexe est
  un élément top-level nommé, comme dans l'exemple fourni)

Ces limites sont assumées : elles couvrent 100% des besoins du XSD `bibliotheque.xsd`
fourni dans le sujet, et permettent de livrer un pipeline complet et fonctionnel plutôt
qu'un parseur XSD générique inachevé.

---

## 5. Statut du projet

Chaque membre implémente sa tâche sur sa propre branche (voir
`docs/Repartition_Taches_Projet_Eval.md`) ; ce document ne couvre que le livrable de
Membre 1 (les règles ci-dessus). État réel du pipeline à date de rédaction :

- **Membre 2 (parsing XSD)** : fait sur la branche `m2` — `XsdParser` construit une
  structure intermédiaire (`ElementDef`) conforme à la section 3, y compris pour des
  types complexes imbriqués sans `ref`
- **Membre 3 (génération des classes Java)** : pas encore implémenté
- **Membre 4 (sérialisation `save()`)** : pas encore implémenté — le sujet ne demande
  que `save()`, pas de `load()`
- **Membre 5 (tests bout en bout)** : dépend de M3 + M4, donc pas encore possible

---

## 6. Conclusion

Les règles définies dans ce document couvrent l'intégralité des concepts présents dans
le XSD de référence du sujet, tout en restant volontairement simples pour rester
réalisables dans le temps imparti. Elles ont été validées empiriquement côté parsing
(Membre 2, branche `m2`) ; la génération de classes et la sérialisation restent à
implémenter par les Membres 3 et 4 avant de pouvoir valider la chaîne complète.

Cette correspondance sert de référence commune à toute l'équipe : le Membre 2 doit
extraire une structure intermédiaire qui distingue clairement type simple vs type
complexe et détecte `maxOccurs="unbounded"` ; le Membre 3 doit produire des classes
respectant exactement les noms de champs et méthodes définis ici ; le Membre 4 doit
baser sa sérialisation par réflexion sur la correspondance nom-de-champ = nom-de-balise
établie en section 3.

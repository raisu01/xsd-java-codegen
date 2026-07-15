package com.ifall.xsdcodegen.test;

import com.ifall.xsdcodegen.generated.Bibliotheque;
import com.ifall.xsdcodegen.generated.Livre;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Valide le pipeline complet (Membre 2 -> Membre 3 -> Membre 4) en
 * utilisant uniquement les classes générées, comme le ferait un
 * utilisateur final du projet.
 */
public class TestMain {

    public static void main(String[] args) throws Exception {
        exempleImpose();
        exempleTroisLivres();
        System.out.println("Tous les tests sont passes.");
    }

    /**
     * Reproduit exactement le code imposé par le sujet
     * (docs/Mini_Projet_Eval.pdf, page 1) : un livre, save() sans argument.
     */
    private static void exempleImpose() throws Exception {
        Livre livre = new Livre();
        livre.setTitre("toto");
        livre.setAuteur("titi");
        livre.setEditeur("tutu");

        Bibliotheque biblio = new Bibliotheque();
        biblio.getLivres().add(livre);

        biblio.save(); // ecrit output/bibliotheque.xml (chemin par defaut)

        String produit = new String(Files.readAllBytes(Paths.get("output/bibliotheque.xml")));
        assertContains(produit, "<bibliotheque>");
        assertContains(produit, "<livre>");
        assertContains(produit, "<titre>toto</titre>");
        assertContains(produit, "<auteur>titi</auteur>");
        assertContains(produit, "<editeur>tutu</editeur>");

        System.out.println("Exemple impose : OK (output/bibliotheque.xml)");
    }

    /**
     * Reproduit l'exemple XML à 3 <livre> du sujet (page 2), pour valider
     * la gestion des listes (maxOccurs="unbounded").
     */
    private static void exempleTroisLivres() throws Exception {
        Bibliotheque biblio = new Bibliotheque();
        for (int i = 1; i <= 3; i++) {
            Livre livre = new Livre();
            livre.setTitre("titre " + i);
            livre.setAuteur("auteur " + i);
            livre.setEditeur("editeur " + i);
            biblio.getLivres().add(livre);
        }

        String path = "output/bibliotheque_3_livres.xml";
        biblio.save(path);

        String produit = new String(Files.readAllBytes(Paths.get(path)));
        for (int i = 1; i <= 3; i++) {
            assertContains(produit, "<titre>titre " + i + "</titre>");
            assertContains(produit, "<auteur>auteur " + i + "</auteur>");
            assertContains(produit, "<editeur>editeur " + i + "</editeur>");
        }

        long nbLivres = produit.lines().filter(l -> l.trim().equals("<livre>")).count();
        if (nbLivres != 3) {
            throw new AssertionError("Attendu 3 balises <livre>, trouve " + nbLivres);
        }

        System.out.println("Exemple 3 livres : OK (" + path + ")");
    }

    private static void assertContains(String haystack, String needle) {
        if (!haystack.contains(needle)) {
            throw new AssertionError("Le XML genere ne contient pas : " + needle);
        }
    }
}

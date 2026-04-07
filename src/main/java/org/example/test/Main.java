package org.example.test;

import org.example.entities.test;
import org.example.services.ServiceTest;
import org.example.utils.MyDatabase;
import org.example.entities.Category;
import org.example.entities.Product;
import org.example.services.CategoryService;
import org.example.services.ProductService;
import java.sql.SQLException;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        MyDatabase.getInstance();
        /*ServiceTest st = new ServiceTest();
        test t = new test("ghaith");

        try {
            // ajouter
            st.ajouter(t);
            System.out.println("Ajout termine");

            // get all
            List<test> listeAvant = st.getAll();
            System.out.println("Liste: " + listeAvant);

            if (!listeAvant.isEmpty()) {
                test premier = listeAvant.get(0);

                // modifier
                premier.setName("rajhi");
                st.modifier(premier);
                System.out.println("Modification terminee");
                System.out.println("Liste modifie: " + st.getAll());

                // supprimer
                st.supprimer(premier.getId());
                System.out.println("Suppression terminee");


                // get all apres suppression
                System.out.println("Liste finale: " + st.getAll());
            } else {
                System.out.println("Aucune donnee dans la table test.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }*/

        CategoryService cs = new CategoryService();
        ProductService ps = new ProductService();


        System.out.println("\n CATEGORIES");


        cs.addCategory(new Category("laptop"));


        cs.getAllCategories().forEach(System.out::println);


        cs.updateCategory(new Category(1, "keyboard updated"));


        System.out.println(cs.getCategoryById(1));

        System.out.println("\nPRODUITS ");


        ps.addProduct(new Product("Laptop Pro", "16GB RAM", 999.99, 10, "laptop.png", 1));


        ps.getAllProducts().forEach(System.out::println);


        ps.updateProduct(new Product(1, "Laptop Pro Max", "32GB RAM", 1299.99, 5, "laptop2.png", 1));


        System.out.println(ps.getProductById(1));


         ps.deleteProduct(4);
        // cs.deleteCategory(4);
    }
}
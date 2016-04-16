package webscrapper;

import webscrapper.models.Retail;
import webscrapper.models.Category;
import webscrapper.models.Item;
import webscrapper.models.Department;
import webscrapper.models.SubCategory;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebScrapper {
    static List<String> UrlException = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println(new Date().toString());
        Retail sodimac = new Retail("Sodimac", "http://www.sodimac.cl/sodimac-cl/");
        processCategories(sodimac);
    }

    public static void processCategories(Retail retail) {
        Department department;
        Category category;
        SubCategory subCategory;
        String departmentUrl;
        List<Item> items = new ArrayList<>();
        Elements subCategories = new Elements();
        Elements categoryExtract = new Elements();
        Elements categories = new Elements();
        Elements departmentExtract = new Elements();
        Elements departments = new Elements();
        Document documentDepartment;
        Document documentCategory;
        
        Document documentRetail = getHtmlDocument(retail.getUrl());
        if (!(documentRetail == null && documentRetail.getElementsByClass("sub-nav").isEmpty())){
            
            departmentExtract = documentRetail.getElementsByClass("sub-nav");
            departments = departmentExtract.get(0).children();

            int z = 0;
            for (Element elemDepartment : departments) {
                z++; //Trabajo con la 2da opcion del menu
                if (z>1){
                    department = new Department(elemDepartment.select("li > a").first().text(), elemDepartment.child(0).attr("href").substring(12), retail);
                    departmentUrl = department.getRetail().getUrl() + department.getUrl();
                    documentDepartment = getHtmlDocument(departmentUrl);
                    if (!(documentDepartment == null && documentDepartment.getElementsByClass("jq-accordionGroup").isEmpty())){
                        categoryExtract = documentDepartment.getElementsByClass("jq-accordionGroup");
                        categories = categoryExtract.get(0).children();
                        for (Element elemCategory : categories){
                            try{ 
                                Thread.sleep(20000); 
                            }catch(InterruptedException e ){ 
                            System.out.println("Thread Interrupted") ;
                            }
                            category = new Category(elemCategory.select("li > a").first().text(), elemCategory.child(0).attr("href").substring(12), department);
                            System.out.println("Categories: " + category.getName() + " URL: "+ category.getDepartmentParent().getRetail().getUrl()+category.getUrl());
                            documentCategory = getHtmlDocument(category.getDepartmentParent().getRetail().getUrl() + category.getUrl());
                            if (documentCategory != null){
                                Elements subCategoryExtract = documentCategory.getElementsByClass("jq-accordionGroup");
                                
                                if (subCategoryExtract.isEmpty()){
                                   /* Cuando una Categoria no tiene subcategoria pero 
                                      tiene productos para mostrar. Ejemplo: imperdible
                                    */
                                }else{
                                    subCategories = subCategoryExtract.get(0).children();
                                    for (Element elemSubCategory : subCategories){
                                        subCategory = new SubCategory(elemSubCategory.select("li > a").first().text(), elemSubCategory.child(0).attr("href").substring(12) , category);
                                        System.out.println("---- SubCategory: " + subCategory.getName() + " URL: "+ subCategory.getCategory().getDepartmentParent().getRetail().getUrl()+subCategory.getUrl());
                                        items.addAll(processItems(subCategory));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("TOTAL DE PRODUCTOS " + items.size());
            System.out.println(new Date().toString());
            print(items);
        }

    }

    public static List<Item> processItems(SubCategory subCategory) {
        List<Item> items = new ArrayList<>();
        Elements catalog;
        Elements catalogDetails;
        Document documentSubCategory;
        Document documentPages;
        int pages;
        int pagesNo;
        Item item;
        String itemName;
        String itemHref;
        
        String url = subCategory.getCategory().getDepartmentParent().getRetail().getUrl() + subCategory.getUrl();
        documentSubCategory = getHtmlDocument(url);
       
        if (!(documentSubCategory == null)) {
            /* Toda parrilla de productos tiene el elemento "pagination" asi
                solo tenga una pagina.
            */
            if (!documentSubCategory.getElementsByClass("pagination").isEmpty()){
                pages = (documentSubCategory.getElementsByClass("pagination").get(0).children().size()) - 1;
                System.out.println("pages: "+ pages);
                /* Ciclo para validar el total de paginas en paginacion
                    queda pendiente validar en caso de que sean mas de 3 paginas
                    como se adapta el ciclo. 
                */
                for (int x = 0; x <= pages; x++) {
                    pagesNo = x*16;
                    documentPages = getHtmlDocument(url + "?No=" + pagesNo);
                    if (documentPages != null) {
                        catalog = documentPages.getElementsByClass("item-list");
                        if (!catalog.isEmpty()){
                            catalogDetails = catalog.get(0).getElementsByClass("informationContainer");
                            if (!catalogDetails.isEmpty()){
                                for (Element elemt : catalogDetails) { 
                                    itemName = elemt.getElementsByClass("name").get(0).text();
                                    itemHref = elemt.select("p > a").get(0).attr("href").substring(12);
                                    item = new Item(itemName, itemHref, subCategory);
                                    /* Si hay un solo precio se llama Precio Normal
                                       Pero si hay dos precios:
                                        El 1ero se llama "Normal:"
                                        El 2do es precio Internet y es menor*/
                                    if (elemt.getElementsByClass("jq-price").get(0).text().isEmpty()){
                                        item.setNormalPrice(elemt.getElementsByClass("jq-price").get(0).text());
                                    }else{
                                        item.setNormalPrice(elemt.getElementsByClass("normal-price").get(0).text());
                                        item.setInternetPrice(elemt.getElementsByClass("jq-price").get(0).text());
                                    }
                                    if (elemt.getElementsByClass("cmr-icon").isEmpty()){
                                        item.setDiscount("false");
                                    }else{
                                        item.setDiscount("true");
                                    }
    //                                item.setCardPrice("catalog-product-card-price".equals(label.className()) ? label.text() : item.getCardPrice());
                                    items.add(item);
                                }
                            }
                        }
                    }
                }
            }
        }
        return items;
    }
    

    public static Document getHtmlDocument(String url) {

        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(300000).get();
        } catch (IOException ex) {
            UrlException.add(url);
            System.out.println("IO Excepción al obtener el HTML de la página " + url + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex) {
            UrlException.add(url);
            System.out.println("Excepción al obtener el HTML de la página " + url + ex.getMessage());
            ex.printStackTrace();
        }
        return doc;
    }

    public static void print(List<Item> items) {
        FileWriter writer;
        String fileName = "/Users/edwinmperazaduran/Documents/Test/sodimac"+(new Date())+".txt";
        
        try {
            writer = new FileWriter(fileName);
            for (Item item : items) {
                writer.write(item.getSubCategory().getCategory().getDepartmentParent().getName() + " | " + item.getSubCategory().getCategory().getName() + " | " + item.getSubCategory().getName() + " | " + item.getName() + " | " + item.getNormalPrice() + " | " + item.getInternetPrice() + " | " + item.getCardPrice() + " | " + item.getDiscount());
                writer.append("\n");
            }
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(WebScrapper.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
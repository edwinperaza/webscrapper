package com.venemprendiendo.pricebot.webscrappers;

import com.venemprendiendo.pricebot.exceptions.IncompleteConfigurationException;
import com.venemprendiendo.pricebot.models.Category;
import com.venemprendiendo.pricebot.models.Department;
import com.venemprendiendo.pricebot.models.Item;
import com.venemprendiendo.pricebot.models.Retail;
import com.venemprendiendo.pricebot.models.Scrapper;
import com.venemprendiendo.pricebot.models.SubCategory;
import com.venemprendiendo.pricebot.utils.Utils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebScrapperFalabella extends Scrapper {

    
    public WebScrapperFalabella(int timeout, String destinationPath) {
        setTimeout(timeout);
        setDestinationPath(destinationPath);
        setRetail(new Retail("Falabella", "http://www.falabella.com/falabella-cl/"));
    }

    @Override
    public void executeScrapper() throws IncompleteConfigurationException{
        Document document = getHtmlDocumentWithRetry(retail.getUrl());
        Document documentCategories;
        Elements departmentExtract = document.getElementsByClass("menu_bg_off");
        Elements departments = departmentExtract.select("td");
        List<Category> categories;
        List<SubCategory> subCategories;
        Department department;

        List<Item> items = new ArrayList<>();
        for (Element elem : departments) {
            department = new Department(elem.select("a").get(0).text(), elem.select("a").attr("href"), retail);
            System.out.println("-------------DEPARTAMENTOS-------------");
            System.out.println(department.getName());
            documentCategories = getHtmlDocumentWithRetry("http://www.falabella.com" + department.getUrl());

            categories = getCategoriesAdvanced(documentCategories, department);
            System.out.println("-------------CATEGORIAS-------------");
            System.out.println("              " + categories.size());
            for (Category category : categories) {
                System.out.println(category.getName());
                Document categorytExtract = getHtmlDocumentWithRetry("http://www.falabella.com" + category.getUrl());
                //subCategories = getSubCategories(categorytExtract.getElementsByClass("facetaCat").select("ul > li > a"), category);
                
                subCategories = getSubCategoriesAdvanced(categorytExtract, category);
                System.out.println("-------------SUBCATEGORIAS-------------");
                System.out.println("              " + subCategories.size());
                for (SubCategory subCategory : subCategories) {
                    System.out.println(subCategory.getName());
                    System.out.println(subCategory.getUrl());
                    items.addAll(processItems(subCategory));
                    //break;
                }

                //break;
            }
            //break;
        }
        System.out.println("TOTAL DE PRODUCTOS " + items.size());
        System.out.println("TOTAL DE URLs con error: " + wrongUrls.size());
        for (String wrong : wrongUrls) {
            System.out.println(wrong);
        }
        System.out.println(new Date().toString());
        Utils.print(items, getDestinationPath());

    }

    @Override
    public List<Item> processItems(SubCategory subCategory) {
        List<Item> items = new ArrayList<>();
        Item item;
        String url = "http://www.falabella.com" + subCategory.getUrl();

        Document document, doc;
        document = getHtmlDocumentWithRetry(url);

        if (document != null) {
            try {

                Element products = document.getElementById("verProductos");
                String product = products.text();
                String number = product.substring(product.indexOf("de") + 3, product.indexOf("productos")).trim();
                doc = getHtmlDocumentWithRetry(url.substring(0, url.indexOf("navAction=push")) + "No=0&Nrpp=" + number);

                Elements catalogItems = doc.getElementsByClass("cajaLP4x");
                for (Element elemt : catalogItems) {

                    try {
                        item = new Item(elemt.getElementsByClass("detalle").first().select("a").attr("title"), elemt.getElementsByClass("precio3").select("span").text(), subCategory);
                        item.setBrand(elemt.getElementsByClass("marca").first().select("a").attr("title"));
                        item.setUrl(elemt.getElementsByClass("marca").first().select("a").attr("href"));
                        item.setUrlImage(elemt.getElementsByClass("lazy").first().attr("data-original"));
                        if (!elemt.getElementsByClass("precio3").select("span").isEmpty()) {
                            item.setNormalPrice(elemt.getElementsByClass("precio3").select("span").text().replaceAll("\\D+", ""));
                            item.setCardPrice(elemt.getElementsByClass("precio1").select("span").text().replaceAll("\\D+", ""));
                            item.setInternetPrice(elemt.getElementsByClass("precio2").select("span").text().replaceAll("\\D+", ""));
                        } else if (!elemt.getElementsByClass("precio2").select("span").isEmpty()) {
                            item.setInternetPrice(elemt.getElementsByClass("precio1").select("span").text().replaceAll("\\D+", ""));
                            item.setNormalPrice(elemt.getElementsByClass("precio2").select("span").text().replaceAll("\\D+", ""));
                        } else {
                            item.setNormalPrice(elemt.getElementsByClass("precio1").select("span").text().replaceAll("\\D+", ""));
                        }
                        item.setIsExclusive(item.getCardPrice() != null ? "TRUE" : "FALSE");
                        item.setDate(Utils.getFormatedDate(new Date(),"yyyy-MM-dd:mm:ss"));
                        items.add(item);

                    } catch (NullPointerException ex) {
                        wrongUrls.add(url);
                        System.out.println(elemt.toString());
                    }

                }

            } catch (NullPointerException npe) {
                wrongUrls.add(url);

            }
        }
        return items;
    }

    

    private List<Category> getCategoriesAdvanced(Document document, Department department) {
        List<Category> categories = new ArrayList<>();
        Category category;

        if (document!= null && !document.getElementsByClass("subCategorias").isEmpty()) {

            for (Element elem1 : document.getElementsByClass("subCategorias").select("li > a")) {
                category = new Category(elem1.text(), elem1.attr("href"), department);
                categories.add(category);

            }

        } else if (document!= null && !document.getElementsByClass("estadoFaceta2").isEmpty()){
            for (Element elem1 : document.getElementsByClass("estadoFaceta2").select("a")) {
                category = new Category(elem1.text(), elem1.attr("href"), department);
                categories.add(category);
            }
        }

        return categories;
    }

    private List<SubCategory> getSubCategoriesAdvanced(Document document, Category category) {
        List<SubCategory> subCategories = new ArrayList<>();
        SubCategory subCategory;

        if (document!= null && !document.getElementsByClass("midLinea").isEmpty()) {

            for (Element elem1 : document.getElementsByClass("menuVerticalInterior").select("ul > li > a")) {
                subCategory = new SubCategory(elem1.text(), elem1.attr("href"), category);
                subCategories.add(subCategory);

            }
        } else if (document!= null && !document.getElementsByClass("subCategorias").isEmpty()) {

            for (Element elem1 : document.getElementsByClass("subCategorias").select("li > a")) {
                subCategory = new SubCategory(elem1.text(), elem1.attr("href"), category);
                subCategories.add(subCategory);

            }
        } else {
            wrongUrls.add(category.getName() + " " + category.getUrl());
            System.out.println("ALGO RARO");
        }

        return subCategories;
    }
}

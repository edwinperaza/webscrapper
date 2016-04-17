/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.venemprendiendo.pricebot.webscrappers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.venemprendiendo.pricebot.models.Category;
import com.venemprendiendo.pricebot.models.Department;
import com.venemprendiendo.pricebot.models.Item;
import com.venemprendiendo.pricebot.models.Retail;
import com.venemprendiendo.pricebot.models.SubCategory;
import com.venemprendiendo.pricebot.utils.Utils;


/**
 *
 * @author edwinmperazaduran
 */
public class WebScrapperSodimac {
    static List<String> urlException = new ArrayList<>();
    
    public static void executeScrapper(Retail retail) {
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

            for (Element elemDepartment : departments) {
                    department = new Department(elemDepartment.select("li > a").first().text(), elemDepartment.child(0).attr("href").substring(12), retail);
                    departmentUrl = department.getRetail().getUrl() + department.getUrl();
                    documentDepartment = getHtmlDocument(departmentUrl);
                    if (!(documentDepartment == null && documentDepartment.getElementsByClass("jq-accordionGroup").isEmpty())){
                        categoryExtract = documentDepartment.getElementsByClass("jq-accordionGroup");
                        categories = categoryExtract.get(0).children();
                        for (Element elemCategory : categories){
                            try{ 
                                Thread.sleep(10000); 
                            }catch(InterruptedException e ){ 
                            System.out.println("Thread Interrupted") ;
                            }
                            category = new Category(elemCategory.select("li > a").first().text(), elemCategory.child(0).attr("href").substring(12), department);
                            System.out.println("Categories: " + category.getName() + " URL: "+ category.getDepartment().getRetail().getUrl()+category.getUrl());
                            documentCategory = getHtmlDocument(category.getDepartment().getRetail().getUrl() + category.getUrl());
                            if (documentCategory != null){
                                Elements subCategoryExtract = documentCategory.getElementsByClass("jq-accordionGroup");
                                
                                if (subCategoryExtract.isEmpty()){
//                                   items.addAll(processItemsCategory(subCategory));
                                }else{
                                    subCategories = subCategoryExtract.get(0).children();
                                    for (Element elemSubCategory : subCategories){
                                        subCategory = new SubCategory(elemSubCategory.select("li > a").first().text(), elemSubCategory.child(0).attr("href").substring(12) , category);
                                        System.out.println("---- SubCategory: " + subCategory.getName() + " URL: "+ subCategory.getCategory().getDepartment().getRetail().getUrl()+subCategory.getUrl());
                                        items.addAll(processItems(subCategory));
//                                        break;
                                    }
                                }
                            }
//                            break;
                        }
//                        break;
                    }
                
            }
            System.out.println("TOTAL DE PRODUCTOS " + items.size());
            System.out.println(new Date().toString());
            Utils.print(items);
        }

    }

    private static List<Item> processItems(SubCategory subCategory) {
        List<Item> items = new ArrayList<>();
        Elements catalog;
        Elements catalogDetails;
        Elements imagesElements;
        Document documentSubCategory;
        Document documentPages;
        int pages;
        int pagesNo;
        Item item;
        String itemName;
        String itemHref;
        String urlImage = "";
        
        String url = subCategory.getCategory().getDepartment().getRetail().getUrl() + subCategory.getUrl();
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
                            imagesElements = catalog.get(0).getElementsByClass("imageBoxContainer");
                            if (!imagesElements.isEmpty()){
                                for (Element image : imagesElements){
                                    urlImage = image.select("img").attr("data-original");
                                }
                            }
                            
                            if (!catalogDetails.isEmpty()){
                                for (Element detail : catalogDetails) { 
                                    itemName = detail.getElementsByClass("name").get(0).text();
                                    itemHref = detail.select("p > a").get(0).attr("href").substring(12);
                                    item = new Item(itemName, itemHref, subCategory);
                                    if (detail.getElementsByClass("normal-price").get(0).text().length() == 1){
                                        item.setNormalPrice(detail.getElementsByClass("jq-price").get(0).text().split("[$CJ]")[1]);
                                    }else{
                                        item.setNormalPrice(detail.getElementsByClass("normal-price").get(0).text().split("[$CJ]")[1]);
                                        item.setInternetPrice(detail.getElementsByClass("jq-price").get(0).text().split("[$CJ]")[1]);
                                    }
                                    if (detail.getElementsByClass("cmr-icon").isEmpty()){
                                        item.setDiscount("false");
                                    }else{
                                        item.setDiscount("true");
                                    }
                                    item.setUrlImage(!urlImage.isEmpty() ? urlImage : "" );
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
    

    private static Document getHtmlDocument(String url) {

        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(300000).get();
        } catch (IOException ex) {
            urlException.add(url);
            System.out.println("IO Excepción al obtener el HTML de la página " + url + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex) {
            urlException.add(url);
            System.out.println("Excepción al obtener el HTML de la página " + url + ex.getMessage());
            ex.printStackTrace();
        }
        return doc;
    }

    
}

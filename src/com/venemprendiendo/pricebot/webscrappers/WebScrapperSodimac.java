package com.venemprendiendo.pricebot.webscrappers;

import com.venemprendiendo.pricebot.exceptions.IncompleteConfigurationException;
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
import com.venemprendiendo.pricebot.models.Scrapper;
import com.venemprendiendo.pricebot.models.SubCategory;
import com.venemprendiendo.pricebot.utils.Utils;

/**
 *
 * @author edwinmperazaduran
 */
public class WebScrapperSodimac extends Scrapper {
    
    public WebScrapperSodimac(int timeout, String destinationPath) {
        setTimeout(timeout);
        setDestinationPath(destinationPath);
        setRetail(new Retail("Sodimac", "http://www.sodimac.cl/sodimac-cl/"));
    }
    
    @Override
    public void executeScrapper() throws IncompleteConfigurationException{
        Department department;
        Category category;
        SubCategory subCategory;
        String departmentUrl;
        List<Item> items = new ArrayList<>();
        Elements subCategories;
        Elements categoryExtract;
        Elements categories;
        Elements departmentExtract;
        Elements departments;
        Document documentDepartment;
        Document documentCategory;
        
        Document documentRetail = getHtmlDocumentWithRetry(retail.getUrl());
        if (!(documentRetail == null && documentRetail.getElementsByClass("sub-nav").isEmpty())){
            departmentExtract = documentRetail.getElementsByClass("sub-nav");
            departments = departmentExtract.get(0).children();
            int p =0;
            for (Element elemDepartment : departments) {
                    department = new Department(elemDepartment.select("li > a").first().text(), elemDepartment.child(0).attr("href").substring(12), retail);
                    departmentUrl = department.getRetail().getUrl() + department.getUrl();
                    documentDepartment = getHtmlDocumentWithRetry(departmentUrl);
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
                            documentCategory = getHtmlDocumentWithRetry(category.getDepartment().getRetail().getUrl() + category.getUrl());
                            if (documentCategory != null){
                                Elements subCategoryExtract = documentCategory.getElementsByClass("jq-accordionGroup");
                                if (subCategoryExtract != null && subCategoryExtract.isEmpty()){
//                                   items.addAll(processItemsCategory(subCategory));
                                }else if (subCategoryExtract != null) {
                                        subCategories = subCategoryExtract.get(0).children();
                                        for (Element elemSubCategory : subCategories){
                                            subCategory = new SubCategory(elemSubCategory.select("li > a").first().text(), elemSubCategory.child(0).attr("href").substring(12) , category);
                                            System.out.println("---- SubCategory: " + subCategory.getName() + " URL: "+ subCategory.getCategory().getDepartment().getRetail().getUrl()+subCategory.getUrl());
                                            items.addAll(processItems(subCategory));
                                        }
//                                        break;
                                }
                            }
//                            break;
                        }
                    }
//                  break;  
            }
            System.out.println("TOTAL DE PRODUCTOS " + items.size());
            System.out.println(new Date().toString());
            Utils.print(items, getDestinationPath());
        }
    }
    
    @Override
    public List<Item> processItems(SubCategory subCategory) {
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
        documentSubCategory = getHtmlDocumentWithRetry(url);
       
        if (!(documentSubCategory == null)) {
            if (!documentSubCategory.getElementsByClass("pagination").isEmpty()){
                pages = (documentSubCategory.getElementsByClass("pagination").get(0).children().size()) - 1;
                System.out.println("pages: "+ pages);
                for (int x = 0; x <= pages; x++) {
                    pagesNo = x*16;
                    documentPages = getHtmlDocumentWithRetry(url + "?No=" + pagesNo);
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
}
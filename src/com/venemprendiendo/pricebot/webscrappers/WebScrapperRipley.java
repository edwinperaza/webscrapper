package com.venemprendiendo.pricebot.webscrappers;

import com.venemprendiendo.pricebot.models.Category;
import com.venemprendiendo.pricebot.models.Department;
import com.venemprendiendo.pricebot.models.Item;
import com.venemprendiendo.pricebot.models.Retail;
import com.venemprendiendo.pricebot.models.SubCategory;
import com.venemprendiendo.pricebot.utils.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebScrapperRipley {

    static int intent = 0;
    static List<String> wrongUrls = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println(new Date().toString());
        Retail ripley = new Retail("Ripley", "http://simple.ripley.cl");
        processCategories(ripley);

    }

    public static void processCategories(Retail retail) {
        Document document = getHtmlDocumentWithRetry(retail.getUrl());
        Elements departmentExtract = document.getElementsByClass("main-categories");
        Elements departments = departmentExtract.get(0).children();
        System.out.println("Departments: ");
        Department department;
        Category category;
        SubCategory subCategory;
        List<Item> items = new ArrayList<>();
        for (Element elem : departments) {
            department = new Department(elem.select("li > a").first().text(), null, retail);
            Element childCategories = elem.select("ul[class*=child-categories]").get(0);
            System.out.println("    Categories: ");
            for (int x = 0; x < childCategories.childNodeSize(); x++) {

                Document doc = Jsoup.parse(childCategories.childNode(x).outerHtml());
                Element cat = doc.body().select("body > li").first();
                category = new Category(cat.child(0).text(), cat.child(0).attr("href"), department);
                System.out.println(category.getName() + " " + category.getUrl());
                System.out.println("        SubCategories ");

                for (Element sub : cat.select("ul > li").select("li > a")) {
                    subCategory = new SubCategory(sub.text(), sub.attr("href"), category);
                    System.out.println(subCategory.getName() + "  " + subCategory.getUrl());
                    items.addAll(processItems(subCategory));
                    break;
                }
                break;
            }
            break;//Solo trdabajare con TV y Audio por ahora.
        }
        System.out.println("TOTAL DE PRODUCTOS " + items.size());
        System.out.println("TOTAL DE URLs con error: " + wrongUrls.size());
        for (String wrong : wrongUrls) {
            System.out.println(wrong);
        }
        System.out.println(new Date().toString());
        Utils.print(items);

    }

    public static List<Item> processItems(SubCategory subCategory) {
        List<Item> items = new ArrayList<>();
        String url = subCategory.getCategory().getDepartment().getRetail().getUrl() + subCategory.getUrl();
        Document document;
        document = getHtmlDocumentWithRetry(url);

        if (document != null) {
            Elements catalog;
            int pages = (document.getElementsByClass("pagination").get(0).children().size()) - 2;
            Item item;
            System.out.println(pages);
            for (int x = 1; x <= pages; x++) {
                document = getHtmlDocumentWithRetry(url + "?page=" + x);

                if (document != null) {
                    catalog = document.getElementsByClass("catalog-container");

                    for (Element elemt : catalog.get(0).children()) {
                        item = new Item(elemt.text(), elemt.attr("href"), subCategory);

                        for (Element label : elemt.getAllElements()) {
                            item.setName("js-clamp catalog-product-name".equals(label.className()) ? label.text() : item.getName());
                            item.setNormalPrice("catalog-product-list-price".equals(label.className()) ? label.text() : item.getNormalPrice());
                            item.setInternetPrice("catalog-product-offer-price".equals(label.className()) ? label.text() : item.getInternetPrice());
                            item.setCardPrice("catalog-product-card-price".equals(label.className()) ? label.text() : item.getCardPrice());
                            item.setDiscount("catalog-discount-tag".equals(label.className()) ? label.text() : item.getDiscount());
                        }
                        items.add(item);
                    }
                }
            }
        }

        return items;
    }

    private static Document getHtmlDocumentWithRetry(String url) {
        Document doc = null;
        if (intent < 3) {
            try {
                doc = getHtmlDocument(url);
                intent = 0;
            } catch (IOException ex) {
                intent++;
                System.out.println("Excepción al obtener el HTML de la página " + url + ex.getMessage());
                System.out.println("Reintento: " + intent);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex1) {
                    Thread.currentThread().interrupt();
                }
                getHtmlDocumentWithRetry(url);
            }
            return doc;
        } else {
            System.out.println("culminaron los reintentos. Total: " + intent);
            wrongUrls.add(url);
            return null;
        }
    }

    public static Document getHtmlDocument(String url) throws IOException {

        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(300000).get();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            System.out.println("Excepción GENERAL al obtener el HTML de la página " + url + ex.getMessage());
            ex.printStackTrace();
        }
        return doc;
    }

}

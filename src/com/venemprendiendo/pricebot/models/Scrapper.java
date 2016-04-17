package com.venemprendiendo.pricebot.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class Scrapper {
    private int timeout = 10000;
    public int intent = 0;
    public List<String> wrongUrls = new ArrayList<>();
    public abstract void executeScrapper(Retail retail);
    public abstract List<Item> processItems(SubCategory subCategory);
    public Document getHtmlDocumentWithRetry(String url) {
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
    
    public Document getHtmlDocument(String url) throws IOException {

        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(timeout).get();
        } catch (IOException ex) {
            System.out.println("Excepción GENERAL al obtener el HTML de la página " + url + " "+ ex.getMessage());
            throw new IOException();
        } catch (Exception ex) {
            System.out.println("Excepción GENERAL al obtener el HTML de la página " + url + " "+ ex.getMessage());
        }
        return doc;
    }

    public final void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    
}

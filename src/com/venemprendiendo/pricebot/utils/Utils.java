/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.venemprendiendo.pricebot.utils;

import com.venemprendiendo.pricebot.exceptions.IncompleteConfigurationException;
import com.venemprendiendo.pricebot.models.Item;
import com.venemprendiendo.pricebot.webscrappers.WebScrapper;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Utils {
    
    public static void print(List<Item> items, String destinationPath) throws IncompleteConfigurationException {
        if(destinationPath == null) throw new IncompleteConfigurationException(" El parametro destinationPath no puede ser nulo.");
        if (items != null && !items.isEmpty()){
            FileWriter writer;
            String retailName = items.get(0).getSubCategory().getCategory().getDepartment().getRetail().getName();
            String fileName = destinationPath +retailName+"_"+getFormatedDate(new Date(), "dd_MM_yyy_HH_mm")+".txt";
            try {
                writer = new FileWriter(fileName);
                writer.write("site  | department  | department-url  |  category  |  category-url  |  subcategory  |  subcategory-url  |  item-name  |  item-url  |  SKU  |  brand  |  price-normal  |  price-sale  |  price-internet  |  image-url  |  date-time  |  isExclusive");
                writer.append("\n");
                for (Item item : items) {
                    writer.write(
                            item.getSubCategory().getCategory().getDepartment().getRetail().getName() + " | "+
                            item.getSubCategory().getCategory().getDepartment().getName() + " | " + 
                            item.getSubCategory().getCategory().getDepartment().getUrl() + " | " + 
                            item.getSubCategory().getCategory().getName() + " | " + 
                            item.getSubCategory().getCategory().getUrl() + " | " + 
                            item.getSubCategory().getName() + " | " + 
                            item.getSubCategory().getUrl() + " | " + 
                            item.getName() + " | " + 
                            item.getUrl() + " | " + 
                            item.getSKU() + " | " + 
                            item.getBrand() + " | " + 
                            item.getNormalPrice() + " | " + 
                            item.getCardPrice() + " | " + 
                            item.getInternetPrice() + " | " + 
                            item.getUrlImage() + " | " + 
                            item.getDate() + " | " + 
                            item.getIsExclusive());
                    writer.append("\n");
                }
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(WebScrapper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static String getFormatedDate(Date date, String format){
        date = date == null ? new Date(): date;
        format = format == null ? "dd-mm-yyyy" : format;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(date);
    }
    
}

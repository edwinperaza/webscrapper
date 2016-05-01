package com.venemprendiendo.pricebot.webscrappers;

import com.venemprendiendo.pricebot.exceptions.IncompleteConfigurationException;
import com.venemprendiendo.pricebot.exceptions.NullParameterException;
import com.venemprendiendo.pricebot.models.Scrapper;
import java.util.Date;

public class WebScrapper {
    
    private static Scrapper getScrapper(String retail, int timeout, String destinationPath) throws NullParameterException{
        if(retail == null)
            throw new NullParameterException("El parametro retail no puede ser nulo.");
        switch(retail.toLowerCase()){
            case "ripley": 
                return new WebScrapperRipley(timeout,destinationPath);
            case "falabella": 
                return new WebScrapperFalabella(timeout,destinationPath);
            case "sodimac": 
                return new WebScrapperSodimac(timeout,destinationPath);
            case "paris": 
                return null;
            default :
                return null;
        }
        
    }
    public static void executeExtraction(String retail, int timeout, String destinationPath) throws NullParameterException, IncompleteConfigurationException{
        System.out.println(new Date().toString());
        getScrapper(retail, timeout, destinationPath).executeScrapper();
    }
    
    public static void main(String[] args) {
        try{
            executeExtraction("Sodimac",30000,"/Users/edwinmperazaduran/Documents/Test/");
        }catch(Exception ex){
        ex.printStackTrace();
        }
    }
}

package com.venemprendiendo.pricebot.webscrappers;

import com.venemprendiendo.pricebot.models.Retail;
import java.util.Date;

public class WebScrapper {
    
    
    public static void main(String[] args) {
        System.out.println(new Date().toString());
        Retail sodimac = new Retail("Sodimac", "http://www.sodimac.cl/sodimac-cl/");
        WebScrapperSodimac.executeScrapper(sodimac);
    }  
}
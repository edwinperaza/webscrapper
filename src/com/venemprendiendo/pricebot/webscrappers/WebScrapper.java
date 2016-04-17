package com.venemprendiendo.pricebot.webscrappers;

import com.venemprendiendo.pricebot.models.Retail;
import java.util.Date;

public class WebScrapper {

    public static void main(String[] args) {
        System.out.println(new Date().toString());
//        Retail sodimac = new Retail("Sodimac", "http://www.sodimac.cl/sodimac-cl/");
        //      WebScrapperSodimac.executeScrapper(sodimac);

//        
//        Retail ripley = new Retail("Ripley", "http://simple.ripley.cl");
//        new WebScrapperRipley().executeScrapper(ripley);
        Retail falabella = new Retail("Falabella", "http://www.falabella.com/falabella-cl/");
        new WebScrapperFalabella().executeScrapper(falabella);
    }
}

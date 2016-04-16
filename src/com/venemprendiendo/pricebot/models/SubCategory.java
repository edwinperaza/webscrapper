/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.venemprendiendo.pricebot.models;

public class SubCategory extends RetailEntityModel{

    private Category category;

    public SubCategory(String name, String url, Category category) {
        super(name, url);
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
    
}

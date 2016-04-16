/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.venemprendiendo.pricebot.models;

public class Department extends RetailEntityModel{
    private Retail retail;

    public Department(String name, String url,Retail retail) {
        super(name, url);
        this.retail = retail;
    }
    
    public Retail getRetail() {
        return retail;
    }

    public void setRetail(Retail retail) {
        this.retail = retail;
    }
    
}

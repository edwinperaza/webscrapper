/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.venemprendiendo.pricebot.models;

public class Category extends RetailEntityModel {

    private Department category;

    public Category(String name, String url, Department department) {
        super(name, url);
        this.category = department;
    }

    public Department getDepartment() {
        return category;
    }

    public void setDepartment(Department categoryParent) {
        this.category = categoryParent;
    }

}

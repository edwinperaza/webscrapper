/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.venemprendiendo.pricebot.models;

/**
 *
 * @author leoromerbric
 */
public class Item extends RetailEntityModel {

    private SubCategory subCategory;
    private String normalPrice;
    private String internetPrice;
    private String cardPrice;
    private String discount;

    public Item(String name, String url, SubCategory subCategory) {
        super(name, url);
        this.subCategory = subCategory;
    }

    public SubCategory getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(SubCategory subCategory) {
        this.subCategory = subCategory;
    }

    public String getNormalPrice() {
        return normalPrice;
    }

    public void setNormalPrice(String normalPrice) {
        this.normalPrice = normalPrice;
    }

    public String getInternetPrice() {
        return internetPrice;
    }

    public void setInternetPrice(String internetPrice) {
        this.internetPrice = internetPrice;
    }

    public String getCardPrice() {
        return cardPrice;
    }

    public void setCardPrice(String cardPrice) {
        this.cardPrice = cardPrice;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

}

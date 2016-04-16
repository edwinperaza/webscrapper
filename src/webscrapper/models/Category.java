/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webscrapper.models;

public class Category extends RetailEntityModel {

    private Department categoryParent;

    public Category(String name, String url, Department department) {
        super(name, url);
        this.categoryParent = department;
    }

    public Department getDepartmentParent() {
        return categoryParent;
    }

    public void setDepartmentParent(Department categoryParent) {
        this.categoryParent = categoryParent;
    }

}

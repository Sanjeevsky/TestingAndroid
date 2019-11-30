package com.devsanjeev.testingandroid;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.ArrayList;
@IgnoreExtraProperties
public class NewProduct {
private String productName="";
    private String category="";
    private String author="";
    private String productDescription="";
    private ArrayList<Comments> comments=new ArrayList<>();
    private ArrayList<String> imageUrl=new ArrayList<>();

    public NewProduct(String productName, String category, String author, String productDescription, ArrayList<Comments> comments, ArrayList<String> imageUrl) {
        this.productName = productName;
        this.category = category;
        this.author = author;
        this.productDescription = productDescription;
        this.comments = comments;
        this.imageUrl = imageUrl;
    }

    public NewProduct() {
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public ArrayList<Comments> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Comments> comments) {
        this.comments = comments;
    }

    public ArrayList<String> getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(ArrayList<String> imageUrl) {
        this.imageUrl = imageUrl;
    }
}

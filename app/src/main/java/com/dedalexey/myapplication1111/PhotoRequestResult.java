package com.dedalexey.myapplication1111;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PhotoRequestResult {
    PhotoResults photos;
    String stat;
    List<GalleryItem> getResults() {
        return photos.getPhotolist();
    }
    int getPageCount() {
        return photos.getMaxPages();
    }
    int getItemCount() {
        return photos.getTotal();
    }
    int getItemsPerPage() {
        return photos.getItemsPerPage();
    }

}

class PhotoResults {
    int page;
    int pages;
    int perpage;
    int total;
    @SerializedName("photo")
    List<GalleryItem> photolist;

    List<GalleryItem> getPhotolist() {
        return photolist;
    }
    int getItemsPerPage() {
        return perpage;
    }
    int getMaxPages() {
        return pages;
    }
    int getTotal() {
        return total;
    }

}
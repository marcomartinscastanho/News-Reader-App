package com.martinscastanho.marco.newsreaderapp;

import java.util.ArrayList;

class ListOfArticles {
    private ArrayList<Integer> articleIds;
    private ArrayList<String> articleTitles;
    private ArrayList<String> articleUrls;

    ListOfArticles() {
        articleIds = new ArrayList<>();
        articleTitles = new ArrayList<>();
        articleUrls = new ArrayList<>();
    }

    void add(Integer id, String title, String url){
        articleIds.add(id);
        articleTitles.add(title);
        articleUrls.add(url);
    }

    ArrayList<String> getTitles() {
        return articleTitles;
    }

    String getArticleUrl(Integer pos){
        return articleUrls.get(pos);
    }
}

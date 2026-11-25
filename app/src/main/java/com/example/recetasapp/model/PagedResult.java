package com.example.recetasapp.model;

import java.util.List;

public class PagedResult<T> {
    public List<T> items;
    public int page;
    public int pageSize;
    public int totalCount;
    public int totalPages;
}
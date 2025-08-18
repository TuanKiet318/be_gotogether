package com.vn.gotogether.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public abstract class PageResponseAbstract implements Serializable {
    public int pageNumber;
    public int pageSize;
    public long totalPages;
    public long totalElements;
}

package com.djmart.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Generic response container for paginated result sets.
 *
 * @param <T> Element type
 */
public class PageResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public PageResponse() {
        this.content = Collections.emptyList();
    }

    public PageResponse(List<T> content, int pageNumber, int pageSize, long totalElements) {
        this.content = content != null ? content : Collections.emptyList();
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / (double) pageSize);
        this.first = pageNumber <= 1;
        this.last = pageNumber >= totalPages;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    @Override
    public String toString() {
        return "PageResponse{" +
                "pageNumber=" + pageNumber +
                ", pageSize=" + pageSize +
                ", totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                '}';
    }
}

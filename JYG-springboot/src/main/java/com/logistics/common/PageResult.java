package com.logistics.common;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分页查询结果
 *
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> {

    /** 当前页码 */
    private Integer page;

    /** 每页条数 */
    private Integer size;

    /** 总记录数 */
    private Long total;

    /** 总页数 */
    private Integer totalPages;

    /** 数据列表 */
    private List<T> data;

    public PageResult() {
    }

    public PageResult(Integer page, Integer size, Long total, Integer totalPages, List<T> data) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = totalPages;
        this.data = data;
    }

    /**
     * 从 Spring Data Page 构建
     */
    public static <T> PageResult<T> of(Page<?> page, List<T> data) {
        return new PageResult<>(
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                data);
    }
}

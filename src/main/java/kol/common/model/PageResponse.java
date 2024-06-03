package kol.common.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * 分页
 *
 * @author kent
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PageResponse<T> extends Response {
    private static final long serialVersionUID = 1L;
    private long totalCount = 0;
    private int pageSize = 1;
    private int pageIndex = 1;
    private Collection<T> data;

    public boolean isEmpty() {
        return this.data == null || this.data.size() == 0;
    }

    public boolean isNotEmpty() {
        return !this.isEmpty();
    }

    public static PageResponse buildSuccess() {
        PageResponse response = new PageResponse<>();
        response.setSuccess(true);
        response.setErrCode("");
        response.setErrMessage("");
        response.setData(new ArrayList());
        return response;
    }

    public static PageResponse<?> buildFailure(String errCode, String errMessage) {
        PageResponse<?> response = new PageResponse<>();
        response.setSuccess(false);
        response.setErrCode(errCode);
        response.setErrMessage(errMessage);
        return response;
    }

    public static <T> PageResponse<T> of(int pageSize, int pageIndex) {
        PageResponse<T> response = new PageResponse<>();
        response.setSuccess(true);
        response.setData(Collections.emptyList());
        response.setTotalCount(0);
        response.setPageSize(pageSize);
        response.setPageIndex(pageIndex);
        return response;
    }

    public static <T> PageResponse<T> of(Collection<T> data, long totalCount, int pageSize, int pageIndex) {
        PageResponse<T> response = new PageResponse<>();
        response.setSuccess(true);
        response.setData(data);
        response.setTotalCount(totalCount);
        response.setPageSize(pageSize);
        response.setPageIndex(pageIndex);
        return response;
    }
}

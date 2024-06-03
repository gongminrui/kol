package kol.common.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;

/**
 * 集合
 * @author kent
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class ArrayResponse<T> extends Response {
    private static final long serialVersionUID = 1L;
    private Collection<T> data;

    public boolean isEmpty() {
        return this.data == null || this.data.size() == 0;
    }

    public boolean isNotEmpty() {
        return !this.isEmpty();
    }

    public static ArrayResponse<?> buildSuccess() {
        ArrayResponse<?> response = new ArrayResponse<>();
        response.setSuccess(true);
        response.setErrCode("");
        response.setErrMessage("");
        return response;
    }

    public static ArrayResponse<?> buildFailure(String errCode, String errMessage) {
        ArrayResponse<?> response = new ArrayResponse<>();
        response.setSuccess(false);
        response.setErrCode(errCode);
        response.setErrMessage(errMessage);
        return response;
    }

    public static <T> ArrayResponse<T> of(Collection<T> data) {
        ArrayResponse<T> response = new ArrayResponse<>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }
}

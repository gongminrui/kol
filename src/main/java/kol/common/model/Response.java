package kol.common.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.io.Serializable;

/**
 * @author kent
 */
@Data
public class Response implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private String errCode;
    private String errMessage;

    private static ObjectMapper om = new ObjectMapper();

    public static Response buildSuccess() {
        Response response = new Response();
        response.setSuccess(true);
        response.setErrCode("");
        response.setErrMessage("");
        return response;
    }

    public static Response buildFailure(String errCode, String errMessage) {
        Response response = new Response();
        response.setSuccess(false);
        response.setErrCode(errCode);
        response.setErrMessage(errMessage);
        return response;
    }

    public String toJsonStr() {
        try {
            return om.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

package kol.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
/**
 * 包装错误信息
 * @author admin
 *
 */
@Getter
@AllArgsConstructor
@Accessors(chain = true)
public class AppException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	private String errorCode;
	private String errorMsg;
}

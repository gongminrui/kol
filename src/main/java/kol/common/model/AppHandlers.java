package kol.common.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 对返回结果进行一些包装
 *
 * @author guanzhenggang@gmail.com
 */
@ControllerAdvice(basePackages = "kol")
@Slf4j
public class AppHandlers {

    @ExceptionHandler(value = {AppException.class})
    public HttpEntity<Response> appException(AppException exception) {
        return new HttpEntity<>(Response.buildFailure(exception.getErrorCode(), exception.getErrorMsg()));
    }

    @ExceptionHandler(value = {VipAccessException.class})
    public HttpEntity<Response> vipAccessException(VipAccessException exception) {
        return new HttpEntity<>(Response.buildFailure(exception.getErrorCode(), exception.getErrorMsg()));
    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    public HttpEntity<Response> accessDeniedException(AccessDeniedException exception) {
        return new HttpEntity<>(Response.buildFailure(ErrorCode.ACCESS_DENIED, "拒绝访问"));
    }

    @ExceptionHandler(value = {RuntimeException.class})
    public HttpEntity<Response> exceptionHandler(RuntimeException exception) {
        log.error(exception.getMessage(), exception);
        return new HttpEntity<>(Response.buildFailure(ErrorCode.SERVER_ERROR, exception.getMessage()));
    }

    /**
     * 请求参数(Parameter)缺失
     */
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public HttpEntity<Response> missingServletRequestParameterException(MissingServletRequestParameterException e) {
        return new HttpEntity<>(Response.buildFailure(ErrorCode.REQUEST_ERROR, e.getMessage()));
    }

    /**
     * 请求参数(RequestPart)缺失
     */
    @ExceptionHandler(value = MissingServletRequestPartException.class)
    public HttpEntity<Response> missingServletRequestPartException(MissingServletRequestPartException e) {
        return new HttpEntity<>(Response.buildFailure(ErrorCode.REQUEST_ERROR, e.getMessage()));
    }

    /**
     * 数据读取异常
     */
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public HttpEntity<Response> httpMessageNotReadableException(HttpMessageNotReadableException e) {
        return new HttpEntity<>(Response.buildFailure(ErrorCode.REQUEST_ERROR, e.getMessage()));
    }

    /**
     * 参数转换异常
     */
    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public HttpEntity<Response> methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return new HttpEntity<>(Response.buildFailure(ErrorCode.REQUEST_ERROR, e.getMessage()));
    }

    /**
     * 参数验证异常
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public HttpEntity<Response> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<String> collect = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(o -> o.getDefaultMessage())
                .collect(Collectors.toList());
        return new HttpEntity<>(Response.buildFailure(ErrorCode.REQUEST_ERROR, collect.toString()));
    }

    /**
     * 参数验证异常
     */
    @ExceptionHandler(value = BindException.class)
    public HttpEntity<Response> bindException(BindException e) {
        List<String> collect = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(o -> o.getDefaultMessage())
                .collect(Collectors.toList());
        return new HttpEntity<>(Response.buildFailure(ErrorCode.REQUEST_ERROR, collect.toString()));
    }

    /**
     * 参数不合法异常
     */
    @ExceptionHandler(value = IllegalArgumentException.class)
    public HttpEntity<Response> illegalArgumentException(IllegalArgumentException e) {
        return new HttpEntity<>(Response.buildFailure(ErrorCode.REQUEST_ERROR, e.getMessage()));
    }

    /**
     * 参数不合法异常
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    public HttpEntity<Response> constraintViolationException(ConstraintViolationException e) {
        List<String> collect = e.getConstraintViolations()
                .stream()
                .map(o -> o.getMessage())
                .collect(Collectors.toList());
        return new HttpEntity<>(Response.buildFailure(ErrorCode.REQUEST_ERROR, collect.toString()));
    }

    @ExceptionHandler(value = Exception.class)
    public HttpEntity<Response> exception(Exception exception) {
        log.error("未知错误", exception);
        return new HttpEntity<>(Response.buildFailure(ErrorCode.SERVER_ERROR, "服务器内部错误"));
    }
}

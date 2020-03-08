package com.xinzhi.furniture.mall.core.config;

import com.xinzhi.furniture.mall.db.result.ErrorCodeEnum;
import com.xinzhi.furniture.mall.db.result.Result;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.internal.engine.path.PathImpl;
import com.xinzhi.furniture.mall.core.util.ResponseUtil;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ControllerAdvice
@Order
public class GlobalExceptionHandler {

    private Log logger = LogFactory.getLog(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public Object badArgumentHandler(IllegalArgumentException e) {
        logger.error(e.getMessage(), e);
        return ResponseUtil.badArgumentValue();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public Object badArgumentHandler(MethodArgumentTypeMismatchException e) {
        logger.error(e.getMessage(), e);
        return ResponseUtil.badArgumentValue();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public Object badArgumentHandler(MissingServletRequestParameterException e) {
        logger.error(e.getMessage(), e);
        return ResponseUtil.badArgumentValue();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public Object badArgumentHandler(HttpMessageNotReadableException e) {
        logger.error(e.getMessage(), e);
        return ResponseUtil.badArgumentValue();
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseBody
    public Object badArgumentHandler(ValidationException e) {
        logger.error(e.getMessage(), e);
        if (e instanceof ConstraintViolationException) {
            ConstraintViolationException exs = (ConstraintViolationException) e;
            Set<ConstraintViolation<?>> violations = exs.getConstraintViolations();
            for (ConstraintViolation<?> item : violations) {
                String message = ((PathImpl) item.getPropertyPath()).getLeafNode().getName() + item.getMessage();
                return ResponseUtil.fail(402, message);
            }
        }
        return ResponseUtil.badArgumentValue();
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object seriousHandler(Exception e) {
        logger.error(e.getMessage(), e);
        return ResponseUtil.serious();
    }

    @ExceptionHandler(UserAuthException.class)
    @ResponseBody
    public Object userAuthHandler(Exception e) {
        logger.error(e.getMessage(), e);
        return ResponseUtil.unlogin();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Result argumentNotValidHandler(HttpServletRequest request, MethodArgumentNotValidException exception){
        logger.error(exception.getMessage(), exception);
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        if (fieldErrors.size() > 0) {
            FieldError fieldError = fieldErrors.get(0);
            String field = fieldError.getField();
            String defaultMessage = fieldError.getDefaultMessage();
            return Result.fail(ErrorCodeEnum.INPUT_ERROR.getCode(), field + " " + defaultMessage);
        }
        return null;
    }

}

package net.ebh.exam.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * 拦截器
 */
@ControllerAdvice
public class ExceptionAdvice {
    @ExceptionHandler(CException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public CheckResult CExceptionHandler(Exception exception) {
        CheckResult checkResult = CheckResult.newInstance(exception.getMessage(), exception.getMessage());
        checkResult.addErrData("msg", exception.getMessage());
        exception.printStackTrace();
        return checkResult;
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public CheckResult CommonExctptionHandler(NativeWebRequest request, Exception exception) {
        CheckResult checkResult = CheckResult.newInstance("700000", exception.getMessage());
        checkResult.addErrData("ex", exception);
        checkResult.addErrData("req", request);
        exception.printStackTrace();
        return checkResult;
    }

}

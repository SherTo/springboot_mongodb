package net.ebh.exam.Aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.Arrays;

/**
 * AOP DEMO
 */
@Aspect
@Order(1)
@Configuration
public class LogAop {
    public static final Logger logger = LoggerFactory.getLogger(LogAop.class);

    @Pointcut("execution(* net.ebh.exam.controller.*.*(..))")
    private void anyMethod() {
    }

    @Before("anyMethod()")
    public void doBefore(JoinPoint point) {
       logger.info("@Before：目标方法为：" +
                point.getSignature().getDeclaringTypeName() +
                "." + point.getSignature().getName());
        System.out.println("@Before：参数为：" + Arrays.toString(point.getArgs()));
        logger.info("@Before：被织入的目标对象为：" + point.getTarget());
    }

    @AfterReturning(pointcut = "anyMethod()",
            returning = "returnValue")
    public void log(JoinPoint point, Object returnValue) {
        logger.info("@AfterReturning：目标方法为：" +
                point.getSignature().getDeclaringTypeName() +
                "." + point.getSignature().getName());
        logger.info("@AfterReturning：参数为：" +
                Arrays.toString(point.getArgs()));
        logger.info("@AfterReturning：返回值为：" + returnValue);
        logger.info("@AfterReturning：被织入的目标对象为：" + point.getTarget());

    }

//    @After("anyMethod()")
//    public void releaseResource(JoinPoint point) {
//        logger.info("@After：模拟释放资源...");
//        logger.info("@After：目标方法为：" +
//                point.getSignature().getDeclaringTypeName() +
//                "." + point.getSignature().getName());
//        logger.info("@After：参数为：" + Arrays.toString(point.getArgs()));
//        logger.info("@After：被织入的目标对象为：" + point.getTarget());
//    }
}

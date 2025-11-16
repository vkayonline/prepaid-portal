package online.vkay.prepaidportal.common.aspect;

import lombok.extern.slf4j.Slf4j;
import online.vkay.prepaidportal.common.enums.ResponseStatus;
import online.vkay.prepaidportal.common.exception.BaseException;
import online.vkay.prepaidportal.dto.BaseApiResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 🧭 Method Logging Aspect
 * ----------------------------------------------------
 * Logs entry, exit, and execution time of controller/service methods.
 * Prints stack trace ONLY here for debugging clarity.
 */
@Slf4j
@Aspect
@Component
public class MethodLoggingAspect {

    @Pointcut("execution(* online.vkay..controller..*(..)) || execution(* online.vkay..service..*(..))")
    public void applicationMethods() {}

    @Around("applicationMethods()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String method = signature.getDeclaringTypeName() + "." + signature.getName();

        log.info("➡️ Entering [{}] with args: {}", method, Arrays.toString(joinPoint.getArgs()));

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;

            if (result instanceof BaseApiResponse<?> response) {
                log.info("✅ Exiting [{}] [{}:{}] | success={} | time={}ms",
                        method, response.getRc(), response.getCode(), response.isSuccess(), duration);
            } else {
                log.info("✅ Exiting [{}] | resultType={} | time={}ms",
                        method,
                        result != null ? result.getClass().getSimpleName() : "void",
                        duration);
            }

            return result;
        } catch (BaseException ex) {
            long duration = System.currentTimeMillis() - start;
            ResponseStatus status = ex.getStatus();

            // Log the stack trace ONCE here
            log.error("❌ [{}] [{}:{}] | msg='{}' | time={}ms",
                    method, status.getRc(), status.getCode(), ex.getMessage(), duration, ex);

            throw ex; // propagate to ControllerAdvice
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - start;

            // Unhandled or unexpected exceptions (still print stack trace once)
            log.error("💥 [{}] | Unhandled Exception='{}' | time={}ms",
                    method, ex.getMessage(), duration, ex);

            throw ex;
        }
    }
}

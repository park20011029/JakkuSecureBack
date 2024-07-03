package secure.project.secureProject.security.oauth;


import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import secure.project.secureProject.annotation.UserId;
import secure.project.secureProject.exception.ApiException;
import secure.project.secureProject.exception.ErrorDefine;

@Component
public class UserIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UserId.class) && parameter.getParameterType() == Long.class;
    }

    @Override
    public Long resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) throws Exception {
        Object userId = webRequest.getAttribute("USER_ID", RequestAttributes.SCOPE_REQUEST);
        if (userId == null) {
            throw new ApiException(ErrorDefine.USER_NOT_FOUND);
        }
        return Long.parseLong(userId.toString());
    }
}

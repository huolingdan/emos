package com.ma.config;

import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Bean
    public Docket createDocket() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2);

        ApiInfoBuilder apiInfoBuilder = new ApiInfoBuilder();
        apiInfoBuilder.title("EMOS在线办公系统");

        ApiInfo apiInfo = apiInfoBuilder.build();
        docket.apiInfo(apiInfo);


        ApiSelectorBuilder apiSelectorBuilder = docket.select();
        //选择所有的API
        apiSelectorBuilder.paths(PathSelectors.any());
        //根据注解过滤方法
        apiSelectorBuilder.apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class));
        docket = apiSelectorBuilder.build();

        //设置请求头添加令牌
        ApiKey apiKey = new ApiKey("token","token","header");
        List<ApiKey> apiKeyList = new ArrayList<>();
        apiKeyList.add(apiKey);
        docket.securitySchemes(apiKeyList);

        //设置作用域：全局
        AuthorizationScope scope = new AuthorizationScope("global","accessEverything");
        AuthorizationScope[] scopes = {scope};
        SecurityReference reference = new SecurityReference("token",scopes);
        List refList = new ArrayList();
        refList.add(reference);

        SecurityContext  context = SecurityContext.builder().securityReferences(refList).build();
        List contextList = new ArrayList();
        contextList.add(context);
        docket.securityContexts(contextList);

        return docket;
    }
}

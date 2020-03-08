package com.xinzhi.furniture.mall.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * swagger在线文档配置<br>
 * 项目启动后可通过地址：http://host:ip/swagger-ui.html 查看在线文档
 *
 * @author enilu
 * @version 2018-07-24
 */

@Configuration
@EnableSwagger2
public class AdminSwagger2Configuration {
    @Bean
    public Docket adminDocket() {

        List<Parameter> parameterList = new ArrayList<Parameter>();
        ParameterBuilder parameterBuilder = new ParameterBuilder();
        parameterBuilder.name("X-Litemall-Admin-Token").description("认证凭证").modelRef(new ModelRef("String")).parameterType("header").required(false).build();
        parameterList.add(parameterBuilder.build());

        return new Docket(DocumentationType.SWAGGER_2)
//                .groupName("admin")
                .apiInfo(adminApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.xinzhi.furniture.mall.admin.web"))
                .paths(PathSelectors.any())
                .build()
                .globalOperationParameters(parameterList);
    }

    private ApiInfo adminApiInfo() {
        return new ApiInfoBuilder()
                .title("心至家具后台管理API文档")
//                .description("心至家具后台管理API文档")
//                .termsOfServiceUrl("http://129.204.122.146:9999")
//                .contact("https://github.com/linlinjava/litemall")
                .version("1.0")
                .build();
    }
}

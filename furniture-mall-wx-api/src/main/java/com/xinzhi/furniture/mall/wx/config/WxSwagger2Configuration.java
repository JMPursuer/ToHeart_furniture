package com.xinzhi.furniture.mall.wx.config;

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
public class WxSwagger2Configuration {
    @Bean
    public Docket wxDocket() {

        List<Parameter> parameterList = new ArrayList<Parameter>();
        ParameterBuilder parameterBuilder = new ParameterBuilder();
        parameterBuilder.name("X-Litemall-Token").description("认证凭证").modelRef(new ModelRef("String")).parameterType("header").required(false).build();
        parameterList.add(parameterBuilder.build());

        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("wx")
                .apiInfo(wxApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.xinzhi.furniture.mall.wx.web"))
                .paths(PathSelectors.any())
                .build()
                .globalOperationParameters(parameterList);
    }

    private ApiInfo wxApiInfo() {
        return new ApiInfoBuilder()
                .title("心至家具小程序API文档")
//                .description("litemall小商场API")
//                .termsOfServiceUrl("https://github.com/linlinjava/litemall")
//                .contact("https://github.com/linlinjava/litemall")
                .version("1.0")
                .build();
    }
}

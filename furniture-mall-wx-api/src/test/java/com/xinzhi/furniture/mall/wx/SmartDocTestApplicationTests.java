package com.xinzhi.furniture.mall.wx;

import com.power.common.util.DateTimeUtil;
import com.power.doc.builder.ApiDocBuilder;
import com.power.doc.builder.HtmlApiDocBuilder;
import com.power.doc.constants.DocGlobalConstants;
import com.power.doc.constants.DocLanguage;
import com.power.doc.model.*;
import com.xinzhi.furniture.mall.db.enumeration.UserStatusEnum;
import com.xinzhi.furniture.mall.db.result.ErrorCodeEnum;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SmartDocTestApplicationTests {

	@Test
	public void testBuilderControllersApi() {
		ApiConfig config = new ApiConfig();
		config.setServerUrl("http://localhost:8080"); //非必须像
		//true会严格要求注释，推荐设置true
		config.setStrict(true);
		//true会将文档合并导出到一个markdown
		config.setAllInOne(true);
		//生成html时加密文档名不暴露controller的名称
		config.setMd5EncryptedHtmlName(true);

		//指定文档输出路径
		//@since 1.7 版本开始，选择生成静态html doc文档可使用该路径：DocGlobalConstants.HTML_DOC_OUT_PATH;
		config.setOutPath(DocGlobalConstants.HTML_DOC_OUT_PATH);
		// @since 1.2,如果不配置该选项，则默认匹配全部的controller,
		// 如果需要配置有多个controller可以使用逗号隔开
		config.setPackageFilters("com.xinzhi.furniture.mall.wx.web.controller");
		//不指定SourcePaths默认加载代码为项目src/main/java下的,如果项目的某一些实体来自外部代码可以一起加载
//		config.setSourceCodePaths(
				//自1.7.0版本开始，在此处可以不设置本地代码路径，单独添加外部代码路径即可
//            SourceCodePath.path().setDesc("本项目代码").setPath("src/main/java")
//				,
//				SourceCodePath.path().setDesc("加载项目外代码").setPath("E:\\ApplicationPower\\ApplicationPower\\Common-util\\src\\main\\java")
//		);
		//since 1.7.5
		//如果该选项的值为false,则smart-doc生成allInOne.md文件的名称会自动添加版本号
		config.setCoverOld(true);
		//since 1.7.5
		//设置项目名(非必须)，如果不设置会导致在使用一些自动添加标题序号的工具显示的序号不正常
		config.setProjectName("心至家具");
		//设置请求头，如果没有请求头，可以不用设置
//		config.setRequestHeaders(
//				ApiReqHeader.header().setName("sessionid").setType("string").setDesc("权限认证值").setRequired(true),
//				ApiReqHeader.header().setName("source").setType("string").setDesc("请求来源").setRequired(true)
//		);
		//对于外部jar的类，编译后注释会被擦除，无法获取注释，但是如果量比较多请使用setSourcePaths来加载外部代码
		//如果有这种场景，则自己添加字段和注释，api-doc后期遇到同名字段则直接给相应字段加注释
		config.setCustomResponseFields(
				CustomRespField.field().setName("success").setDesc("成功返回true, 失败返回false"),
				CustomRespField.field().setName("msg").setDesc("接口响应信息"),
				CustomRespField.field().setName("data").setDesc("接口响应数据"),
				CustomRespField.field().setName("code").setValue("1000").setDesc("响应代码")
		);

		//设置项目错误码列表，设置自动生成错误列表,
		List<ApiErrorCode> errorCodeList = new ArrayList<>();
		for (ErrorCodeEnum codeEnum : ErrorCodeEnum.values()) {
			ApiErrorCode errorCode = new ApiErrorCode();
			errorCode.setValue(codeEnum.getCode()).setDesc(codeEnum.getDesc());
			errorCodeList.add(errorCode);
		}
		//如果没需要可以不设置
		config.setErrorCodes(errorCodeList);

		//非必须只有当setAllInOne设置为true时文档变更记录才生效，https://gitee.com/sunyurepository/ApplicationPower/issues/IPS4O
		config.setRevisionLogs(
				RevisionLog.getLog().setRevisionTime("2018/12/15").setAuthor("chen").setRemarks("测试").setStatus("创建").setVersion("V1.0"),
				RevisionLog.getLog().setRevisionTime("2018/12/16").setAuthor("chen2").setRemarks("测试2").setStatus("修改").setVersion("V2.0")
		);

		//since 1.7.5
		//文档添加数据字典
		config.setDataDictionaries(
				ApiDataDictionary.dict().setTitle("用户状态").setEnumClass(UserStatusEnum.class).setCodeField("code").setDescField("desc")
		);

		config.setLanguage(DocLanguage.CHINESE);

		long start = System.currentTimeMillis();
		HtmlApiDocBuilder.builderControllersApi(config);

		//@since 1.7+版本开始，smart-doc支持生成带书签的html文档，html文档可选择下面额方式
		ApiDocBuilder.builderControllersApi(config);
		//@since 1.7+版本开始，smart-doc支撑生成AsciiDoc文档，你可以把AsciiDoc转成HTML5的格式。
		//@see https://gitee.com/sunyurepository/api-doc-test
		//AdocDocBuilder.builderControllersApi(config);
		//@since 1.7.8,smart-doc支持导出Postman测试的json
		//PostmanJsonBuilder.buildPostmanApi(config);

		long end = System.currentTimeMillis();
		DateTimeUtil.printRunTime(end, start);
	}

}

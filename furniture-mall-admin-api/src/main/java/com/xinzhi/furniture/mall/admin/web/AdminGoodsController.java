package com.xinzhi.furniture.mall.admin.web;

import com.google.common.collect.Lists;
import com.xinzhi.furniture.mall.admin.annotation.RequiresPermissionsDesc;
import com.xinzhi.furniture.mall.admin.service.AdminGoodsService;
import com.xinzhi.furniture.mall.db.domain.*;
import com.xinzhi.furniture.mall.db.result.ErrorCodeEnum;
import com.xinzhi.furniture.mall.db.result.Result;
import com.xinzhi.furniture.mall.db.service.req.IdObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.xinzhi.furniture.mall.admin.dto.GoodsAllinone;
import com.xinzhi.furniture.mall.core.validator.Order;
import com.xinzhi.furniture.mall.core.validator.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/admin/goods")
@Validated
@Slf4j
@Transactional
@Api(tags = "商品管理")
public class AdminGoodsController {

    @Autowired
    private AdminGoodsService adminGoodsService;

    /**
     * 查询商品
     *
     * @param goodsId
     * @param goodsSn
     * @param name
     * @param page
     * @param limit
     * @param sort
     * @param order
     * @return
     */
    @RequiresPermissions("admin:goods:list")
    @RequiresPermissionsDesc(menu = {"商品管理", "商品管理"}, button = "查询")
    @GetMapping("/list")
    public Object list(Integer goodsId, String goodsSn, String name,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       @Order @RequestParam(defaultValue = "desc") String order) {
        return adminGoodsService.list(goodsId, goodsSn, name, page, limit, sort, order);
    }

    @GetMapping("/catAndBrand")
    public Object list2() {
        return adminGoodsService.list2();
    }

    /**
     * 编辑商品
     *
     * @param goodsAllinone
     * @return
     */
    @RequiresPermissions("admin:goods:update")
    @RequiresPermissionsDesc(menu = {"商品管理", "商品管理"}, button = "编辑")
    @PostMapping("/update")
    public Object update(@RequestBody GoodsAllinone goodsAllinone) {
        return adminGoodsService.update(goodsAllinone);
    }

    /**
     * 删除商品
     *
     * @param goods
     * @return
     */
    @RequiresPermissions("admin:goods:delete")
    @RequiresPermissionsDesc(menu = {"商品管理", "商品管理"}, button = "删除")
    @PostMapping("/delete")
    public Object delete(@RequestBody LitemallGoods goods) {
        return adminGoodsService.delete(goods);
    }

    /**
     * 添加商品
     *
     * @param goodsAllinone
     * @return
     */
//    @RequiresPermissions("admin:goods:create")
//    @RequiresPermissionsDesc(menu = {"商品管理", "商品管理"}, button = "上架")
//    @PostMapping("/create")
//    public Object create(@RequestBody GoodsAllinone goodsAllinone) {
//        return adminGoodsService.create(goodsAllinone);
//    }

    /**
     * 商品详情
     *
     * @param id
     * @return
     */
    @RequiresPermissions("admin:goods:read")
    @RequiresPermissionsDesc(menu = {"商品管理", "商品管理"}, button = "详情")
    @GetMapping("/detail")
    public Object detail(@NotNull Integer id) {
        return adminGoodsService.detail(id);

    }

    @RequiresPermissions("admin:goods:upload")
    @RequiresPermissionsDesc(menu = {"商品管理", "产品配置"}, button = "临时规格上传")
    @ApiOperation(value = "临时规格上传")
    @PostMapping("/option/upload")
    public Result<List<LitemallGoodsOptionTmp>> upload(@RequestParam("file") MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if(!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx")){
            return Result.fail(ErrorCodeEnum.EXCEL_FORMAT_ERROR);
        }
        Workbook workbook = null;
        try {
            InputStream inputStream = file.getInputStream();
            //判断什么类型文件
            if (fileName.endsWith(".xls")) {
                workbook = new HSSFWorkbook(inputStream);
            } else if (fileName.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            }
        } catch (Exception e) {
            log.error("File error", e);
            return Result.fail(ErrorCodeEnum.EXCEL_FORMAT_ERROR);
        }
        if(workbook == null) {
            return Result.fail(ErrorCodeEnum.EXCEL_FORMAT_ERROR);
        }
        Sheet sheet = workbook.getSheetAt(0);
        // 总行数
        int rowLength = sheet.getLastRowNum() + 1;
        if(rowLength <= 1) {
            return Result.fail(ErrorCodeEnum.EXCEL_FORMAT_ERROR);
        }
        List<LitemallGoodsOptionTmp> optionList = Lists.newArrayList();
        for(int i = 1; i < rowLength; i++) {
            Row row = sheet.getRow(i);
            String type = row.getCell(0).getStringCellValue();
            String code = row.getCell(1).getStringCellValue();
            String image = row.getCell(2).getStringCellValue();
            double price = row.getCell(3).getNumericCellValue();
            String material = row.getCell(4).getStringCellValue();
            String desc = row.getCell(5).getStringCellValue();
            String status = row.getCell(6).getStringCellValue();
            log.info(type + " " + code + " " + image + " " + price + " " + material + " " + desc + " " + status);
            LitemallGoodsOptionTmp option = new LitemallGoodsOptionTmp();
            option.setType(type);
            option.setCode(code);
            option.setMaterial(material);
            option.setDesc(desc);
            option.setStatus("Y".equalsIgnoreCase(status) ? 1 : 0);
            option.setImage(image);
            option.setPrice(new BigDecimal(price));
            optionList.add(option);
        }
        return adminGoodsService.add(optionList);
    }


    @RequiresPermissions("admin:goods:option:delete")
    @RequiresPermissionsDesc(menu = {"商品管理", "产品配置"}, button = "临时规格删除")
    @ApiOperation(value = "临时规格删除")
    @PostMapping("/option/delete")
    public Result deleteOption() {
        return adminGoodsService.deleteOption();
    }

    @RequiresPermissions("admin:goods:option:list")
    @RequiresPermissionsDesc(menu = {"商品管理", "产品配置"}, button = "临时规格列表")
    @ApiOperation(value = "临时规格列表")
    @PostMapping("/option/list")
    public Result<List<LitemallGoodsOptionTmp>> listOption() {
        return adminGoodsService.listOption();
    }

    @RequiresPermissions("admin:goods:option:use")
    @RequiresPermissionsDesc(menu = {"商品管理", "产品配置"}, button = "使用临时规格数据")
    @ApiOperation(value = "使用临时规格数据")
    @PostMapping("/option/use")
    public Result useOption() {
        return adminGoodsService.insertOrUpdate();
    }

    @RequiresPermissions("admin:goods:option:download")
    @RequiresPermissionsDesc(menu = {"商品管理", "产品配置"}, button = "下载规格模板")
    @ApiOperation(value = "下载规格模板")
    @PostMapping("/option/download")
    public void downloadOption(HttpServletResponse response) {
        ClassPathResource resource = new ClassPathResource("/templates/option.xlsx");
        try {
            InputStream inputStream = resource.getInputStream();
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=option.xlsx");
            byte[] bytes = new byte[inputStream.available()];
            int len;
            while ((len = inputStream.read(bytes)) != -1) {
                response.getOutputStream().write(bytes, 0, len);
                response.flushBuffer();
            }
            response.flushBuffer();
            inputStream.close();
        } catch (Exception e) {
            log.error("下载规格模板错误", e);
        }
    }

}

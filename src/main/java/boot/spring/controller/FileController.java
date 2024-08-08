package boot.spring.controller;


import java.io.InputStream;
import java.net.URLEncoder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import boot.spring.domain.AjaxResult;
import boot.spring.service.MinIOService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Api(tags = "文件操作接口")
@RestController
@RequestMapping("/minio")
@Slf4j
public class FileController {
    @Autowired
    private MinIOService minIOService;

    @ApiOperation("上传一个文件")
    @PostMapping("/uploadfile")
    public AjaxResult fileupload(
            @RequestParam MultipartFile uploadfile, @RequestParam String bucket,
            @RequestParam(required = false) String objectName
    ) throws Exception {
        minIOService.createBucket(bucket);
        if (objectName != null) {
            minIOService.uploadFile(uploadfile.getInputStream(), bucket,
                    objectName + "/" + uploadfile.getOriginalFilename());
        } else {
            minIOService.uploadFile(uploadfile.getInputStream(), bucket, uploadfile.getOriginalFilename());
        }
        return AjaxResult.success();
    }

    @ApiOperation("列出所有的桶")
    @GetMapping("/listBuckets")
    public AjaxResult listBuckets() throws Exception {
        return AjaxResult.success(minIOService.listBuckets());
    }

    @ApiOperation("递归列出一个桶中的所有文件和目录")
    @GetMapping("/listFiles")
    public AjaxResult listFiles(@RequestParam String bucket) throws Exception {
        return AjaxResult.success("200", minIOService.listFiles(bucket));
    }

    @ApiOperation("下载一个文件")
    @GetMapping("/downloadFile")
    public void downloadFile(
            @RequestParam String bucket, @RequestParam String objectName,
            HttpServletResponse response
    ) throws Exception {
        InputStream stream = minIOService.download(bucket, objectName);
        ServletOutputStream output = response.getOutputStream();
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(objectName.substring(
                objectName.lastIndexOf("/") + 1), "UTF-8"));
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("UTF-8");
        IOUtils.copy(stream, output);
    }


    @ApiOperation("删除一个文件")
    @GetMapping("/deleteFile")
    public AjaxResult deleteFile(@RequestParam String bucket, @RequestParam String objectName) throws Exception {
        minIOService.deleteObject(bucket, objectName);
        return AjaxResult.success();
    }

    @ApiOperation("删除一个桶")
    @GetMapping("/deleteBucket")
    public AjaxResult deleteBucket(@RequestParam String bucket) throws Exception {
        minIOService.deleteBucket(bucket);
        return AjaxResult.success();
    }
}
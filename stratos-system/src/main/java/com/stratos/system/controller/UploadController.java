package com.stratos.system.controller;

import com.stratos.common.exception.BusinessException;
import com.stratos.common.result.Result;
import com.stratos.common.result.ResultCode;
import com.stratos.common.util.SnowflakeIdGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Tag(name = "本地上传")
@RestController
@RequiredArgsConstructor
public class UploadController {

    private static final Set<String> ALLOWED = Set.of("jpg", "jpeg", "png", "gif", "webp", "svg");

    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Value("${stratos.upload.dir:uploads}")
    private String uploadDir;

    @Operation(summary = "上传图片")
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "请选择文件");
        }
        String original = file.getOriginalFilename();
        String ext = StringUtils.getFilenameExtension(original);
        if (!StringUtils.hasText(ext) || !ALLOWED.contains(ext.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "仅支持图片文件");
        }
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        String filename = snowflakeIdGenerator.nextId() + "." + ext.toLowerCase(Locale.ROOT);
        Path target = dir.resolve(filename);
        file.transferTo(target);
        Map<String, String> data = new HashMap<>();
        data.put("filename", filename);
        data.put("url", "/api/files/" + filename);
        return Result.success("上传成功", data);
    }

    @Operation(summary = "读取上传文件")
    @GetMapping("/files/{filename}")
    public ResponseEntity<Resource> file(@PathVariable String filename) {
        if (!StringUtils.hasText(filename) || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }
        Path path = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(filename);
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }
        String contentType;
        try {
            contentType = Files.probeContentType(path);
        } catch (IOException ex) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : contentType))
                .body(new FileSystemResource(path));
    }
}

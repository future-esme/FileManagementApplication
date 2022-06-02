package com.esempla.file.service;

import com.esempla.file.config.AppProperties;
import com.esempla.file.domain.Document;
import com.esempla.file.domain.User;
import com.esempla.file.repository.DocumentRepository;
import com.esempla.file.repository.UserRepository;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
public class MinioService {

    private final MinioClient minioClient;
    private final AppProperties properties;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private String bucketName;

    public MinioService(MinioClient minioClient, AppProperties properties, UserRepository userRepository, DocumentRepository documentRepository) {
        this.minioClient = minioClient;
        this.properties = properties;
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.bucketName = properties.getMinio().getBucketName();
    }

   /* public List<FileDto> getListObjects() {
        List<FileDto> objects = new ArrayList<>();
        try {
            Iterable<Result<Item>> result = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .recursive(true)
                    .build());
            for (Result<Item> item : result) {
                objects.add(FileDto.builder()
                        .filename(item.get().objectName())
                        .size(item.get().size())
                        .url(getPreSignedUrl(item.get().objectName()))
                        .build());
            }
            return objects;
        } catch (Exception e) {
            log.error("Happened error when get list objects from minio: ", e);
        }

        return objects;
    }*/

   /* private String getPreSignedUrl(String filename) {
        return "http://localhost:9001/file/".concat(filename);
    }*/

    public InputStream getObject(String filename, String path) {
        InputStream stream;
        try {
            stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(path)
                    .object(filename)
                    .build());
        } catch (Exception e) {
            log.error("Happened error when get list objects from minio: ", e);
            return null;
        }

        return stream;
    }
    public boolean deleteFile(String fileName, String path) {

        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(path).object(fileName).build());
        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean uploadFile(MultipartFile file, String userLogin) {
        try {
            String objectName = userLogin + "/" + UUID.randomUUID().toString() + file.getOriginalFilename();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .build());
            Document document = new Document();
            document.setTitle(file.getOriginalFilename());
            document.setUuidDoc(objectName);
            document.setPath(bucketName);
            document.setSize(file.getSize());
            document.setMimeType(file.getContentType());
            documentRepository.save(document);

        } catch (Exception e) {
            log.error("Happened error when upload file: ", e);
        }
        return true;
    }

}

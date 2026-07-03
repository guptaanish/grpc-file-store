package com.example.filestore.mapper;

import java.time.Instant;

import com.google.protobuf.Timestamp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.example.filestore.entity.FileEntity;
import com.example.filestore.entity.FileVersionEntity;
import com.example.filestore.grpc.FileMetadata;
import com.example.filestore.grpc.FileVersionInfo;

/**
 * MapStruct mapper for converting between JPA entities and proto messages.
 */
@Mapper(componentModel = "spring")
public interface FileProtoMapper {

    /**
     * Maps a FileEntity and its latest version to a proto FileMetadata message.
     *
     * @param entity  the file entity.
     * @param version the latest version entity.
     * @return the proto FileMetadata message.
     */
    @Mapping(target = "fileId", expression = "java(entity.getId().toString())")
    @Mapping(target = "filename", source = "entity.filename")
    @Mapping(target = "contentType", source = "entity.contentType")
    @Mapping(target = "size", source = "version.size")
    @Mapping(target = "checksum", source = "version.checksum")
    @Mapping(target = "currentVersion", source = "entity.currentVersion")
    @Mapping(target = "createdAt", source = "entity.createdAt", qualifiedByName = "instantToTimestamp")
    @Mapping(target = "updatedAt", source = "entity.updatedAt", qualifiedByName = "instantToTimestamp")
    @Mapping(target = "mergeFrom", ignore = true)
    @Mapping(target = "clearField", ignore = true)
    @Mapping(target = "clearOneof", ignore = true)
    @Mapping(target = "unknownFields", ignore = true)
    @Mapping(target = "allFields", ignore = true)
    FileMetadata toFileMetadata(FileEntity entity, FileVersionEntity version);

    /**
     * Maps a FileVersionEntity to a proto FileVersionInfo message.
     *
     * @param version the version entity.
     * @return the proto FileVersionInfo message.
     */
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToTimestamp")
    @Mapping(target = "mergeFrom", ignore = true)
    @Mapping(target = "clearField", ignore = true)
    @Mapping(target = "clearOneof", ignore = true)
    @Mapping(target = "unknownFields", ignore = true)
    @Mapping(target = "allFields", ignore = true)
    FileVersionInfo toFileVersionInfo(FileVersionEntity version);

    /**
     * Converts a Java Instant to a protobuf Timestamp.
     *
     * @param instant the instant to convert.
     * @return the protobuf timestamp.
     */
    @Named("instantToTimestamp")
    default Timestamp instantToTimestamp(Instant instant) {
        if (instant == null) {
            return Timestamp.getDefaultInstance();
        }
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}

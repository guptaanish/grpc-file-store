package com.example.filestore.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener for file lifecycle events.
 */
@Slf4j
@Component
public class FileEventListener {

    /**
     * Handles file upload events.
     *
     * @param event the upload event.
     */
    @EventListener
    public void onFileUploaded(FileUploadedEvent event) {
        log.info("File uploaded: {} v{} (id={})", event.filename(), event.version(), event.fileId());
    }

    /**
     * Handles file deletion events.
     *
     * @param event the delete event.
     */
    @EventListener
    public void onFileDeleted(FileDeletedEvent event) {
        log.info("File deleted: {} (id={})", event.filename(), event.fileId());
    }
}

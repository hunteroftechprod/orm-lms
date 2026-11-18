package com.example.lms.dto;

import java.time.Instant;

public class LessonDto {
    private Long id;
    private Long moduleId;
    private String title;
    private String content;
    private String videoUrl;
    private Instant scheduledAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getModuleId() { return moduleId; }
    public void setModuleId(Long moduleId) { this.moduleId = moduleId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public Instant getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }
}



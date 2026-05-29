package com.granishots.notifications.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateResponseDTO {
    private Long id;
    private String name;
    private String type;
    private String channel;
    private String subject;
    private String body;
    private Boolean active;
}

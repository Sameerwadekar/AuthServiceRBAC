package com.learn.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssignedUsersSummary {

    private long totalCount;

    @Builder.Default
    private List<UserPreviewAvatar> previewAvatars = new ArrayList<>();
}

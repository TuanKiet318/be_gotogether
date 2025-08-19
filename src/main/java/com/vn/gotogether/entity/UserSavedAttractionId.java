package com.vn.gotogether.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserSavedAttractionId implements Serializable {
    private String userId;
    private String attractionId;
}


package com.example.gradlecoding.domain.nft.domain;

import com.example.gradlecoding.global.domain.BaseEntity;
import com.fasterxml.jackson.databind.ser.Serializers.Base;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Nft extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String txhash; // 트랜잭션 블록의 해시값.
    private Long tokenId; // tokenId #없이 저장. 프론트가 보여줄때만 #. API 요청도 다 #없이
    @Column(precision = 18, scale = 10)
    private BigDecimal price; // eth
    private String type; // 문서로만
    private String seller; // 사람이름
    private String owner; // 현재 소유자 지갑
    private String description;
    private String fileURL;
    private Boolean certified;
    @Enumerated(EnumType.STRING)
    private Status status; // NFT 상태

    public static Nft of(String title, String txhash, Long tokenId, String type,
        String seller, String description, String fileURL, boolean certified, Status status) {
        return Nft.builder()
            .title(title)
            .txhash(txhash)
            .tokenId(tokenId)
            .type(type)
            .seller(seller)
            .description(description)
            .fileURL(fileURL)
            .certified(certified)
            .status(status)
            .build();
    }

    public void updateOwner(String owner) {
        this.owner = owner;
    }

    public void updateStatus(Status status) {
        this.status = status;
    }

    public void updatePrice(BigDecimal price) {
        this.price = price;
    }

}

package com.example.gradlecoding.domain.nft.dto.response;

import com.example.gradlecoding.domain.nft.domain.Nft;
import com.example.gradlecoding.domain.nft.domain.Status;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MintResponseDto {
    private Long id;
    private String title;
    private String txhash; // 트랜잭션 블록의 해시값.
    private Long tokenId; // tokenId #없이 저장. 프론트가 보여줄때만 #. API 요청도 다 #없이
    private String price; // eth -> 이건 status가 onsale일때만 값 있음
    private String type; // 문서로만
    private String seller; // 사람이름
    private String description;
    private String fileURL;
    private Boolean certified;
    private Status status; // NFT 상태

    public static MintResponseDto of(Nft nft) {
        return MintResponseDto.builder()
            .id(nft.getId())
            .title(nft.getTitle())
            .txhash(nft.getTxhash())
            .tokenId(nft.getTokenId())
            .price(nft.getPrice() == null ?
             null : new BigDecimal(nft.getPrice().toString()).stripTrailingZeros().toPlainString())
            .type(nft.getType())
            .seller(nft.getSeller())
            .description(nft.getDescription())
            .fileURL(nft.getFileURL())
            .certified(nft.getCertified())
            .status(nft.getStatus())
            .build();
    }
}

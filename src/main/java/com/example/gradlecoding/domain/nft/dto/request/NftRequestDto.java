package com.example.gradlecoding.domain.nft.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class NftRequestDto {

    @Schema(description = "NFT의 이름", example = "민주의 첫 NFT")
    private String name;

    @Schema(description = "NFT의 설명", example = "이것은 민주가 만든 첫 번째 디지털 아트입니다.")
    private String description;

    @Schema(description = "NFT 파일 (이미지 또는 미디어 파일)", type = "string", format = "binary")
    private MultipartFile file;

    @Schema(description = "NFT를 받을 사용자의 지갑 주소", example = "test 계정 지갑 주소")
    private String toAddress;

}


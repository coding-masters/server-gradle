package com.example.gradlecoding.domain.nft.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MintResponseDto {
    private String txHash;
    private Long tokenId;
}

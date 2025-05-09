package com.example.gradlecoding.domain.nft.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {

    MINT("MINT", "NFT로 등록됨"),
    ONSALE("ONSALE", "NFT 판매중"),
    HOLD("HOLD", "NFT 구매한 것");

    private final String key;
    private final String description;


}
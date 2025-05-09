package com.example.gradlecoding.domain.nft.controller;


import com.example.gradlecoding.domain.nft.dto.request.NftRequestDto;
import com.example.gradlecoding.domain.nft.service.NftService;
import com.example.gradlecoding.global.response.ApiResponse;
import com.example.gradlecoding.global.response.Status;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigInteger;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/nft")
@RequiredArgsConstructor
@Tag(name = "Nft", description = "nft 관련 api")
public class NftController {

    private final NftService nftService;


    @PostMapping("/mint")
    public ApiResponse<?> mint(@RequestBody NftRequestDto requestDto) throws Exception {
        String tx = nftService.mintNft(requestDto);
        return ApiResponse.success(Status.CREATED.getCode(),
            Status.CREATED.getMessage(), tx);
    }

    @PostMapping("/buy")
    public ApiResponse<?> buy(
        @RequestParam BigInteger tokenId,
        @RequestParam BigInteger priceInWei) throws Exception {
        String tx = nftService.buyNft(tokenId, priceInWei);
        return ApiResponse.success(Status.CREATED.getCode(),
            Status.CREATED.getMessage(), tx);
    }

}

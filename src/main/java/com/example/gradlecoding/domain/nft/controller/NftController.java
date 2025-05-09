package com.example.gradlecoding.domain.nft.controller;


import com.example.gradlecoding.domain.nft.dto.request.NftRequestDto;
import com.example.gradlecoding.domain.nft.service.NftService;
import com.example.gradlecoding.global.response.ApiResponse;
import com.example.gradlecoding.global.response.Status;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigInteger;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/nft")
@RequiredArgsConstructor
@Tag(name = "Nft", description = "nft 관련 api")
public class NftController {

    private final NftService nftService;


    @PostMapping(value = "/mint", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces =
        MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<?> mint( @RequestPart("file") MultipartFile file,
        @Parameter(description = "NFT 이름", required = true, example = "민주의 첫 NFT")
        @RequestPart("name") String name,

        @Parameter(description = "NFT 설명", required = true, example = "이것은 민주가 만든 첫 번째 디지털 아트입니다.")
        @RequestPart("description") String description,

        @Parameter(description = "NFT를 받을 지갑 주소", required = true, example = "0x1234...abcd")
        @RequestPart("toAddress") String toAddress) throws Exception {
        String tx = nftService.mintNft(name, description, toAddress, file);
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

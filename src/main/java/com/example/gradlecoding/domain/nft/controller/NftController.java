package com.example.gradlecoding.domain.nft.controller;


import com.example.gradlecoding.domain.nft.dto.response.MintResponseDto;
import com.example.gradlecoding.domain.nft.service.NftService;
import com.example.gradlecoding.global.response.ApiResponse;
import com.example.gradlecoding.global.response.Status;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.math.BigInteger;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping(value = "/all")
    public ApiResponse<?> getAll() {
        List<MintResponseDto> responseList = nftService.getAll();
        return ApiResponse.success(Status.CREATED.getCode(),
            Status.CREATED.getMessage(), responseList);
    }

    @PostMapping(value = "/mint", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces =
        MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<?> mint( @RequestPart("file") MultipartFile file,
        @Parameter(description = "NFT 제목", required = true, example = "민주의 첫 NFT")
        @RequestPart("title") String name,

        @Parameter(description = "NFT 설명", required = true, example = "이것은 민주가 만든 첫 번째 디지털 아트입니다.")
        @RequestPart("description") String description,

        @Parameter(description = "발행자 이름", required = true, example = "전민주.")
        @RequestPart("name") String seller,

        @Parameter(description = "NFT를 받을 지갑 주소", required = true, example = "0x1234...abcd")
        @RequestPart("toAddress") String toAddress) throws Exception {
        MintResponseDto response = nftService.mintNft(name, description, toAddress, seller, file);
        return ApiResponse.success(Status.CREATED.getCode(),
            Status.CREATED.getMessage(), response);
    }

    @PostMapping(value = "/list")
    public ApiResponse<?>  listNftForSale(
        @RequestParam("tokenId") @Schema(description = "판매할 NFT의 토큰 ID", example = "0") Long tokenId,
        @RequestParam("priceEth") @Schema(description = "판매할 가격 (ETH 단위)", example = "0.0001") BigDecimal priceEth
    ) throws Exception {

        MintResponseDto tx = nftService.listForSale(tokenId, priceEth);
        return ApiResponse.success(Status.CREATED.getCode(),
            Status.CREATED.getMessage(), tx);
    }

    @PostMapping("/buy")
    public ApiResponse<?> buy(
        @RequestParam("tokenId") @Schema(description = "구매할 NFT의 tokenId", example = "3") Long tokenId,
        @RequestParam("priceEth") @Schema(description = "판매할 가격 (ETH 단위)", example = "0.0001") BigDecimal priceEth,
        @RequestParam("buyer") @Schema(description = "구매자 지갑계쩡", example = "0x...") String buyer) throws Exception {

        String tx = nftService.buyNft(tokenId, priceEth, buyer);
        return ApiResponse.success(Status.CREATED.getCode(),
            Status.CREATED.getMessage(), tx);
    }

    @GetMapping("/my/buy")
    public ApiResponse<?> myBuy(@RequestParam("wallet") @Schema(description = "나의 지갑계쩡", example = "0x...") String wallet) throws Exception {
        List<MintResponseDto> lists = nftService.myBuy(wallet);
        return ApiResponse.success(Status.CREATED.getCode(),
            Status.CREATED.getMessage(), lists);
    }

    @GetMapping("/my/onsale")
    public ApiResponse<?> myOnsale(@RequestParam("wallet") @Schema(description = "나의 지갑계쩡", example = "0x...") String wallet) throws Exception {
        List<MintResponseDto> lists = nftService.myOnsale(wallet);
        return ApiResponse.success(Status.CREATED.getCode(),
            Status.CREATED.getMessage(), lists);
    }

}

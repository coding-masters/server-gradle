package com.example.gradlecoding.domain.nft.service;

import com.example.gradlecoding.domain.nft.domain.Nft;
import com.example.gradlecoding.domain.nft.domain.Status;
import com.example.gradlecoding.domain.nft.dto.response.MintResponseDto;
import com.example.gradlecoding.domain.nft.repository.NftRepository;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.RawTransactionManager;
import org.web3j.utils.Convert;

@RequiredArgsConstructor
@Service
@Slf4j
public class NftService {

    private final Web3j web3j;
    private final Credentials credentials;
    private final S3Service s3Service;
    private final NftRepository repository;

    private final String contractAddress = "0x04183d126577a039ea2abec488c7edeae8d2ae5d";
    private static final long CHAIN_ID = 11155111L;

    // ------------------ 상태 변경 함수 ------------------

    public MintResponseDto mintNft(String title, String description, String toAddress, String seller,
        MultipartFile file) throws Exception {
        String imageUrl = s3Service.uploadFile(file, "nft");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", title);
        metadata.put("description", description);
        metadata.put("image", imageUrl);

        String tokenURI = s3Service.uploadJson(metadata, "metadata"); // s3에 올리고 컨텐츠에 접근할 수 있는 tokenURI를 받음

        String fileUrl = extractImageUrlFromTokenURI(tokenURI);


        Function function = new Function(
            "mint",
            Arrays.asList(new Address(toAddress), new Utf8String(tokenURI)),
            Collections.emptyList()
        );

        log.info(function + "만듦");

        String encodedFunction = FunctionEncoder.encode(function);
        BigInteger gasPrice = Convert.toWei("1", Convert.Unit.GWEI).toBigInteger(); // 10 Gwei

        BigInteger gasLimit = BigInteger.valueOf(6721975);
        RawTransactionManager txManager = new RawTransactionManager(web3j, credentials, CHAIN_ID);

        log.info("[4] 트랜잭션 전송 시작 - to: {}, gasPrice: {}, gasLimit: {}", contractAddress, gasPrice, gasLimit);

        EthSendTransaction receipt = txManager.sendTransaction(
            gasPrice, gasLimit, contractAddress, encodedFunction, BigInteger.ZERO
        );

        if (receipt.hasError()) {
            log.error("⚠️ 트랜잭션 실패: {}", receipt.getError().getMessage());
        }

        log.info("[5] 트랜잭션 전송 완료 - txHash: {}", receipt.getTransactionHash());

        BigInteger nextTokenId = getNextTokenId(); // 컨트랙트 호출
        Long mintedTokenId = nextTokenId.subtract(BigInteger.ONE).longValue();

        Nft nft = Nft.of(title, receipt.getTransactionHash(), mintedTokenId + 1, "문서", seller, description, fileUrl,
            true, Status.MINT);
        nft.updateOwner(toAddress);
        repository.save(nft);

        return MintResponseDto.of(nft);
    }

    public String extractImageUrlFromTokenURI(String tokenUri) {
        RestTemplate restTemplate = new RestTemplate();

        // S3 JSON 파일에서 내용 가져오기
        ResponseEntity<Map> response = restTemplate.getForEntity(tokenUri, Map.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> metadata = response.getBody();
            return (String) metadata.get("image"); // 여기서 image URL 추출
        } else {
            throw new RuntimeException("토큰 URI를 불러오지 못했습니다.");
        }
    }


    public List<MintResponseDto> getAll() {
        return repository.findAllByStatus(Status.ONSALE)
            .stream()
            .map(MintResponseDto::of)
            .toList();
    }

    public BigInteger getNextTokenId() throws Exception {
        Function function = new Function(
            "nextTokenId",
            Collections.emptyList(),
            Collections.singletonList(new TypeReference<Uint256>() {})
        );

        String encodedFunction = FunctionEncoder.encode(function);

        EthCall response = web3j.ethCall(
            Transaction.createEthCallTransaction(credentials.getAddress(), contractAddress, encodedFunction),
            org.web3j.protocol.core.DefaultBlockParameterName.LATEST
        ).send();

        List<Type> decoded = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
        return (BigInteger) decoded.get(0).getValue();
    }

    public MintResponseDto listForSale(Long tokenId, BigDecimal priceEth) throws Exception {
        // ETH → wei 변환
        BigInteger priceInWei = priceEth.multiply(BigDecimal.TEN.pow(18)).toBigInteger();
        Function function = new Function(
            "listForSale",
            Arrays.asList(new Uint256(tokenId), new Uint256(priceInWei)),
            Collections.emptyList()
        );

        Nft nft = repository.findByTokenId(tokenId);
        nft.updateStatus(Status.ONSALE);
        nft.updatePrice(priceEth);

        repository.save(nft);

        String encodedFunction = FunctionEncoder.encode(function);
        BigInteger gasPrice = Convert.toWei("1", Convert.Unit.GWEI).toBigInteger(); // 1 Gwei
        BigInteger gasLimit = BigInteger.valueOf(6721975);

        RawTransactionManager txManager = new RawTransactionManager(web3j, credentials, CHAIN_ID);

        log.info("[판매등록] 트랜잭션 전송 시작 - tokenId: {}, price: {} wei", tokenId, priceInWei);

        EthSendTransaction receipt = txManager.sendTransaction(
            gasPrice, gasLimit, contractAddress, encodedFunction, BigInteger.ZERO
        );

        if (receipt.hasError()) {
            log.error("⚠️ 판매 등록 실패: {}", receipt.getError().getMessage());
            throw new RuntimeException("판매 등록 실패");
        }

        log.info("[판매등록] 트랜잭션 전송 완료 - txHash: {}", receipt.getTransactionHash());

        return MintResponseDto.of(nft);
    }


    public String buyNft(Long tokenId, BigDecimal priceEth, String buyer) throws Exception {
        Function function = new Function(
            "buy",
            Arrays.asList(new Uint256(tokenId)),
            Collections.emptyList()
        );

        // ETH → wei 변환
        BigInteger priceInWei = priceEth.multiply(BigDecimal.TEN.pow(18)).toBigInteger();

        Nft nft = repository.findByTokenId(tokenId);
        nft.updateOwner(buyer);
        nft.updateStatus(Status.HOLD);
        repository.save(nft);


        String encodedFunction = FunctionEncoder.encode(function);
        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
        BigInteger gasLimit = BigInteger.valueOf(6721975);

        RawTransactionManager txManager = new RawTransactionManager(web3j, credentials, CHAIN_ID);

        EthSendTransaction receipt = txManager.sendTransaction(
            gasPrice, gasLimit, contractAddress, encodedFunction, priceInWei
        );
        if (receipt.hasError()) {
            throw new RuntimeException("구매 트랜잭션 실패: " + receipt.getError().getMessage());
        }
        return receipt.getTransactionHash();
    }

    // ------------------ 조회 함수들 ------------------

    public BigInteger getPrice(BigInteger tokenId) throws Exception {
        Function function = new Function(
            "getPrice",
            Collections.singletonList(new Uint256(tokenId)),
            Collections.singletonList(new TypeReference<Uint256>() {})
        );

        String encodedFunction = FunctionEncoder.encode(function);
        EthCall response = web3j.ethCall(
            Transaction.createEthCallTransaction(credentials.getAddress(), contractAddress, encodedFunction),
            org.web3j.protocol.core.DefaultBlockParameterName.LATEST
        ).send();

        List<Type> decoded = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
        return ((Uint256) decoded.get(0)).getValue();
    }

    public String getOwner(BigInteger tokenId) throws Exception {
        Function function = new Function(
            "getOwner",
            Collections.singletonList(new Uint256(tokenId)),
            Collections.singletonList(new TypeReference<Address>() {})
        );

        String encodedFunction = FunctionEncoder.encode(function);
        EthCall response = web3j.ethCall(
            Transaction.createEthCallTransaction(credentials.getAddress(), contractAddress, encodedFunction),
            org.web3j.protocol.core.DefaultBlockParameterName.LATEST
        ).send();

        List<Type> decoded = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
        return decoded.get(0).getValue().toString();
    }

    public String getTokenURI(BigInteger tokenId) throws Exception {
        Function function = new Function(
            "getTokenURI",
            Collections.singletonList(new Uint256(tokenId)),
            Collections.singletonList(new TypeReference<Utf8String>() {})
        );

        String encodedFunction = FunctionEncoder.encode(function);
        EthCall response = web3j.ethCall(
            Transaction.createEthCallTransaction(credentials.getAddress(), contractAddress, encodedFunction),
            org.web3j.protocol.core.DefaultBlockParameterName.LATEST
        ).send();

        List<Type> decoded = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
        return decoded.get(0).getValue().toString();
    }

    public List<MintResponseDto> myBuy(String wallet) {
        return repository.findByOwnerAndStatus(wallet, Status.HOLD)
            .stream()
            .map(MintResponseDto::of)
            .toList();
    }

    public List<MintResponseDto> myOnsale(String wallet) {
        return repository.findByOwnerAndStatus(wallet, Status.ONSALE)
            .stream()
            .map(MintResponseDto::of)
            .toList();
    }




}

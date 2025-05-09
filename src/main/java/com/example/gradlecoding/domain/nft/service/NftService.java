package com.example.gradlecoding.domain.nft.service;

import com.example.gradlecoding.domain.nft.dto.response.MintResponseDto;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    private final String contractAddress = "0x04183d126577a039ea2abec488c7edeae8d2ae5d";
    private static final long CHAIN_ID = 11155111L;

    // ------------------ 상태 변경 함수 ------------------

    public MintResponseDto mintNft(String name, String description, String toAddress, MultipartFile file) throws Exception {
        String imageUrl = s3Service.uploadFile(file, "nft");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", name);
        metadata.put("description", description);
        metadata.put("image", imageUrl);

        String tokenURI = s3Service.uploadJson(metadata, "metadata"); // s3에 올리고 컨텐츠에 접근할 수 있는 tokenURI를 받음

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

        return new MintResponseDto(receipt.getTransactionHash(), mintedTokenId);
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

    public String listForSale(BigInteger tokenId, BigInteger priceInWei) throws Exception {
        Function function = new Function(
            "listForSale",
            Arrays.asList(new Uint256(tokenId), new Uint256(priceInWei)),
            Collections.emptyList()
        );

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

        return receipt.getTransactionHash();
    }


    public String buyNft(BigInteger tokenId, BigInteger priceInWei) throws Exception {
        Function function = new Function(
            "buy",
            Arrays.asList(new Uint256(tokenId)),
            Collections.emptyList()
        );

        String encodedFunction = FunctionEncoder.encode(function);
        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
        BigInteger gasLimit = BigInteger.valueOf(6721975);

        RawTransactionManager txManager = new RawTransactionManager(web3j, credentials, CHAIN_ID);

        EthSendTransaction receipt = txManager.sendTransaction(
            gasPrice, gasLimit, contractAddress, encodedFunction, priceInWei
        );
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



}

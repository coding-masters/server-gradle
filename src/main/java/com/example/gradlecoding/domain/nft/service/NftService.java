package com.example.gradlecoding.domain.nft.service;

import com.example.gradlecoding.domain.nft.dto.request.NftRequestDto;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

@RequiredArgsConstructor
@Service
public class NftService {

    private final Web3j web3j;
    private final Credentials credentials;
    private final S3Service s3Service;

    private final String contractAddress = "0x78c5B1577da951C984804f1a2aA0049fe23b2F29";
    private static final int CHAIN_ID = 11155111;

    // ------------------ 상태 변경 함수 ------------------

    public String mintNft(NftRequestDto requestDto) throws Exception {
        String imageUrl = s3Service.uploadFile(requestDto.getFile(), "nft");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", requestDto.getName());
        metadata.put("description", requestDto.getDescription());
        metadata.put("image", imageUrl);

        String tokenURI = s3Service.uploadJson(metadata, "metadata"); // s3에 올리고 컨텐츠에 접근할 수 있는 tokenURI를 받음

        Function function = new Function(
            "mint",
            Arrays.asList(new Address(requestDto.getToAddress()), new Utf8String(tokenURI)),
            Collections.emptyList()
        );

        String encodedFunction = FunctionEncoder.encode(function);
        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
        BigInteger gasLimit = BigInteger.valueOf(6721975);

        RawTransactionManager txManager = new RawTransactionManager(web3j, credentials, CHAIN_ID);

        EthSendTransaction receipt = txManager.sendTransaction(
            gasPrice, gasLimit, contractAddress, encodedFunction, BigInteger.ZERO
        );
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

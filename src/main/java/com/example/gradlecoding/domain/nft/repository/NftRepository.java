package com.example.gradlecoding.domain.nft.repository;

import com.example.gradlecoding.domain.nft.domain.Nft;
import com.example.gradlecoding.domain.nft.domain.Status;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NftRepository extends JpaRepository<Nft, Long> {

    Nft findByTokenId(Long tokenId);

    List<Nft> findAllByStatus(Status status);

    List<Nft> findByOwnerAndStatus(String wallet, Status status);

}

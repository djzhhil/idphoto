package com.example.idphoto.order;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdPhotoOrderRepository extends JpaRepository<IdPhotoOrder, Long> {

    Optional<IdPhotoOrder> findByOrderNo(String orderNo);

    Optional<IdPhotoOrder> findByOrderNoAndDownloadToken(String orderNo, String downloadToken);

    List<IdPhotoOrder> findTop50ByOpenidOrderByCreatedAtDesc(String openid);
}

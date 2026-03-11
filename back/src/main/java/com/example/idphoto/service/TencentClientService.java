package com.example.idphoto.service;

import com.tencentcloudapi.bda.v20200324.BdaClient;
import com.tencentcloudapi.bda.v20200324.models.SegmentPortraitPicRequest;
import com.tencentcloudapi.bda.v20200324.models.SegmentPortraitPicResponse;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TencentClientService {

    @Value("${tencent.secret-id}")
    private String secretId;

    @Value("${tencent.secret-key}")
    private String secretKey;

    @Value("${tencent.region}")
    private String region;

    public byte[] getPortraitMask(String base64Image) {
        try {

            // 去掉 data:image/...;base64, 前缀
            if (base64Image != null && base64Image.contains(",")) {
                base64Image = base64Image.substring(base64Image.indexOf(",") + 1);
            }

            Credential cred = new Credential(secretId, secretKey);

            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("bda.tencentcloudapi.com");

            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);

            BdaClient client = new BdaClient(cred, region, clientProfile);

            SegmentPortraitPicRequest req = new SegmentPortraitPicRequest();
            req.setImage(base64Image);
            req.setRspImgType("base64");

            SegmentPortraitPicResponse resp = client.SegmentPortraitPic(req);

            return java.util.Base64.getDecoder().decode(resp.getResultImage());

        } catch (TencentCloudSDKException e) {
            throw new RuntimeException("调用腾讯云抠图失败: " + e.getMessage(), e);
        }
    }
}

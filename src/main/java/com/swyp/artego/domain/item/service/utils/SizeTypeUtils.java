package com.swyp.artego.domain.item.service.utils;

import com.swyp.artego.domain.item.enums.SizeType;
import org.springframework.stereotype.Component;

@Component
public class SizeTypeUtils {

    /**
     * 제품의 가로, 세로, 높이를 토대로 제품의 사이즈(S,M,L 혹은 X)를 구한다.
     *
     * @param width  가로
     * @param height 세로
     * @param depth  폭
     * @return SizeType S, M, L 사이즈. X는 실측이 불가능한 작품을 의미합니다.
     */
    public SizeType calculateSizeType(int width, int height, int depth) {
        int sum = width + height + depth;

        if (sum == 0) return SizeType.X;
        else if (sum <= 100) return SizeType.S;
        else if (sum <= 160) return SizeType.M;
        else return SizeType.L;
    }
}

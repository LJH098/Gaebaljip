package com.gaebaljip.exceed.food.application.port.out;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import com.gaebaljip.exceed.food.adapter.out.FoodEntity;
import com.gaebaljip.exceed.food.domain.Food;
import com.gaebaljip.exceed.member.adapter.out.persistence.MemberEntity;

@Component
public interface FoodPort {

    List<Food> query(Long memberId, LocalDate date);

    List<FoodEntity> query(List<Long> foodIds);

    Slice<Food> query(String lastFoodName, int size, String keyword);

    FoodEntity command(FoodEntity foodEntity);

    void saveAll(List<FoodEntity> foodEntities);

    List<Food> findByMemberId(Long memberId);

    void deleteByAllByIdInQuery(List<Long> ids);

    List<FoodEntity> findByMemberEntity(MemberEntity memberEntity);

    FoodEntity query(Long foodId);
}

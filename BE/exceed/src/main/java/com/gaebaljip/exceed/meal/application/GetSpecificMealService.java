package com.gaebaljip.exceed.meal.application;

import com.gaebaljip.exceed.dto.response.CurrentMeal;
import com.gaebaljip.exceed.dto.response.DailyMeal;
import com.gaebaljip.exceed.dto.response.GetFood;
import com.gaebaljip.exceed.dto.response.GetMeal;
import com.gaebaljip.exceed.meal.application.port.in.GetSpecificMealQuery;
import com.gaebaljip.exceed.meal.application.port.out.LoadDailyMealPort;
import com.gaebaljip.exceed.meal.domain.MealModel;
import com.gaebaljip.exceed.meal.domain.MealsModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class GetSpecificMealService implements GetSpecificMealQuery {

    private final S3Presigner s3Presigner;
    private final LoadDailyMealPort loadDailyMealPort;
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Override
    public GetMeal execute(Long memberId, LocalDate date) {
        List<MealModel> mealModels = loadDailyMealPort.queryMealsForDate(memberId, date);
        MealsModel mealsModel = new MealsModel(mealModels);
        List<DailyMeal> dailyMeals = new ArrayList<>();
        setDailyMeals(mealModels, dailyMeals, memberId);
        return getGetMeal(mealsModel, dailyMeals);
    }

    private void setDailyMeals(List<MealModel> mealModels, List<DailyMeal> dailyMeals, Long memberId) {
        IntStream.range(0, mealModels.size()).forEach(i -> {
            DailyMeal dailyMeal = DailyMeal.builder()
                    .mealType(mealModels.get(i).getMealType())
                    .time(mealModels.get(i).getMealDateTime().toLocalTime())
                    .foods(mealModels.get(i).getFoodModels().stream().map(foodModel -> GetFood.builder()
                            .id(foodModel.getId())
                            .name(foodModel.getName())
                            .imageUri(getPresignedUrl(memberId, mealModels.get(i).getId()))
                            .build()).toList()
                    ).build();
            dailyMeals.add(dailyMeal);
        });
    }

    private String getPresignedUrl(Long memberId, Long mealId) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(memberId + "_" + mealId)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    private GetMeal getGetMeal(MealsModel mealsModel, List<DailyMeal> dailyMeals) {
        return GetMeal.builder()
                .currentMeal(getCurrentMeal(mealsModel))
                .dailyMeals(dailyMeals)
                .build();
    }

    private CurrentMeal getCurrentMeal(MealsModel mealsModel) {
        return CurrentMeal.builder()
                .currentCalorie(mealsModel.calculateCurrentCalorie())
                .currentCarbohydrate(mealsModel.calculateCurrentCarbohydrate())
                .currentFat(mealsModel.calculateCurrentFat())
                .currentProtein(mealsModel.calculateCurrentProtein())
                .build();
    }
}

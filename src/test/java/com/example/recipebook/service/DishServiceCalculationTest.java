package com.example.recipebook.service;

import com.example.recipebook.model.Nutrition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static com.example.recipebook.service.DishTestDataFactory.beet;
import static com.example.recipebook.service.DishTestDataFactory.ingredient;
import static com.example.recipebook.service.DishTestDataFactory.meat;
import static com.example.recipebook.service.DishTestDataFactory.potato;
import static com.example.recipebook.service.DishTestDataFactory.water;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DishServiceCalculationTest {
    @Mock
    private ProductLookup productLookup;

    private DishService dishService;

    @BeforeEach
    void setUp() {
        dishService = new DishService(null, productLookup);
        when(productLookup.getById("potato")).thenReturn(potato());
        when(productLookup.getById("water")).thenReturn(water());
        when(productLookup.getById("meat")).thenReturn(meat());
        when(productLookup.getById("beet")).thenReturn(beet());
    }

    @Nested
    @DisplayName("Эквивалентное разбиение")
    class EquivalencePartitioningTests {
        /**
         * Класс эквивалентности: один продукт с корректным >0 количеством
         * Картофель 200 г: 77*200 / 100 = 154 ккал
         */
        @Test
        @DisplayName("Должен корректно рассчитать калорийность блюда из одного продукта")
        void shouldCalculateCaloriesForSingleProduct() {
            Nutrition response = dishService.calculateDraft(List.of(ingredient("potato", 200.0)));

            assertEquals(154.0, response.getCalories());
        }

        /**
         * Класс эквивалентности: несколько продуктов с корректными количествами
         * 43 +115.5 +0 +224.64 = 383.14 ккал
         */
        @Test
        @DisplayName("Должен корректно рассчитать калорийность блюда из нескольких продуктов")
        void shouldCalculateCaloriesForMultipleProducts() {
            Nutrition response = dishService.calculateDraft(List.of(
                    ingredient("beet", 100.0),
                    ingredient("potato", 150.0),
                    ingredient("water", 300.0),
                    ingredient("meat", 120.0)
            ));

            assertEquals(383.14, response.getCalories());
        }

        /**
         * Класс эквивалентности: продукт с калорийностью 0
         */
        @Test
        @DisplayName("Должен вернуть 0 калорий для продукта с нулевой калорийностью")
        void shouldReturnZeroCaloriesForZeroCalorieProduct() {
            Nutrition response = dishService.calculateDraft(List.of(ingredient("water", 500.0)));

            assertEquals(0.0, response.getCalories());
        }

        /**
         * Класс эквивалентности: полный расчёт КБЖУ, не только калорий
         */
        @Test
        @DisplayName("Должен корректно рассчитать калории, белки, жиры и углеводы")
        void shouldCalculateCaloriesProteinsFatsAndCarbs() {
            Nutrition response = dishService.calculateDraft(List.of(ingredient("potato", 200.0)));

            assertEquals(154.0, response.getCalories());
            assertEquals(4.0, response.getProteins());
            assertEquals(0.8, response.getFats());
            assertEquals(32.6, response.getCarbs());
        }
    }

    @Nested
    @DisplayName("Анализ граничных значений")
    class BoundaryValueAnalysisTests {
        /**
         * Ниже границы
         */
        @Test
        @DisplayName("Должен выбросить ошибку при количестве продукта -0.1 г")
        void shouldThrowExceptionForQuantityBelowBoundary() {
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> dishService.calculateDraft(List.of(ingredient("potato", -0.1)))
            );

            assertTrue(exception.getMessage().contains("Количество каждого продукта должно быть больше 0"));
        }

        /**
         * Граница
         */
        @Test
        @DisplayName("Должна быть ошибка при количестве продукта 0 г")
        void shouldThrowExceptionForQuantityOnBoundary() {
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> dishService.calculateDraft(List.of(ingredient("potato", 0.0)))
            );

            assertTrue(exception.getMessage().contains("Количество каждого продукта должно быть больше 0"));
        }

        /**
         * Выше границы
         * 0.1 г: 77*0.1 / 100 = 0.077
         */
        @Test
        @DisplayName("Должен принять минимальное положительное количество продукта")
        void shouldAcceptQuantityAboveBoundary() {
            Nutrition response = dishService.calculateDraft(List.of(ingredient("potato", 0.1)));

            assertEquals(0.077, response.getCalories());
        }
    }
}

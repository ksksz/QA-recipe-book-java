package com.example.recipebook.storage;

import com.example.recipebook.model.Dish;
import com.example.recipebook.model.Flags;
import com.example.recipebook.model.Ingredient;
import com.example.recipebook.model.Nutrition;
import com.example.recipebook.model.Product;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;

@Component
public class RecipeBookDatabase {
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    public RecipeBookDatabase(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    @PostConstruct
    void initializeSchema() {
        jdbcTemplate.execute("""
                create table if not exists products (
                    id varchar(64) primary key,
                    name varchar(255) not null,
                    calories double precision not null,
                    proteins double precision not null,
                    fats double precision not null,
                    carbs double precision not null,
                    composition_text clob,
                    category varchar(128) not null,
                    cooking_need varchar(128) not null,
                    vegan boolean not null,
                    gluten_free boolean not null,
                    sugar_free boolean not null,
                    created_at varchar(64) not null,
                    updated_at varchar(64)
                )
                """);
        jdbcTemplate.execute("""
                create table if not exists product_photos (
                    product_id varchar(64) not null,
                    position integer not null,
                    path varchar(1024) not null,
                    primary key (product_id, position),
                    foreign key (product_id) references products(id) on delete cascade
                )
                """);
        jdbcTemplate.execute("""
                create table if not exists dishes (
                    id varchar(64) primary key,
                    name varchar(255) not null,
                    calories double precision not null,
                    proteins double precision not null,
                    fats double precision not null,
                    carbs double precision not null,
                    draft_calories double precision not null,
                    draft_proteins double precision not null,
                    draft_fats double precision not null,
                    draft_carbs double precision not null,
                    portion_size double precision not null,
                    category varchar(128) not null,
                    vegan boolean not null,
                    gluten_free boolean not null,
                    sugar_free boolean not null,
                    available_vegan boolean not null,
                    available_gluten_free boolean not null,
                    available_sugar_free boolean not null,
                    created_at varchar(64) not null,
                    updated_at varchar(64)
                )
                """);
        jdbcTemplate.execute("""
                create table if not exists dish_photos (
                    dish_id varchar(64) not null,
                    position integer not null,
                    path varchar(1024) not null,
                    primary key (dish_id, position),
                    foreign key (dish_id) references dishes(id) on delete cascade
                )
                """);
        jdbcTemplate.execute("""
                create table if not exists dish_ingredients (
                    dish_id varchar(64) not null,
                    position integer not null,
                    product_id varchar(64) not null,
                    amount double precision not null,
                    primary key (dish_id, position),
                    foreign key (dish_id) references dishes(id) on delete cascade,
                    foreign key (product_id) references products(id)
                )
                """);
    }

    public List<Product> listProducts() {
        return jdbcTemplate.query("""
                select id, name, calories, proteins, fats, carbs, composition_text, category, cooking_need,
                       vegan, gluten_free, sugar_free, created_at, updated_at
                from products
                order by created_at, id
                """, (rs, rowNum) -> {
            Product product = new Product();
            String id = rs.getString("id");
            product.setId(id);
            product.setName(rs.getString("name"));
            product.setPhotos(listProductPhotos(id));
            product.setCalories(rs.getDouble("calories"));
            product.setProteins(rs.getDouble("proteins"));
            product.setFats(rs.getDouble("fats"));
            product.setCarbs(rs.getDouble("carbs"));
            product.setCompositionText(rs.getString("composition_text"));
            product.setCategory(rs.getString("category"));
            product.setCookingNeed(rs.getString("cooking_need"));
            product.setFlags(new Flags(rs.getBoolean("vegan"), rs.getBoolean("gluten_free"), rs.getBoolean("sugar_free")));
            product.setCreatedAt(rs.getString("created_at"));
            product.setUpdatedAt(rs.getString("updated_at"));
            return product;
        });
    }

    public Optional<Product> findProduct(String id) {
        return listProducts().stream()
                .filter(product -> product.getId().equals(id))
                .findFirst();
    }

    public void insertProduct(Product product) {
        transactionTemplate.executeWithoutResult(status -> {
            jdbcTemplate.update("""
                    insert into products (
                        id, name, calories, proteins, fats, carbs, composition_text, category, cooking_need,
                        vegan, gluten_free, sugar_free, created_at, updated_at
                    ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    product.getId(),
                    product.getName(),
                    product.getCalories(),
                    product.getProteins(),
                    product.getFats(),
                    product.getCarbs(),
                    product.getCompositionText(),
                    product.getCategory(),
                    product.getCookingNeed(),
                    product.getFlags().isVegan(),
                    product.getFlags().isGlutenFree(),
                    product.getFlags().isSugarFree(),
                    product.getCreatedAt(),
                    product.getUpdatedAt()
            );
            replaceProductPhotos(product);
        });
    }

    public void updateProduct(Product product) {
        transactionTemplate.executeWithoutResult(status -> {
            jdbcTemplate.update("""
                    update products
                    set name = ?, calories = ?, proteins = ?, fats = ?, carbs = ?, composition_text = ?,
                        category = ?, cooking_need = ?, vegan = ?, gluten_free = ?, sugar_free = ?, updated_at = ?
                    where id = ?
                    """,
                    product.getName(),
                    product.getCalories(),
                    product.getProteins(),
                    product.getFats(),
                    product.getCarbs(),
                    product.getCompositionText(),
                    product.getCategory(),
                    product.getCookingNeed(),
                    product.getFlags().isVegan(),
                    product.getFlags().isGlutenFree(),
                    product.getFlags().isSugarFree(),
                    product.getUpdatedAt(),
                    product.getId()
            );
            replaceProductPhotos(product);
        });
    }

    public void deleteProduct(String id) {
        jdbcTemplate.update("delete from products where id = ?", id);
    }

    public List<Dish> listDishes() {
        return jdbcTemplate.query("""
                select id, name, calories, proteins, fats, carbs,
                       draft_calories, draft_proteins, draft_fats, draft_carbs, portion_size, category,
                       vegan, gluten_free, sugar_free, available_vegan, available_gluten_free, available_sugar_free,
                       created_at, updated_at
                from dishes
                order by created_at, id
                """, (rs, rowNum) -> {
            Dish dish = new Dish();
            String id = rs.getString("id");
            dish.setId(id);
            dish.setName(rs.getString("name"));
            dish.setPhotos(listDishPhotos(id));
            dish.setCalories(rs.getDouble("calories"));
            dish.setProteins(rs.getDouble("proteins"));
            dish.setFats(rs.getDouble("fats"));
            dish.setCarbs(rs.getDouble("carbs"));
            dish.setNutritionDraft(new Nutrition(
                    rs.getDouble("draft_calories"),
                    rs.getDouble("draft_proteins"),
                    rs.getDouble("draft_fats"),
                    rs.getDouble("draft_carbs")
            ));
            dish.setComposition(listIngredients(id));
            dish.setPortionSize(rs.getDouble("portion_size"));
            dish.setCategory(rs.getString("category"));
            dish.setFlags(new Flags(rs.getBoolean("vegan"), rs.getBoolean("gluten_free"), rs.getBoolean("sugar_free")));
            dish.setAvailableFlags(new Flags(
                    rs.getBoolean("available_vegan"),
                    rs.getBoolean("available_gluten_free"),
                    rs.getBoolean("available_sugar_free")
            ));
            dish.setCreatedAt(rs.getString("created_at"));
            dish.setUpdatedAt(rs.getString("updated_at"));
            return dish;
        });
    }

    public Optional<Dish> findDish(String id) {
        return listDishes().stream()
                .filter(dish -> dish.getId().equals(id))
                .findFirst();
    }

    public void insertDish(Dish dish) {
        transactionTemplate.executeWithoutResult(status -> {
            insertDishRow(dish);
            replaceDishPhotos(dish);
            replaceIngredients(dish);
        });
    }

    public void updateDish(Dish dish) {
        transactionTemplate.executeWithoutResult(status -> {
            jdbcTemplate.update("""
                    update dishes
                    set name = ?, calories = ?, proteins = ?, fats = ?, carbs = ?,
                        draft_calories = ?, draft_proteins = ?, draft_fats = ?, draft_carbs = ?,
                        portion_size = ?, category = ?, vegan = ?, gluten_free = ?, sugar_free = ?,
                        available_vegan = ?, available_gluten_free = ?, available_sugar_free = ?, updated_at = ?
                    where id = ?
                    """,
                    dish.getName(),
                    dish.getCalories(),
                    dish.getProteins(),
                    dish.getFats(),
                    dish.getCarbs(),
                    dish.getNutritionDraft().getCalories(),
                    dish.getNutritionDraft().getProteins(),
                    dish.getNutritionDraft().getFats(),
                    dish.getNutritionDraft().getCarbs(),
                    dish.getPortionSize(),
                    dish.getCategory(),
                    dish.getFlags().isVegan(),
                    dish.getFlags().isGlutenFree(),
                    dish.getFlags().isSugarFree(),
                    dish.getAvailableFlags().isVegan(),
                    dish.getAvailableFlags().isGlutenFree(),
                    dish.getAvailableFlags().isSugarFree(),
                    dish.getUpdatedAt(),
                    dish.getId()
            );
            replaceDishPhotos(dish);
            replaceIngredients(dish);
        });
    }

    public void deleteDish(String id) {
        jdbcTemplate.update("delete from dishes where id = ?", id);
    }

    private void insertDishRow(Dish dish) {
        jdbcTemplate.update("""
                insert into dishes (
                    id, name, calories, proteins, fats, carbs,
                    draft_calories, draft_proteins, draft_fats, draft_carbs, portion_size, category,
                    vegan, gluten_free, sugar_free, available_vegan, available_gluten_free, available_sugar_free,
                    created_at, updated_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                dish.getId(),
                dish.getName(),
                dish.getCalories(),
                dish.getProteins(),
                dish.getFats(),
                dish.getCarbs(),
                dish.getNutritionDraft().getCalories(),
                dish.getNutritionDraft().getProteins(),
                dish.getNutritionDraft().getFats(),
                dish.getNutritionDraft().getCarbs(),
                dish.getPortionSize(),
                dish.getCategory(),
                dish.getFlags().isVegan(),
                dish.getFlags().isGlutenFree(),
                dish.getFlags().isSugarFree(),
                dish.getAvailableFlags().isVegan(),
                dish.getAvailableFlags().isGlutenFree(),
                dish.getAvailableFlags().isSugarFree(),
                dish.getCreatedAt(),
                dish.getUpdatedAt()
        );
    }

    private List<String> listProductPhotos(String productId) {
        return jdbcTemplate.queryForList("""
                select path
                from product_photos
                where product_id = ?
                order by position
                """, String.class, productId);
    }

    private List<String> listDishPhotos(String dishId) {
        return jdbcTemplate.queryForList("""
                select path
                from dish_photos
                where dish_id = ?
                order by position
                """, String.class, dishId);
    }

    private List<Ingredient> listIngredients(String dishId) {
        return jdbcTemplate.query("""
                select product_id, amount
                from dish_ingredients
                where dish_id = ?
                order by position
                """, (rs, rowNum) -> new Ingredient(rs.getString("product_id"), rs.getDouble("amount")), dishId);
    }

    private void replaceProductPhotos(Product product) {
        jdbcTemplate.update("delete from product_photos where product_id = ?", product.getId());
        for (int index = 0; index < product.getPhotos().size(); index++) {
            jdbcTemplate.update("""
                    insert into product_photos (product_id, position, path)
                    values (?, ?, ?)
                    """, product.getId(), index, product.getPhotos().get(index));
        }
    }

    private void replaceDishPhotos(Dish dish) {
        jdbcTemplate.update("delete from dish_photos where dish_id = ?", dish.getId());
        for (int index = 0; index < dish.getPhotos().size(); index++) {
            jdbcTemplate.update("""
                    insert into dish_photos (dish_id, position, path)
                    values (?, ?, ?)
                    """, dish.getId(), index, dish.getPhotos().get(index));
        }
    }

    private void replaceIngredients(Dish dish) {
        jdbcTemplate.update("delete from dish_ingredients where dish_id = ?", dish.getId());
        for (int index = 0; index < dish.getComposition().size(); index++) {
            Ingredient ingredient = dish.getComposition().get(index);
            jdbcTemplate.update("""
                    insert into dish_ingredients (dish_id, position, product_id, amount)
                    values (?, ?, ?, ?)
                    """, dish.getId(), index, ingredient.getProductId(), ingredient.getAmount());
        }
    }
}

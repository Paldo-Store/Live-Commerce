package com.live_commerce.product.inventory.infrastructure.repository;


import com.live_commerce.product.inventory.domain.model.Inventory;
import com.live_commerce.product.inventory.domain.repository.InventoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaInventoryRepository extends JpaRepository<Inventory, UUID>, InventoryRepository {

    Optional<Inventory> findByInventoryIdAndDeletedStatusFalse(UUID inventoryId);

    Optional<Inventory> findByProductIdAndDeletedStatusFalse(UUID productId);

    @Query("""
    SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END
    FROM Inventory i
    WHERE i.productId = :productId
      AND i.availableQuantity >= :quantity
      AND i.deletedStatus = false
""")
    boolean existsOrderableInventory(@Param("productId") UUID productId,
                                     @Param("quantity") int quantity);

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Inventory i
        SET i.quantity = i.quantity - :quantity,
            i.availableQuantity = i.availableQuantity - :quantity
        WHERE i.productId = :productId
          AND i.deletedStatus = false
          AND i.availableQuantity >= :quantity
    """)
    int decreaseInventoryAtomically(
            @Param("productId") UUID productId,
            @Param("quantity") int quantity
    );

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Inventory i
        SET i.quantity = i.quantity + :quantity,
            i.availableQuantity = i.availableQuantity + :quantity
        WHERE i.productId = :productId
          AND i.deletedStatus = false
    """)
    int increaseInventoryAtomically(
            @Param("productId") UUID productId,
            @Param("quantity") int quantity
    );

}

package com.supplysync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplysync.dto.request.InventoryAdjustRequest;
import com.supplysync.dto.request.InventoryTransferRequest;
import com.supplysync.entity.Category;
import com.supplysync.entity.Inventory;
import com.supplysync.entity.Product;
import com.supplysync.entity.User;
import com.supplysync.entity.Warehouse;
import com.supplysync.enums.TransactionType;
import com.supplysync.enums.UserRole;
import com.supplysync.repository.CategoryRepository;
import com.supplysync.repository.InventoryRepository;
import com.supplysync.repository.ProductRepository;
import com.supplysync.repository.UserRepository;
import com.supplysync.repository.WarehouseRepository;
import com.supplysync.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class InventoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User managerUser;
    private User procurementUser;
    private String managerToken;
    private String procurementToken;

    private Product product;
    private Warehouse sourceWarehouse;
    private Warehouse destWarehouse;

    @BeforeEach
    void setUp() {
        // Clear database
        inventoryRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        warehouseRepository.deleteAll();
        userRepository.deleteAll();

        // Seed Users
        managerUser = User.builder()
                .username("manager")
                .email("manager@test.com")
                .passwordHash("password")
                .fullName("Warehouse Manager")
                .role(UserRole.WAREHOUSE_MANAGER)
                .isActive(true)
                .build();
        userRepository.save(managerUser);
        managerToken = "Bearer " + jwtService.generateAccessToken(managerUser);

        procurementUser = User.builder()
                .username("procurement")
                .email("proc@test.com")
                .passwordHash("password")
                .fullName("Procurement Manager")
                .role(UserRole.PROCUREMENT_MANAGER)
                .isActive(true)
                .build();
        userRepository.save(procurementUser);
        procurementToken = "Bearer " + jwtService.generateAccessToken(procurementUser);

        // Seed Warehouses
        sourceWarehouse = Warehouse.builder()
                .warehouseCode("WH-SRC")
                .name("Source Warehouse")
                .location("Loc 1")
                .city("City 1")
                .state("State 1")
                .pincode("123456")
                .capacity(1000)
                .isActive(true)
                .build();
        warehouseRepository.save(sourceWarehouse);

        destWarehouse = Warehouse.builder()
                .warehouseCode("WH-DST")
                .name("Dest Warehouse")
                .location("Loc 2")
                .city("City 2")
                .state("State 2")
                .pincode("789012")
                .capacity(1000)
                .isActive(true)
                .build();
        warehouseRepository.save(destWarehouse);

        // Seed Category & Product
        Category category = Category.builder()
                .categoryCode("CAT-GEN")
                .name("General")
                .build();
        categoryRepository.save(category);

        product = Product.builder()
                .sku("SKU-PROD")
                .name("Test Product")
                .category(category)
                .unitPrice(BigDecimal.TEN)
                .unitOfMeasure("Units")
                .reorderLevel(10)
                .isActive(true)
                .build();
        productRepository.save(product);

        // Seed Inventory
        Inventory inventory = Inventory.builder()
                .product(product)
                .warehouse(sourceWarehouse)
                .quantityAvailable(100)
                .quantityReserved(0)
                .quantityDamaged(0)
                .build();
        inventoryRepository.save(inventory);
    }

    @Test
    void adjustInventory_shouldReturn200_forAuthorizedUser() throws Exception {
        InventoryAdjustRequest request = new InventoryAdjustRequest();
        request.setProductId(product.getId());
        request.setWarehouseId(sourceWarehouse.getId());
        request.setTransactionType(TransactionType.INBOUND);
        request.setQuantity(50);

        mockMvc.perform(post("/api/v1/inventory/adjust")
                .header("Authorization", managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void adjustInventory_shouldReturn403_forUnauthorizedRole() throws Exception {
        InventoryAdjustRequest request = new InventoryAdjustRequest();
        request.setProductId(product.getId());
        request.setWarehouseId(sourceWarehouse.getId());
        request.setTransactionType(TransactionType.INBOUND);
        request.setQuantity(50);

        mockMvc.perform(post("/api/v1/inventory/adjust")
                .header("Authorization", procurementToken) // PROCUREMENT_MANAGER role
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getLowStockAlerts_shouldReturn200_withListOfAlerts() throws Exception {
        // Update stock to trigger low stock
        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(product.getId(), sourceWarehouse.getId()).get();
        inventory.setQuantityAvailable(5); // Below level 10
        inventoryRepository.save(inventory);

        mockMvc.perform(get("/api/v1/inventory/low-stock")
                .header("Authorization", managerToken))
                .andExpect(status().isOk());
    }

    @Test
    void transferInventory_shouldReturn200_withValidRequest() throws Exception {
        InventoryTransferRequest request = new InventoryTransferRequest();
        request.setProductId(product.getId());
        request.setSourceWarehouseId(sourceWarehouse.getId());
        request.setDestinationWarehouseId(destWarehouse.getId());
        request.setQuantity(20);

        mockMvc.perform(post("/api/v1/inventory/transfer")
                .header("Authorization", managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}

package secure.project.secureProject.dto.response;

import lombok.Builder;
import lombok.Getter;
import secure.project.secureProject.domain.enums.OrderState;

import java.time.LocalDate;

@Getter
@Builder
public class OrderDto {
    private Long orderId;
    private String itemName;
    private OrderState orderState;
    private LocalDate createAt;
    private Integer orderItemTotalAmount;
    private String imageUrl;
    private Integer totalPrice;
}

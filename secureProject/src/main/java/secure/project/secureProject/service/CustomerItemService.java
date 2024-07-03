    package secure.project.secureProject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import secure.project.secureProject.domain.*;
import secure.project.secureProject.dto.request.BasketAddItemRequestDto;
import secure.project.secureProject.dto.request.CustomerOrderItemRequestDto;
import secure.project.secureProject.dto.response.*;
import secure.project.secureProject.domain.enums.OrderState;
import secure.project.secureProject.exception.ApiException;
import secure.project.secureProject.exception.ErrorDefine;
import secure.project.secureProject.repository.*;
import secure.project.secureProject.util.SecurityUtil;

import secure.project.secureProject.validator.InputValidator;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerItemService {

    private final CustomerItemRepository customerItemRepository;
    private final UserRepository userRepository;
    private final BasketRepository basketRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SecurityUtil securityUtil;

    public Map<String, Object> selectCustomerItem(Integer page, Integer size, String latest, String price,String searchName){

        if(searchName != null && InputValidator.lengthNotInRange(searchName, 0, 30)) {
            throw new ApiException(ErrorDefine.INVALID_ARGUMENT);
        }
        if(searchName != null && InputValidator.containsAnySpecialCharacter(searchName)){
            throw new ApiException(ErrorDefine.INVALID_ARGUMENT);
        }


        Sort sort = Sort.by(
                new Sort.Order(Sort.Direction.fromString(latest), "updateAt"),
                new Sort.Order(Sort.Direction.fromString(price), "itemPrice")
        );

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Item> selectItem = customerItemRepository.searchItemList(searchName, pageable);

        PageInfo pageInfo = PageInfo.builder()
                .currentPage(selectItem.getNumber() + 1)
                .totalPages(selectItem.getTotalPages())
                .pageSize(selectItem.getSize())
                .currentItems(selectItem.getNumberOfElements())
                .totalItems(selectItem.getTotalElements())
                .build();

        Map<String, Object> result = new HashMap<>();

        result.put("selectItem",selectItem.getContent().stream()
                .map(items-> ItemDto.builder()
                        .itemId(items.getId())
                        .itemName(items.getItemName())
                        .itemPrice(items.getItemPrice())
                        .itemAmount(items.getItemAmount())
                        .imageUrl(items.getImageUrl())
                        .build())
                .collect(Collectors.toList()));

        result.put("pageInfo",pageInfo);

        return result;
    }

    public Boolean addItemToBasket(BasketAddItemRequestDto basketAddItemRequestDto) {
        User user = userRepository.findByNickname(securityUtil.getCurrentUsername())
                .orElseThrow( () -> new ApiException(ErrorDefine.USER_NOT_FOUND));

        Item item = customerItemRepository.findById(basketAddItemRequestDto.getItemId())
                .orElseThrow(() -> new ApiException(ErrorDefine.ITEM_NOT_FOUND));

        if(basketRepository.existsByItemIdAndUserId(item, user)){
            System.err.println("sadfasdfasdf");
            Basket basket = basketRepository.findByItemIdAndUserId(item, user)
                            .orElseThrow(()->new ApiException(ErrorDefine.BASKET_IS_EMPTY));
            basket.updateAdminItemAmount(basketAddItemRequestDto.getItemAmount());
        } else {
            Basket basket = Basket.builder()
                    .basketItemAmount(basketAddItemRequestDto.getItemAmount())
                    .userId(user)
                    .itemId(item)
                    .build();
            basketRepository.save(basket);
        }
        return true;
    }

    public Long selectUserPoint() {

        User user = userRepository.findByNickname(securityUtil.getCurrentUsername())
                .orElseThrow(() -> new ApiException(ErrorDefine.USER_NOT_FOUND));

        return user.getPoint();
    }

    public Map<String, Object> selectBasketItem() {
        User finduser = userRepository.findByNickname(securityUtil.getCurrentUsername())
                .orElseThrow(()-> new ApiException(ErrorDefine.USER_NOT_FOUND));

        List<Basket> selectBasket = basketRepository.findBasketsByUserIdWithItem(finduser);

        if(selectBasket.isEmpty())
            throw new ApiException(ErrorDefine.BASKET_IS_EMPTY);

        Map<String, Object> result = new HashMap<>();

        result.put("selectbasket", selectBasket.stream()
                .map(baskets -> BasketDto.builder()
                        .basketId(baskets.getId())
                        .itemId(baskets.getItemId().getId())
                        .itemName(baskets.getItemId().getItemName())
                        .itemPrice(baskets.getItemId().getItemPrice())
                        .basketItemAmount(baskets.getBasketItemAmount())
                        .itemAmount(baskets.getItemId().getItemAmount())
                        .imageUrl(baskets.getItemId().getImageUrl())
                        .build())
                .collect(Collectors.toList()));

        return result;
    }

    public Boolean basketItemDelete(Long itemId) {
        Item item = customerItemRepository.findById(itemId)
                .orElseThrow(() -> new ApiException(ErrorDefine.ITEM_NOT_FOUND));

        User user = userRepository.findByNickname(securityUtil.getCurrentUsername())
                .orElseThrow(() -> new ApiException(ErrorDefine.USER_NOT_FOUND));

        Basket basket = basketRepository.findByItemIdAndUserId(item, user)
                .orElseThrow(() -> new ApiException(ErrorDefine.BAKSET_NOT_FOUND));

        basketRepository.delete(basket);

        return true;
    }

    public Boolean paymentItem(List<CustomerOrderItemRequestDto> customerOrderItemRequestDto) {
        int totalPrice = 0;
        int totalAmount = 0;

        for(CustomerOrderItemRequestDto customerOrderItemRequestDto1 : customerOrderItemRequestDto) {
            Item item = customerItemRepository.findById(customerOrderItemRequestDto1.getItemId())
                    .orElseThrow(() -> new ApiException(ErrorDefine.ITEM_NOT_FOUND));

            totalPrice += item.getItemPrice() * customerOrderItemRequestDto1.getBuyItemAmount();
            totalAmount += customerOrderItemRequestDto1.getBuyItemAmount();
            if(item.getItemAmount() < totalAmount)
                throw new ApiException(ErrorDefine.OVER_ITEM_AMOUNT);
            item.updateItemAmount(item.getItemAmount()-totalAmount);
        }

        User user = userRepository.findByNickname(securityUtil.getCurrentUsername())
                .orElseThrow(() -> new ApiException(ErrorDefine.USER_NOT_FOUND));
        user.updatePoint(user.getPoint() - totalPrice);

        Order order = Order.builder()
                .orderItemTotalAmount(totalAmount)
                .orderItemTotalPrice(totalPrice)
                .orderState(OrderState.READY)
                .createAt(LocalDate.now())
                .user(user)
                .build();
        orderRepository.save(order);

        for(CustomerOrderItemRequestDto customerOrderItemRequestDto1 : customerOrderItemRequestDto) {
            Item item = customerItemRepository.findById(customerOrderItemRequestDto1.getItemId())
                    .orElseThrow(()-> new ApiException(ErrorDefine.ITEM_NOT_FOUND));
            OrderItem orderItem = OrderItem.builder()
                    .orderItemAmount(customerOrderItemRequestDto1.getBuyItemAmount())
                    .order(order)
                    .item(item)
                    .build();
            orderItemRepository.save(orderItem);

            Basket deleteBasket = basketRepository.findByItemIdAndUserId(item,user)
                    .orElseThrow(() -> new ApiException(ErrorDefine.BASKET_IS_EMPTY));
            basketRepository.delete(deleteBasket);
        }

        return true;
    }

    public Map<String, Object> selectHistroyItem(Integer page, Integer size, String latest, String status) {
        User user1 = userRepository.findByNickname(securityUtil.getCurrentUsername())
                .orElseThrow(() -> new ApiException(ErrorDefine.USER_NOT_FOUND));

        Sort sort = Sort.by(
                new Sort.Order(Sort.Direction.fromString(latest), "createAt"),
                new Sort.Order(Sort.Direction.fromString(status), "orderState")
        );

        Pageable pageable = PageRequest.of(page,size,sort);
        Page<Order> selectOrder = orderRepository.searchOrderListByUserId(user1,pageable);

        PageInfo pageInfo = PageInfo.builder()
                .currentPage(selectOrder.getNumber() + 1)
                .totalPages(selectOrder.getTotalPages())
                .pageSize(selectOrder.getSize())
                .currentItems(selectOrder.getNumberOfElements())
                .totalItems(selectOrder.getTotalElements())
                .build();

        Map<String, Object> result = new HashMap<>();

        result.put("selectOrder", selectOrder.getContent().stream()
                .map(orders -> OrderDto.builder()
                        .orderId(orders.getId())
                        .itemName(orders.getOrderItems().get(0).getItem().getItemName())
                        .orderState(orders.getOrderState())
                        .createAt(orders.getCreateAt())
                        .imageUrl(orders.getOrderItems().get(0).getItem().getImageUrl())
                        .orderItemTotalAmount(orders.getOrderItemTotalAmount())
                        .totalPrice(orders.getOrderItemTotalPrice())
                        .build())
                .collect(Collectors.toList()));
        result.put("pageInfo",pageInfo);

        return result;
    }

    public Map<String, Object> selectHistoryDetailItem(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(ErrorDefine.ORDER_NOT_FOUND));
        List<OrderItem> selectHistoryDetail = orderItemRepository.findByOrder(order);

        if(selectHistoryDetail.isEmpty())
            throw new ApiException(ErrorDefine.ITEM_NOT_FOUND);

        Map<String, Object> result = new HashMap<>();

        result.put("selectHistoryDetail", selectHistoryDetail.stream()
                .map(orderItems -> HistoryDetailDto.builder()
                        .itemId(orderItems.getItem().getId())
                        .imageUrl(orderItems.getItem().getImageUrl())
                        .itemPrice(orderItems.getItem().getItemPrice())
                        .itemAmount(orderItems.getOrderItemAmount())
                        .itemName(orderItems.getItem().getItemName())
                        .build())
                .collect(Collectors.toList()));

        return result;
    }

    public Boolean refundOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(ErrorDefine.ORDER_NOT_FOUND));
        User user = userRepository.findByNickname(securityUtil.getCurrentUsername())
                .orElseThrow(() -> new ApiException(ErrorDefine.USER_NOT_FOUND));

        List<OrderItem> deleteOrderItem = orderItemRepository.findByOrder(order);

        if(deleteOrderItem.isEmpty())
            throw new ApiException(ErrorDefine.ORDER_NOT_FOUND);

        user.updatePoint(user.getPoint() + order.getOrderItemTotalPrice());
        order.updateOrderState(OrderState.REFUND);

        return true;
    }
 }

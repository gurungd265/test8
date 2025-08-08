package com.example.backend.service.cart;

import com.example.backend.dto.cart.AddCartItemDto;
import com.example.backend.dto.cart.CartDto;
import com.example.backend.dto.cart.CartItemOptionDto;
import com.example.backend.dto.cart.CartItemDto;
import com.example.backend.entity.cart.Cart;
import com.example.backend.entity.cart.CartItem;
import com.example.backend.entity.cart.CartItemOption;
import com.example.backend.entity.product.Product;
import com.example.backend.entity.product.ProductOption;
import com.example.backend.entity.user.User;

import com.example.backend.repository.cart.CartItemRepository;
import com.example.backend.repository.cart.CartRepository;
import com.example.backend.repository.product.ProductOptionRepository;
import com.example.backend.repository.product.ProductRepository;
import com.example.backend.repository.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public CartItem addProductToCart(String userEmail, String sessionId, AddCartItemDto addCartItemDto) {
        int quantity = addCartItemDto.getQuantity();
        if (quantity <= 0) {
            throw new IllegalArgumentException("追加する数量は1以上である必要があります。");
        }

        Cart cart = getOrCreateCart(userEmail, sessionId);
        Product product = productRepository.findById(addCartItemDto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("商品が見つかりません。"));

        List<CartItemOptionDto> optionDtos = addCartItemDto.getOptions();

        Optional<CartItem> existingItemOpt = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(addCartItemDto.getProductId())
                        && item.getDeletedAt() == null
                        && optionsMatch(item.getOptions(), optionDtos))
                .findFirst();

        int newQuantity = quantity;
        CartItem cartItem;

        if (existingItemOpt.isPresent()) {
            cartItem = existingItemOpt.get();
            newQuantity += cartItem.getQuantity();
            cartItem.setQuantity(newQuantity);
            cartItem.setPriceAtAddition(product.getDiscountPrice());
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setPriceAtAddition(product.getDiscountPrice());
            cart.addCartItem(cartItem);

            if (optionDtos != null && !optionDtos.isEmpty()) {
                for (CartItemOptionDto optionDto : optionDtos) {
                    ProductOption productOption = productOptionRepository.findById(optionDto.getProductOptionId())
                            .orElseThrow(() -> new EntityNotFoundException("Product option not found"));
                    CartItemOption option = new CartItemOption();
                    option.setCartItem(cartItem);
                    option.setProductOption(productOption);
                    option.setOptionValue(optionDto.getOptionValue());
                    cartItem.getOptions().add(option);
                }
            }
        }

        if (product.getStockQuantity() == null || product.getStockQuantity() < newQuantity) {
            throw new IllegalStateException("在庫切れです: " + product.getName());
        }

        cartItemRepository.save(cartItem);
        return cartItem;
    }

    private boolean optionsMatch(List<CartItemOption> existingOptions, List<CartItemOptionDto> newOptions) {
        if ((existingOptions == null || existingOptions.isEmpty()) && (newOptions == null || newOptions.isEmpty())) {
            return true;
        }
        if (existingOptions == null || newOptions == null) {
            return false;
        }
        if (existingOptions.size() != newOptions.size()) {
            return false;
        }

        List<String> existingOptionStrings = existingOptions.stream()
                .map(opt -> opt.getProductOption().getId() + ":" + opt.getOptionValue())
                .sorted()
                .collect(Collectors.toList());

        List<String> newOptionStrings = newOptions.stream()
                .map(opt -> opt.getProductOptionId() + ":" + opt.getOptionValue())
                .sorted()
                .collect(Collectors.toList());

        return existingOptionStrings.equals(newOptionStrings);
    }

    @Transactional
    public void mergeCarts(String userEmail, String sessionId) {
        if (userEmail == null || sessionId == null) {
            throw new IllegalArgumentException("ユーザーのメールアドレスとセッションIDは必須です");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません。"));

        Optional<Cart> userCartOpt = cartRepository.findByUserAndDeletedAtIsNull(user);
        Cart userCart;

        if (userCartOpt.isPresent()) {
            userCart = userCartOpt.get();
        } else {
            userCart = new Cart();
            userCart.setUser(user);
            cartRepository.save(userCart);
        }

        Cart sessionCart = cartRepository.findBySessionIdAndDeletedAtIsNull(sessionId)
                .orElse(null);

        if (sessionCart == null || sessionCart.getCartItems().stream().noneMatch(item -> item.getDeletedAt() == null)) {
            if (sessionCart != null) {
                sessionCart.setDeletedAt(LocalDateTime.now());
                cartRepository.save(sessionCart);
            }
            return;
        }

        for (CartItem sessionItem : sessionCart.getCartItems()) {
            if (sessionItem.getDeletedAt() != null) continue;

            Optional<CartItem> existingItemOpt = userCart.getCartItems().stream()
                    .filter(item -> item.getProduct().getId().equals(sessionItem.getProduct().getId())
                            && item.getDeletedAt() == null
                            && optionsMatch(item.getOptions(), sessionItem.getOptions().stream()
                            .map(opt -> CartItemOptionDto.builder()
                                    .productOptionId(opt.getProductOption().getId())
                                    .optionValue(opt.getOptionValue())
                                    .build())
                            .collect(Collectors.toList())))
                    .findFirst();

            if (existingItemOpt.isPresent()) {
                CartItem existingItem = existingItemOpt.get();
                existingItem.setQuantity(existingItem.getQuantity() + sessionItem.getQuantity());
                if (existingItem.getProduct().getStockQuantity() == null ||
                        existingItem.getProduct().getStockQuantity() < (existingItem.getQuantity())) {
                    throw new IllegalStateException("商品'" + existingItem.getProduct().getName() + "'の在庫が不足しているため、カートの結合に失敗しました。");
                }
                cartItemRepository.save(existingItem);
            } else {
                CartItem newCartItem = new CartItem();
                newCartItem.setCart(userCart);
                newCartItem.setProduct(sessionItem.getProduct());
                newCartItem.setQuantity(sessionItem.getQuantity());
                newCartItem.setPriceAtAddition(sessionItem.getPriceAtAddition());
                newCartItem.setAddedAt(LocalDateTime.now());
                cartItemRepository.save(newCartItem);

                for (CartItemOption sessionOption : sessionItem.getOptions()) {
                    CartItemOption newOption = new CartItemOption();
                    newOption.setCartItem(newCartItem);
                    newOption.setProductOption(sessionOption.getProductOption());
                    newOption.setOptionValue(sessionOption.getOptionValue());
                    newCartItem.getOptions().add(newOption);
                }
            }
        }

        sessionCart.setDeletedAt(LocalDateTime.now());
        cartRepository.save(sessionCart);
    }

    @Transactional
    public void softDeleteCartItemsByProductId(Long productId) {
        cartItemRepository.softDeleteByProductId(productId);
    }

    @Transactional(readOnly = true)
    public CartDto getCartByUserEmailOrSessionId(String userEmail, String sessionId) {
        Cart cart = getOrCreateCart(userEmail, sessionId);
        // 고객님의 기존 Repository 메서드를 사용하도록 수정
        List<CartItem> cartItemsWithChars = cartItemRepository.findByCartIdWithoptions(cart.getId());

        return convertToDto(cartItemsWithChars);
    }

    @Transactional
    public CartItem updateCartItemQuantity(String userEmail, String sessionId, Long cartItemId, int newQuantity) {
        Cart cart = getOrCreateCart(userEmail, sessionId);

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId) && item.getDeletedAt() == null)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("カートアイテムが見つかりません。"));

        if (newQuantity <= 0) {
            cartItem.setDeletedAt(LocalDateTime.now());
        } else {
            if (cartItem.getProduct().getStockQuantity() == null || cartItem.getProduct().getStockQuantity() < newQuantity) {
                throw new IllegalStateException("在庫切れです: " + cartItem.getProduct().getName());
            }
            cartItem.setQuantity(newQuantity);
        }

        cartItemRepository.save(cartItem);
        return cartItem;
    }

    @Transactional
    public void softDeleteCartItem(String userEmail, String sessionId, Long cartItemId) {
        Cart cart = getOrCreateCart(userEmail, sessionId);

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId) && item.getDeletedAt() == null)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("カートアイテムが見つかりません。"));
        cartItem.setDeletedAt(LocalDateTime.now());
        cartItemRepository.save(cartItem);
    }

    @Transactional
    public void softDeleteCartItemsBatch(String userEmail, String sessionId, List<Long> itemIds) {
        Cart cart;
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません。"));
            cart = cartRepository.findByUserAndDeletedAtIsNull(user)
                    .orElseThrow(() -> new IllegalArgumentException("ユーザーのカートが見つかりません。"));
        } else if (sessionId != null) {
            cart = cartRepository.findBySessionIdAndDeletedAtIsNull(sessionId)
                    .orElseThrow(() -> new IllegalArgumentException("セッションIDのカートが見つかりません。"));
        } else {
            throw new IllegalArgumentException("ユーザー情報またはセッションIDが必要です。");
        }

        // 추가된 findByCartAndIdInAndDeletedAtIsNull 메서드를 사용하여 활성 아이템만 조회
        List<CartItem> itemsToDelete = cartItemRepository.findByCartAndIdInAndDeletedAtIsNull(cart, itemIds);

        if (itemsToDelete.size() != itemIds.size()) {
            System.err.println("警告: リクエストされた一部のカートアイテムIDが見つかりませんでした。");
        }

        LocalDateTime now = LocalDateTime.now();
        for (CartItem item : itemsToDelete) {
            item.setDeletedAt(now);
            cartItemRepository.save(item);
        }
    }

    @Transactional
    public void softClearCart(String userEmail, String sessionId) {
        Cart cart = getOrCreateCart(userEmail, sessionId);
        if (cart.getDeletedAt() != null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (CartItem item : cart.getCartItems()) {
            if (item.getDeletedAt() == null) {
                item.setDeletedAt(now);
            }
        }
        cart.setDeletedAt(now);
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(String userEmail, String sessionId) {
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません。"));
            Optional<Cart> existingUserCart = cartRepository.findByUserAndDeletedAtIsNull(user);
            if (existingUserCart.isPresent()) {
                return existingUserCart.get();
            } else {
                Cart newCart = new Cart();
                newCart.setUser(user);
                return cartRepository.save(newCart);
            }
        } else if (sessionId != null) {
            return cartRepository.findBySessionIdAndDeletedAtIsNull(sessionId)
                    .orElseGet(() -> {
                        Cart newCart = new Cart();
                        newCart.setSessionId(sessionId);
                        return cartRepository.save(newCart);
                    });
        } else {
            throw new IllegalArgumentException("ユーザーのメールアドレスかセッションIDのいずれかを指定してください");
        }
    }

    private CartDto convertToDto(List<CartItem> cartItems) {
        if (cartItems.isEmpty()) {
            return new CartDto(
                    null,
                    null,
                    null,
                    null,
                    null,
                    List.of(),
                    0
            );
        }

        Cart cart = cartItems.get(0).getCart();
        List<CartItemDto> items = cartItems.stream()
                .filter(item -> item.getDeletedAt() == null)
                .map(this::convertToDto)  // 단일 CartItem 변환 메서드 재사용
                .toList();

        return new CartDto(
                cart.getId(),
                cart.getUser() != null ? cart.getUser().getId() : null,
                cart.getSessionId(),
                cart.getCreatedAt(),
                cart.getUpdatedAt(),
                items,
                items.size()
        );
    }

    private CartItemDto convertToDto(CartItem item) {
        return CartItemDto.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productPrice(item.getProduct().getPrice())
                .priceAtAddition(item.getPriceAtAddition())
                .productImageUrl(item.getProduct().getMainImageUrl())
                .quantity(item.getQuantity())
                .options(item.getOptions().stream()
                        .map(this::convertOptionToDto)
                        .toList())
                .build();
    }

    private CartItemOptionDto convertOptionToDto(CartItemOption option) {
        return CartItemOptionDto.builder()
                .id(option.getId())
                .productOptionId(option.getProductOption().getId())
                .optionName(option.getProductOption().getOptionName())
                .optionValue(option.getOptionValue())
                .build();
    }

    @Transactional(readOnly = true)
    public int getTotalCartItemCount(String userEmail, String sessionId) {
        Cart cart = getOrCreateCart(userEmail,sessionId);
        return (int) cart.getCartItems().stream().filter(item -> item.getDeletedAt() == null).count();
    }

    @Transactional(readOnly = true)
    public int getTotalCartItemCount(User user) {
        Optional<Cart> cartOpt = cartRepository.findByUserAndDeletedAtIsNull(user);
        if (cartOpt.isPresent()) {
            return (int) cartOpt.get().getCartItems().stream().filter(item -> item.getDeletedAt() == null).count();
        }
        return 0;
    }
}

package com.radovan.spring.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.radovan.spring.converter.TempConverter;
import com.radovan.spring.dto.CartItemDto;
import com.radovan.spring.entity.CartEntity;
import com.radovan.spring.entity.CartItemEntity;
import com.radovan.spring.entity.CustomerEntity;
import com.radovan.spring.entity.LoyaltyCardEntity;
import com.radovan.spring.repository.CartItemRepository;
import com.radovan.spring.repository.CartRepository;
import com.radovan.spring.service.CartItemService;
import com.radovan.spring.service.CartService;

@Service
@Transactional
public class CartItemServiceImpl implements CartItemService {

	@Autowired
	private CartItemRepository cartItemRepository;

	@Autowired
	private TempConverter tempConverter;

	@Autowired
	private CartService cartService;

	@Autowired
	private CartRepository cartRepository;

	@Override
	public CartItemDto addCartItem(CartItemDto cartItem) {
		// TODO Auto-generated method stub
		CartItemEntity cartItemEntity = tempConverter.cartItemDtoToEntity(cartItem);
		CartItemEntity storedItem = cartItemRepository.save(cartItemEntity);
		CartItemDto returnValue = tempConverter.cartItemEntityToDto(storedItem);
		return returnValue;
	}

	@Override
	public void removeCartItem(Integer cartId, Integer itemId) {
		// TODO Auto-generated method stub
		cartItemRepository.removeCartItem(itemId);
		cartItemRepository.flush();
		cartService.refreshCartState(cartId);

	}

	@Override
	public void eraseAllCartItems(Integer cartId) {
		// TODO Auto-generated method stub
		cartItemRepository.removeAllByCartId(cartId);
		cartItemRepository.flush();
		cartService.refreshCartState(cartId);
	}

	@Override
	public List<CartItemDto> listAllByCartId(Integer cartId) {
		// TODO Auto-generated method stub
		Optional<List<CartItemEntity>> cartItemsOpt = Optional.ofNullable(cartItemRepository.findAllByCartId(cartId));
		List<CartItemDto> returnValue = new ArrayList<>();

		if (!cartItemsOpt.isEmpty()) {
			cartItemsOpt.get().forEach((item) -> {
				CartItemDto itemDto = tempConverter.cartItemEntityToDto(item);
				returnValue.add(itemDto);
			});
		}

		return returnValue;
	}

	@Override
	public CartItemDto getCartItem(Integer id) {
		// TODO Auto-generated method stub
		Optional<CartItemEntity> cartItemOpt = cartItemRepository.findById(id);
		CartItemDto returnValue = null;
		if (cartItemOpt.isPresent()) {
			returnValue = tempConverter.cartItemEntityToDto(cartItemOpt.get());
		}

		return returnValue;
	}

	@Override
	public CartItemDto getCartItemByCartIdAndBookId(Integer cartId, Integer bookId) {
		// TODO Auto-generated method stub
		Optional<CartItemEntity> cartItemOpt = Optional
				.ofNullable(cartItemRepository.findByCartIdAndBookId(cartId, bookId));
		CartItemDto returnValue = null;
		if (cartItemOpt.isPresent()) {
			returnValue = tempConverter.cartItemEntityToDto(cartItemOpt.get());
		}

		return returnValue;
	}

	@Override
	public Integer getBookQuantity(Integer cartId) {
		// TODO Auto-generated method stub
		Integer returnValue = 0;
		Optional<Integer> bookQuantityOpt = Optional.ofNullable(cartItemRepository.findBookQuantity(cartId));
		if (bookQuantityOpt.isPresent()) {
			returnValue = bookQuantityOpt.get();
		}
		return returnValue;
	}

	@Override
	public void eraseAllByBookId(Integer bookId) {
		// TODO Auto-generated method stub
		cartItemRepository.removeAllByBookId(bookId);
		cartItemRepository.flush();

		Optional<List<CartEntity>> allCartsOpt = Optional.ofNullable(cartRepository.findAll());
		if (!allCartsOpt.isEmpty()) {
			allCartsOpt.get().forEach((cartEntity) -> {
				cartService.refreshCartState(cartEntity.getCartId());
			});
		}
	}

	@Override
	public Boolean hasDiscount(Integer itemId) {
		// TODO Auto-generated method stub
		Boolean returnValue = false;
		Optional<CartItemEntity> itemOpt = cartItemRepository.findById(itemId);
		if (itemOpt.isPresent()) {
			CartEntity cartEntity = itemOpt.get().getCart();
			CustomerEntity customerEntity = cartEntity.getCustomer();
			Optional<LoyaltyCardEntity> loyaltyCardOpt = Optional.ofNullable(customerEntity.getLoyaltyCard());
			if (loyaltyCardOpt.isPresent()) {
				Integer discount = loyaltyCardOpt.get().getDiscount();
				if (discount > 0) {
					returnValue = true;
				}
			}
		}

		return returnValue;
	}

}

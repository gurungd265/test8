package com.example.backend.service;

import com.example.backend.dto.payment.CardRegistrationRequestDto;
import com.example.backend.entity.payment.Card;
import com.example.backend.repository.payment.CardRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    public Optional<Card> getCardInfo(String userId) {
        return cardRepository.findByUserId(userId);
    }

    @Transactional
    public Card registerCard(CardRegistrationRequestDto requestDto) {
        Optional<Card> existingCard = cardRepository.findByUserId(requestDto.getUserId());
        if (existingCard.isPresent()) {
            throw new IllegalStateException("すでに登録されているクレジットカードがあります。");
        }

        Card newCard = new Card();
        newCard.setUserId(requestDto.getUserId());
        newCard.setCardCompanyName(requestDto.getCardCompanyName());
        String maskedCardNumber = "XXXX-XXXX-XXXX-" + requestDto.getCardNumber().substring(12);
        newCard.setMaskedCardNumber(maskedCardNumber);
        newCard.setCardHolderName(requestDto.getCardHolderName());
        newCard.setExpiryDate(requestDto.getExpiryDate());

        return cardRepository.save(newCard);
    }
}
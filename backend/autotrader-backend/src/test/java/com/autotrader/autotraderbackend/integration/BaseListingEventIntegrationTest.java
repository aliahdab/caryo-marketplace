package com.autotrader.autotraderbackend.integration;

import com.autotrader.autotraderbackend.mapper.CarListingMapper;
import com.autotrader.autotraderbackend.model.CarListing;
import com.autotrader.autotraderbackend.model.User;
import com.autotrader.autotraderbackend.repository.CarListingRepository;
import com.autotrader.autotraderbackend.repository.UserRepository;
import com.autotrader.autotraderbackend.service.CarListingStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public abstract class BaseListingEventIntegrationTest {

    @InjectMocks
    protected CarListingStatusService carListingStatusService;

    @Mock
    protected ApplicationEventPublisher eventPublisher;

    @Mock
    protected UserRepository userRepository;

    @Mock
    protected CarListingRepository carListingRepository;

    @Mock
    protected CarListingMapper carListingMapper;

    protected CarListing mockListing;
    protected User mockUser;

    @BeforeEach
    public void setUpBase() {
        // Setup test user
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");

        // Setup test listing
        mockListing = new CarListing();
        mockListing.setId(1L);
        mockListing.setTitle("Test Listing");
        mockListing.setSeller(mockUser);
        mockListing.setArchived(false);

        // Setup repository mocks
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(carListingRepository.findById(mockListing.getId())).thenReturn(Optional.of(mockListing));
        when(carListingRepository.save(any(CarListing.class))).thenAnswer(i -> i.getArgument(0));
    }
}

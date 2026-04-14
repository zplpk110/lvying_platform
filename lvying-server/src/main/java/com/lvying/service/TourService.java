package com.lvying.service;

import com.lvying.domain.*;
import com.lvying.repo.IncomeRepository;
import com.lvying.repo.TourGuestRepository;
import com.lvying.repo.TourRepository;
import com.lvying.repo.UserRepository;
import com.lvying.web.dto.*;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TourService {

  private final TourRepository tourRepository;
  private final UserRepository userRepository;
  private final IncomeRepository incomeRepository;
  private final TourGuestRepository tourGuestRepository;
  private final FundService fundService;

  @Transactional
  public TourDetailResponse create(CreateTourRequest req) {
    BigDecimal revenue =
        BigDecimal.valueOf(req.guestCount()).multiply(req.pricePerGuest());
    BigDecimal redline = computeRedline(req, revenue);
    User sales = req.salesUserId() != null ? userRepository.getReferenceById(req.salesUserId()) : null;
    Tour tour =
        Tour.builder()
            .tourCode(req.tourCode())
            .name(req.name())
            .departureDate(req.departureDate())
            .guestCount(req.guestCount())
            .pricePerGuest(req.pricePerGuest())
            .budgetRedline(redline)
            .grossMarginPct(req.grossMarginPct() != null ? req.grossMarginPct() : new BigDecimal("25"))
            .salesUser(sales)
            .status(TourStatus.IN_PROGRESS)
            .build();
    tour = tourRepository.save(tour);
    if (req.copyFromTourId() != null) {
      tourRepository
          .findById(req.copyFromTourId())
          .ifPresent(
              src -> {
                tour.setBudgetRedline(src.getBudgetRedline());
                tour.setGrossMarginPct(src.getGrossMarginPct());
                tour.setCommissionRate(src.getCommissionRate());
                tourRepository.save(tour);
              });
    }
    return getDetail(tour.getId());
  }

  private BigDecimal computeRedline(CreateTourRequest req, BigDecimal revenue) {
    if (req.budgetRedline() != null) return req.budgetRedline();
    if (req.grossMarginPct() != null) {
      BigDecimal gm = req.grossMarginPct().divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP);
      return revenue.multiply(BigDecimal.ONE.subtract(gm));
    }
    return revenue.multiply(new BigDecimal("0.75"));
  }

  @Transactional(readOnly = true)
  public List<TourListItem> listInProgress() {
    return tourRepository.findByStatusOrderByDepartureDateAsc(TourStatus.IN_PROGRESS).stream()
        .map(
            t ->
                new TourListItem(
                    t.getId(),
                    t.getTourCode(),
                    t.getName(),
                    t.getDepartureDate(),
                    t.getStatus(),
                    t.getSalesUser() != null ? t.getSalesUser().getName() : null))
        .toList();
  }

  @Transactional(readOnly = true)
  public TourDetailResponse getDetail(UUID id) {
    Tour tour =
        tourRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("团不存在"));
    return toDetail(tour);
  }

  @Transactional
  public TourDetailResponse recordIncome(UUID tourId, RecordIncomeRequest req) {
    Tour tour =
        tourRepository.findById(tourId).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("团不存在"));
    incomeRepository.save(
        Income.builder()
            .tour(tour)
            .amount(req.amount())
            .type(req.type())
            .note(req.note())
            .build());
    return getDetail(tourId);
  }

  @Transactional
  public TourDetailResponse addGuest(UUID tourId, AddGuestRequest req) {
    Tour tour =
        tourRepository.findById(tourId).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("团不存在"));
    tourGuestRepository.save(
        TourGuest.builder()
            .tour(tour)
            .name(req.name())
            .phoneMasked(req.phoneMasked())
            .phoneRaw(req.phoneRaw())
            .balanceDue(req.balanceDue() != null ? req.balanceDue() : BigDecimal.ZERO)
            .build());
    return getDetail(tourId);
  }

  @Transactional
  public TourDetailResponse settle(UUID tourId) {
    Tour tour = tourRepository.getReferenceById(tourId);
    tour.setStatus(TourStatus.SETTLED);
    tourRepository.save(tour);
    return getDetail(tourId);
  }

  private TourDetailResponse toDetail(Tour tour) {
    UUID id = tour.getId();
    BigDecimal totalReceivable =
        fundService.tourExpectedRevenue(tour.getGuestCount(), tour.getPricePerGuest());
    BigDecimal incomeSum = fundService.tourIncomeSum(id);
    BigDecimal paid = fundService.tourPaidCost(id);
    BigDecimal pend = fundService.tourPendingStaffCost(id);
    BigDecimal est = paid.add(pend);
    BigDecimal net = incomeSum.subtract(est);
    BigDecimal deposit = nz(incomeRepository.sumAmountByTourIdAndType(id, IncomeType.DEPOSIT));
    BigDecimal balance = nz(incomeRepository.sumAmountByTourIdAndType(id, IncomeType.BALANCE));
    BigDecimal tail = totalReceivable.subtract(incomeSum);
    BigDecimal redline = tour.getBudgetRedline();
    BigDecimal surplus = redline.subtract(est);
    BigDecimal rate = tour.getCommissionRate();
    BigDecimal commission =
        net.compareTo(BigDecimal.ZERO) > 0
            ? net.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

    LoginResponse.UserInfo sales = null;
    if (tour.getSalesUser() != null) {
      User u = tour.getSalesUser();
      sales = new LoginResponse.UserInfo(u.getId(), u.getName(), u.getPhone(), u.getRole());
    }

    var finance =
        new TourDetailResponse.Finance(
            totalReceivable.toPlainString(),
            incomeSum.toPlainString(),
            deposit.toPlainString(),
            balance.toPlainString(),
            tail.toPlainString(),
            redline.toPlainString(),
            paid.toPlainString(),
            pend.toPlainString(),
            est.toPlainString(),
            net.toPlainString(),
            surplus.toPlainString(),
            rate.toPlainString(),
            commission.toPlainString());

    return new TourDetailResponse(
        tour.getId(),
        tour.getTourCode(),
        tour.getName(),
        tour.getStatus(),
        tour.getDepartureDate(),
        tour.getGuestCount(),
        tour.getPricePerGuest(),
        tour.getBudgetRedline(),
        tour.getGrossMarginPct(),
        sales,
        tour.getCommissionRate(),
        finance);
  }

  private static BigDecimal nz(BigDecimal v) {
    return v == null ? BigDecimal.ZERO : v;
  }
}

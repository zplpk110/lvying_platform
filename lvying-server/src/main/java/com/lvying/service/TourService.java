package com.lvying.service;

import com.lvying.domain.*;
import com.lvying.mapper.IncomeMapper;
import com.lvying.mapper.TourGuestMapper;
import com.lvying.mapper.TourMapper;
import com.lvying.mapper.UserMapper;
import com.lvying.web.dto.*;
import com.lvying.web.error.BusinessException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 团期业务：新建/列表/详情、记收款、加游客、封团。
 *
 * <p>详情 {@link #getDetail(java.util.UUID)} 组装 {@link com.lvying.web.dto.TourDetailResponse}，其中财务块由 {@link
 * FundService} 聚合，与 PRD「财务控制中心」三栏数据一致。
 */
@Service
@RequiredArgsConstructor
public class TourService {

  private final TourMapper tourMapper;
  private final UserMapper userMapper;
  private final IncomeMapper incomeMapper;
  private final TourGuestMapper tourGuestMapper;
  private final FundService fundService;

  /**
   * 新建团期；若带 {@code copyFromTourId} 则复制源团的毛利率、红线、提成等参数。
   *
   * <p>红线优先取请求中的 {@code budgetRedline}；否则按毛利率从应收推算；再否则默认按应收的 75% 作为成本上限。
   */
  @Transactional
  public TourDetailResponse create(CreateTourRequest req) {
    BigDecimal revenue =
        BigDecimal.valueOf(req.guestCount()).multiply(req.pricePerGuest());
    BigDecimal redline = computeRedline(req, revenue);
    LocalDateTime now = LocalDateTime.now();
    Tour tour =
        Tour.builder()
            .id(UUID.randomUUID())
            .tourCode(req.tourCode())
            .name(req.name())
            .departureDate(req.departureDate())
            .guestCount(req.guestCount())
            .pricePerGuest(req.pricePerGuest())
            .budgetRedline(redline)
            .grossMarginPct(req.grossMarginPct() != null ? req.grossMarginPct() : new BigDecimal("25"))
            .salesUserId(req.salesUserId())
            .status(TourStatus.IN_PROGRESS)
            .commissionRate(new BigDecimal("15"))
            .createdAt(now)
            .updatedAt(now)
            .build();
    tourMapper.insert(tour);
    if (req.copyFromTourId() != null) {
      Tour src = tourMapper.selectById(req.copyFromTourId());
      if (src != null) {
        tour.setBudgetRedline(src.getBudgetRedline());
        tour.setGrossMarginPct(src.getGrossMarginPct());
        tour.setCommissionRate(src.getCommissionRate());
        tour.setUpdatedAt(LocalDateTime.now());
        tourMapper.update(tour);
      }
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

  /** 进行中团列表（老板首页看板、团队列表页）。 */
  @Transactional(readOnly = true)
  public List<TourListItem> listInProgress() {
    return tourMapper.selectInProgressWithSalesName().stream()
        .map(
            row ->
                new TourListItem(
                    row.getId(),
                    row.getTourCode(),
                    row.getName(),
                    row.getDepartureDate(),
                    row.getStatus(),
                    row.getSalesName()))
        .toList();
  }

  /** 团详情 + 财务块（应收、已收、尾欠、成本、净利、提成预估）。 */
  @Transactional(readOnly = true)
  public TourDetailResponse getDetail(UUID id) {
    Tour tour = tourMapper.selectById(id);
    if (tour == null) {
      throw new BusinessException("NOT_FOUND", "团不存在");
    }
    return toDetail(tour);
  }

  /** 记一笔收款，写入 {@link com.lvying.domain.Income}。 */
  @Transactional
  public TourDetailResponse recordIncome(UUID tourId, RecordIncomeRequest req) {
    if (tourMapper.selectById(tourId) == null) {
      throw new BusinessException("NOT_FOUND", "团不存在");
    }
    LocalDateTime now = LocalDateTime.now();
    incomeMapper.insert(
        Income.builder()
            .id(UUID.randomUUID())
            .tourId(tourId)
            .amount(req.amount())
            .type(req.type())
            .note(req.note())
            .receivedAt(now)
            .createdAt(now)
            .build());
    return getDetail(tourId);
  }

  /** 添加游客，用于尾款催收名单与欠费明细。 */
  @Transactional
  public TourDetailResponse addGuest(UUID tourId, AddGuestRequest req) {
    if (tourMapper.selectById(tourId) == null) {
      throw new BusinessException("NOT_FOUND", "团不存在");
    }
    LocalDateTime now = LocalDateTime.now();
    tourGuestMapper.insert(
        TourGuest.builder()
            .id(UUID.randomUUID())
            .tourId(tourId)
            .name(req.name())
            .phoneMasked(req.phoneMasked())
            .phoneRaw(req.phoneRaw())
            .balanceDue(req.balanceDue() != null ? req.balanceDue() : BigDecimal.ZERO)
            .createdAt(now)
            .build());
    return getDetail(tourId);
  }

  /** 申请封团：状态置为 {@link TourStatus#SETTLED}（仅老板接口层控制）。 */
  @Transactional
  public TourDetailResponse settle(UUID tourId) {
    Tour tour = tourMapper.selectById(tourId);
    if (tour == null) {
      throw new BusinessException("NOT_FOUND", "团不存在");
    }
    tour.setStatus(TourStatus.SETTLED);
    tour.setUpdatedAt(LocalDateTime.now());
    tourMapper.update(tour);
    return getDetail(tourId);
  }

  /** 将实体 {@link Tour} 转为 API详情 DTO，并计算 finance 子对象。 */
  private TourDetailResponse toDetail(Tour tour) {
    UUID id = tour.getId();
    BigDecimal totalReceivable =
        fundService.tourExpectedRevenue(tour.getGuestCount(), tour.getPricePerGuest());
    BigDecimal incomeSum = fundService.tourIncomeSum(id);
    BigDecimal paid = fundService.tourPaidCost(id);
    BigDecimal pend = fundService.tourPendingStaffCost(id);
    BigDecimal est = paid.add(pend);
    BigDecimal net = incomeSum.subtract(est);
    BigDecimal deposit =
        nz(incomeMapper.sumAmountByTourIdAndType(id, IncomeType.DEPOSIT.name()));
    BigDecimal balance =
        nz(incomeMapper.sumAmountByTourIdAndType(id, IncomeType.BALANCE.name()));
    BigDecimal tail = totalReceivable.subtract(incomeSum);
    BigDecimal redline = tour.getBudgetRedline();
    BigDecimal surplus = redline.subtract(est);
    BigDecimal rate = tour.getCommissionRate();
    BigDecimal commission =
        net.compareTo(BigDecimal.ZERO) > 0
            ? net.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

    LoginResponse.UserInfo sales = null;
    if (tour.getSalesUserId() != null) {
      User u = userMapper.selectById(tour.getSalesUserId());
      if (u != null) {
        sales = new LoginResponse.UserInfo(u.getId(), u.getName(), u.getPhone(), u.getRole());
      }
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

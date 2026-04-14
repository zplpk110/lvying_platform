package com.lvying.service;

import com.lvying.domain.CollectionReminder;
import com.lvying.domain.Tour;
import com.lvying.domain.TourGuest;
import com.lvying.domain.TourStatus;
import com.lvying.domain.User;
import com.lvying.mapper.CollectionReminderMapper;
import com.lvying.mapper.TourGuestMapper;
import com.lvying.mapper.TourMapper;
import com.lvying.mapper.UserMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 尾款催收：欠收团列表、游客维度欠费；群发提醒 MVP 仅写 {@link com.lvying.domain.CollectionReminder}。
 */
@Service
@RequiredArgsConstructor
public class CollectionService {

  private final TourMapper tourMapper;
  private final TourGuestMapper tourGuestMapper;
  private final CollectionReminderMapper reminderMapper;
  private final UserMapper userMapper;
  private final FundService fundService;

  /** 仍有尾款未收齐的进行中团（按应收−已实收判断），附带游客列表。 */
  @Transactional(readOnly = true)
  public List<OverdueTour> overdueBalanceList() {
    List<Tour> tours =
        tourMapper.selectByStatusOrderByDepartureDate(TourStatus.IN_PROGRESS.name());
    List<OverdueTour> out = new ArrayList<>();
    for (Tour t : tours) {
      UUID id = t.getId();
      BigDecimal income = fundService.tourIncomeSum(id);
      BigDecimal receivable =
          BigDecimal.valueOf(t.getGuestCount()).multiply(t.getPricePerGuest());
      BigDecimal tail = receivable.subtract(income);
      if (tail.compareTo(BigDecimal.ZERO) <= 0) continue;
      List<TourGuest> guests = tourGuestMapper.selectByTourId(id);
      List<GuestRow> guestRows =
          guests.stream()
              .map(
                  g ->
                      new GuestRow(
                          g.getName(), g.getPhoneMasked(), g.getBalanceDue().toPlainString()))
              .toList();
      out.add(
          new OverdueTour(
              t.getId(),
              t.getTourCode(),
              t.getName(),
              t.getDepartureDate().toString(),
              tail.toPlainString(),
              resolveSalesName(t.getSalesUserId()),
              guestRows));
    }
    return out;
  }

  private String resolveSalesName(UUID salesUserId) {
    if (salesUserId == null) return null;
    User u = userMapper.selectById(salesUserId);
    return u != null ? u.getName() : null;
  }

  /**
   * 对指定团生成催收记录（演示）：按游客 {@code balanceDue} 写多条 {@link CollectionReminder}，无游客时按团总欠写一条。
   */
  @Transactional
  public BulkRemindResult bulkRemind(List<UUID> tourIds) {
    List<String> previews = new ArrayList<>();
    LocalDateTime now = LocalDateTime.now();
    for (UUID tourId : tourIds) {
      Tour t = tourMapper.selectById(tourId);
      if (t == null) continue;
      BigDecimal income = fundService.tourIncomeSum(tourId);
      BigDecimal receivable =
          BigDecimal.valueOf(t.getGuestCount()).multiply(t.getPricePerGuest());
      BigDecimal tail = receivable.subtract(income);
      List<TourGuest> guests = tourGuestMapper.selectByTourId(tourId);
      for (TourGuest g : guests) {
        if (g.getBalanceDue().compareTo(BigDecimal.ZERO) > 0) {
          String text =
              String.format(
                  "[旅盈旅行社] 尊敬的游客，您预订的%s行程即将开始，请您尽快支付尾款%s元，感谢支持。",
                  t.getName(), g.getBalanceDue().toPlainString());
          reminderMapper.insert(
              CollectionReminder.builder()
                  .id(UUID.randomUUID())
                  .tourId(t.getId())
                  .channel("SMS_STUB")
                  .payload(text)
                  .sentAt(now)
                  .build());
          previews.add(t.getTourCode() + " -> " + g.getName() + ": " + text);
        }
      }
      if (tail.compareTo(BigDecimal.ZERO) > 0 && guests.isEmpty()) {
        String text =
            String.format(
                "[旅盈旅行社] 尊敬的游客，您预订的%s行程即将开始，请您尽快支付尾款%s元，感谢支持。",
                t.getName(), tail.toPlainString());
        reminderMapper.insert(
            CollectionReminder.builder()
                .id(UUID.randomUUID())
                .tourId(t.getId())
                .channel("SMS_STUB")
                .payload(text)
                .sentAt(now)
                .build());
      }
    }
    return new BulkRemindResult(previews.size(), previews);
  }

  /** 仍有尾款未收齐的团及销售、游客欠费列表。 */
  public record OverdueTour(
      UUID tourId,
      String tourCode,
      String name,
      String departureDate,
      String tailRemaining,
      String salesName,
      List<GuestRow> guests) {}

  /** 游客维度欠费（展示用手机脱敏）。 */
  public record GuestRow(String name, String phoneMasked, String balanceDue) {}

  /** 催收演示结果：记录条数与话术预览列表。 */
  public record BulkRemindResult(int sent, List<String> previews) {}
}

package com.lvying.service;

import com.lvying.domain.CollectionReminder;
import com.lvying.domain.Tour;
import com.lvying.domain.TourStatus;
import com.lvying.repo.CollectionReminderRepository;
import com.lvying.repo.TourRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CollectionService {

  private final TourRepository tourRepository;
  private final CollectionReminderRepository reminderRepository;
  private final FundService fundService;

  @Transactional(readOnly = true)
  public List<OverdueTour> overdueBalanceList() {
    List<Tour> tours =
        tourRepository.findByStatusOrderByDepartureDateAsc(TourStatus.IN_PROGRESS);
    List<OverdueTour> out = new ArrayList<>();
    for (Tour t : tours) {
      UUID id = t.getId();
      BigDecimal income = fundService.tourIncomeSum(id);
      BigDecimal receivable =
          BigDecimal.valueOf(t.getGuestCount()).multiply(t.getPricePerGuest());
      BigDecimal tail = receivable.subtract(income);
      if (tail.compareTo(BigDecimal.ZERO) <= 0) continue;
      List<GuestRow> guests =
          t.getGuests().stream()
              .map(
                  g ->
                      new GuestRow(
                          g.getName(), g.getPhoneMasked(), g.getBalanceDue().toPlainString()))
              .toList();
      String salesName = t.getSalesUser() != null ? t.getSalesUser().getName() : null;
      out.add(
          new OverdueTour(
              t.getId(),
              t.getTourCode(),
              t.getName(),
              t.getDepartureDate().toString(),
              tail.toPlainString(),
              salesName,
              guests));
    }
    return out;
  }

  /** MVP：写库 + 返回预览，短信通道后续对接 */
  @Transactional
  public BulkRemindResult bulkRemind(List<UUID> tourIds) {
    List<String> previews = new ArrayList<>();
    for (UUID tourId : tourIds) {
      Tour t = tourRepository.findById(tourId).orElse(null);
      if (t == null) continue;
      BigDecimal income = fundService.tourIncomeSum(tourId);
      BigDecimal receivable =
          BigDecimal.valueOf(t.getGuestCount()).multiply(t.getPricePerGuest());
      BigDecimal tail = receivable.subtract(income);
      for (var g : t.getGuests()) {
        if (g.getBalanceDue().compareTo(BigDecimal.ZERO) > 0) {
          String text =
              String.format(
                  "[旅盈旅行社] 尊敬的游客，您预订的%s行程即将开始，请您尽快支付尾款%s元，感谢支持。",
                  t.getName(), g.getBalanceDue().toPlainString());
          reminderRepository.save(
              CollectionReminder.builder().tour(t).channel("SMS_STUB").payload(text).build());
          previews.add(t.getTourCode() + " -> " + g.getName() + ": " + text);
        }
      }
      if (tail.compareTo(BigDecimal.ZERO) > 0 && t.getGuests().isEmpty()) {
        String text =
            String.format(
                "[旅盈旅行社] 尊敬的游客，您预订的%s行程即将开始，请您尽快支付尾款%s元，感谢支持。",
                t.getName(), tail.toPlainString());
        reminderRepository.save(
            CollectionReminder.builder().tour(t).channel("SMS_STUB").payload(text).build());
      }
    }
    return new BulkRemindResult(previews.size(), previews);
  }

  public record OverdueTour(
      UUID tourId,
      String tourCode,
      String name,
      String departureDate,
      String tailRemaining,
      String salesName,
      List<GuestRow> guests) {}

  public record GuestRow(String name, String phoneMasked, String balanceDue) {}

  public record BulkRemindResult(int sent, List<String> previews) {}
}

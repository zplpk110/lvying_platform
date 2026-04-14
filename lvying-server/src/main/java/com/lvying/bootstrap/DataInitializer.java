package com.lvying.bootstrap;

import com.lvying.domain.*;
import com.lvying.repo.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

  private final UserRepository userRepository;
  private final TourRepository tourRepository;
  private final TourGuestRepository tourGuestRepository;
  private final IncomeRepository incomeRepository;
  private final ExpenseRepository expenseRepository;
  private final PasswordEncoder passwordEncoder;

  @Bean
  CommandLineRunner seed() {
    return args -> {
      if (userRepository.count() > 0) return;
      User boss =
          userRepository.save(
              User.builder()
                  .phone("13800000001")
                  .passwordHash(passwordEncoder.encode("demo123456"))
                  .name("王老板")
                  .role(UserRole.BOSS_FINANCE)
                  .bankName("招商银行")
                  .bankAccountLast4("8888")
                  .build());
      User zhang =
          userRepository.save(
              User.builder()
                  .phone("13800000002")
                  .passwordHash(passwordEncoder.encode("demo123456"))
                  .name("张三")
                  .role(UserRole.SALES_GUIDE)
                  .bankName("招商银行")
                  .bankAccountLast4("6666")
                  .build());
      Tour tour =
          tourRepository.save(
              Tour.builder()
                  .tourCode("HZ0420")
                  .name("杭州2日游")
                  .status(TourStatus.IN_PROGRESS)
                  .departureDate(LocalDate.of(2026, 4, 20))
                  .guestCount(20)
                  .pricePerGuest(new BigDecimal("1000"))
                  .budgetRedline(new BigDecimal("15000"))
                  .grossMarginPct(new BigDecimal("25"))
                  .salesUser(zhang)
                  .commissionRate(new BigDecimal("15"))
                  .build());
      tourGuestRepository.save(
          TourGuest.builder()
              .tour(tour)
              .name("王先生")
              .phoneMasked("139****0000")
              .phoneRaw("13900000000")
              .balanceDue(new BigDecimal("4000"))
              .build());
      incomeRepository.save(
          Income.builder()
              .tour(tour)
              .amount(new BigDecimal("6000"))
              .type(IncomeType.DEPOSIT)
              .build());
      incomeRepository.save(
          Income.builder()
              .tour(tour)
              .amount(new BigDecimal("10000"))
              .type(IncomeType.BALANCE)
              .build());
      expenseRepository.save(
          Expense.builder()
              .tour(tour)
              .amount(new BigDecimal("5000"))
              .category(ExpenseCategory.LODGING)
              .paymentMethod(PaymentMethod.COMPANY_ACCOUNT)
              .approvalStatus(ExpenseApprovalStatus.APPROVED)
              .payStatus(ExpensePayStatus.PAID)
              .approvedBy(boss)
              .approvedAt(java.time.Instant.now())
              .build());
      expenseRepository.save(
          Expense.builder()
              .tour(tour)
              .amount(new BigDecimal("65"))
              .category(ExpenseCategory.TRANSPORT)
              .paymentMethod(PaymentMethod.STAFF_ADVANCE)
              .staffUser(zhang)
              .approvalStatus(ExpenseApprovalStatus.APPROVED)
              .payStatus(ExpensePayStatus.UNPAID)
              .approvedBy(boss)
              .approvedAt(java.time.Instant.now())
              .build());
      expenseRepository.save(
          Expense.builder()
              .tour(tour)
              .amount(new BigDecimal("45"))
              .category(ExpenseCategory.DINING)
              .paymentMethod(PaymentMethod.STAFF_ADVANCE)
              .staffUser(zhang)
              .approvalStatus(ExpenseApprovalStatus.PENDING)
              .payStatus(ExpensePayStatus.UNPAID)
              .build());
    };
  }
}

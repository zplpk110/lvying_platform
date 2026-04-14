package com.lvying.bootstrap;

import com.lvying.domain.*;
import com.lvying.mapper.ExpenseMapper;
import com.lvying.mapper.IncomeMapper;
import com.lvying.mapper.TourGuestMapper;
import com.lvying.mapper.TourMapper;
import com.lvying.mapper.UserMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 开发/演示用种子数据：仅在用户表为空时插入老板、业务员与示例团（与前端演示账号一致）。
 *
 * <p>生产环境可关闭或改为 Flyway 迁移脚本。
 */
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

  private final UserMapper userMapper;
  private final TourMapper tourMapper;
  private final TourGuestMapper tourGuestMapper;
  private final IncomeMapper incomeMapper;
  private final ExpenseMapper expenseMapper;
  private final PasswordEncoder passwordEncoder;

  @Bean
  CommandLineRunner seed() {
    return args -> {
      if (userMapper.count() > 0) return;
      LocalDateTime now = LocalDateTime.now();
      UUID bossId = UUID.randomUUID();
      UUID zhangId = UUID.randomUUID();
      UUID tourId = UUID.randomUUID();
      userMapper.insert(
          User.builder()
              .id(bossId)
              .phone("13800000001")
              .passwordHash(passwordEncoder.encode("demo123456"))
              .name("王老板")
              .role(UserRole.BOSS_FINANCE)
              .bankName("招商银行")
              .bankAccountLast4("8888")
              .createdAt(now)
              .updatedAt(now)
              .build());
      userMapper.insert(
          User.builder()
              .id(zhangId)
              .phone("13800000002")
              .passwordHash(passwordEncoder.encode("demo123456"))
              .name("张三")
              .role(UserRole.SALES_GUIDE)
              .bankName("招商银行")
              .bankAccountLast4("6666")
              .createdAt(now)
              .updatedAt(now)
              .build());
      tourMapper.insert(
          Tour.builder()
              .id(tourId)
              .tourCode("HZ0420")
              .name("杭州2日游")
              .status(TourStatus.IN_PROGRESS)
              .departureDate(LocalDate.of(2026, 4, 20))
              .guestCount(20)
              .pricePerGuest(new BigDecimal("1000"))
              .budgetRedline(new BigDecimal("15000"))
              .grossMarginPct(new BigDecimal("25"))
              .salesUserId(zhangId)
              .commissionRate(new BigDecimal("15"))
              .createdAt(now)
              .updatedAt(now)
              .build());
      tourGuestMapper.insert(
          TourGuest.builder()
              .id(UUID.randomUUID())
              .tourId(tourId)
              .name("王先生")
              .phoneMasked("139****0000")
              .phoneRaw("13900000000")
              .balanceDue(new BigDecimal("4000"))
              .createdAt(now)
              .build());
      incomeMapper.insert(
          Income.builder()
              .id(UUID.randomUUID())
              .tourId(tourId)
              .amount(new BigDecimal("6000"))
              .type(IncomeType.DEPOSIT)
              .note(null)
              .receivedAt(now)
              .createdAt(now)
              .build());
      incomeMapper.insert(
          Income.builder()
              .id(UUID.randomUUID())
              .tourId(tourId)
              .amount(new BigDecimal("10000"))
              .type(IncomeType.BALANCE)
              .note(null)
              .receivedAt(now)
              .createdAt(now)
              .build());
      expenseMapper.insert(
          Expense.builder()
              .id(UUID.randomUUID())
              .tourId(tourId)
              .amount(new BigDecimal("5000"))
              .category(ExpenseCategory.LODGING)
              .paymentMethod(PaymentMethod.COMPANY_ACCOUNT)
              .staffUserId(null)
              .receiptImageUrl(null)
              .note(null)
              .approvalStatus(ExpenseApprovalStatus.APPROVED)
              .payStatus(ExpensePayStatus.PAID)
              .approvedById(bossId)
              .approvedAt(now)
              .batchPayRef(null)
              .createdAt(now)
              .updatedAt(now)
              .build());
      expenseMapper.insert(
          Expense.builder()
              .id(UUID.randomUUID())
              .tourId(tourId)
              .amount(new BigDecimal("65"))
              .category(ExpenseCategory.TRANSPORT)
              .paymentMethod(PaymentMethod.STAFF_ADVANCE)
              .staffUserId(zhangId)
              .receiptImageUrl(null)
              .note(null)
              .approvalStatus(ExpenseApprovalStatus.APPROVED)
              .payStatus(ExpensePayStatus.UNPAID)
              .approvedById(bossId)
              .approvedAt(now)
              .batchPayRef(null)
              .createdAt(now)
              .updatedAt(now)
              .build());
      expenseMapper.insert(
          Expense.builder()
              .id(UUID.randomUUID())
              .tourId(tourId)
              .amount(new BigDecimal("45"))
              .category(ExpenseCategory.DINING)
              .paymentMethod(PaymentMethod.STAFF_ADVANCE)
              .staffUserId(zhangId)
              .receiptImageUrl(null)
              .note(null)
              .approvalStatus(ExpenseApprovalStatus.PENDING)
              .payStatus(ExpensePayStatus.UNPAID)
              .approvedById(null)
              .approvedAt(null)
              .batchPayRef(null)
              .createdAt(now)
              .updatedAt(now)
              .build());
    };
  }
}

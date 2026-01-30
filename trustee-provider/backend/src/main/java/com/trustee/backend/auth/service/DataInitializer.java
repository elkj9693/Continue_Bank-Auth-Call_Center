package com.trustee.backend.auth.service;

import com.trustee.backend.auth.entity.CarrierUser;
import com.trustee.backend.auth.repository.CarrierUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CarrierUserRepository carrierUserRepository;

    public DataInitializer(CarrierUserRepository carrierUserRepository) {
        this.carrierUserRepository = carrierUserRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("[TRUSTEE-DB] Checking CarrierUser data...");

        List<CarrierUser> initialUsers = Arrays.asList(
                new CarrierUser("김중수", "01095119924", "SKT", "850101"),
                new CarrierUser("방수진", "01087176882", "KT", "900101"),
                new CarrierUser("김은수", "01051337437", "LGU+", "920101"),
                new CarrierUser("이광진", "01030659693", "ALDDLE", "880101"),
                new CarrierUser("임혜진", "01037315819", "SKT", "940101"),
                new CarrierUser("전용준", "01050470664", "KT", "820101"),
                new CarrierUser("김유진", "01092877379", "LGU+", "950101"),
                new CarrierUser("장민아", "01049328977", "SKT", "910101"),
                new CarrierUser("이승원", "01092128221", "KT", "890101"),
                new CarrierUser("홍길동", "01000000000", "SKT", "000101"),
                // Requested Dummy Data
                new CarrierUser("홍길순", "01000000001", "SKT", "010101"),
                new CarrierUser("고길동", "01000000002", "SKT", "750101"),
                new CarrierUser("차은우", "01011111111", "SKT", "970330"),
                // New Test Users (일영이 ~ 십영이) - All SKT as requested
                new CarrierUser("일영이", "01011111111", "SKT", "010101"),
                new CarrierUser("이영이", "01022222222", "SKT", "020202"),
                new CarrierUser("삼영이", "01033333333", "SKT", "030303"),
                new CarrierUser("사영이", "01044444444", "SKT", "040404"),
                new CarrierUser("오영이", "01055555555", "SKT", "050505"),
                new CarrierUser("육영이", "01066666666", "SKT", "060606"),
                new CarrierUser("칠영이", "01077777777", "SKT", "070707"),
                new CarrierUser("팔영이", "01088888888", "SKT", "080808"),
                new CarrierUser("구영이", "01099999999", "SKT", "090909"),
                new CarrierUser("십영이", "01010101010", "SKT", "101010"),
                // User Requested Test Data
                new CarrierUser("테스트", "01012345678", "SKT", "900101"));

        for (CarrierUser user : initialUsers) {
            if (carrierUserRepository.findByPhoneNumber(user.getPhoneNumber()).isEmpty()) {
                carrierUserRepository.save(user);
                System.out.println("[TRUSTEE-DB] Added missing user: " + user.getName());
            }
        }
    }
}

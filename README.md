# 이벤트 티켓팅 시스템

> 개발 기간 :   2024.11.15 ~ 2024.12.06 (3주)
>
>
> 개발 인원 :  1명 (개인 프로젝트)

## 1. 프로젝트 소개

> 이 프로젝트는 대량 트래픽을 효율적으로 처리하고, 동시에 요청이 몰리는 환경에서도 안정적인 예약이 가능한 **이벤트 예매 시스템**을 설계 및 구현한 개인 프로젝트입니다.
>
>
> 실시간 예매 환경에서 발생할 수 있는 문제를 해결하기 위해 Redis를 활용한 **대기열 관리와 동시성 제어**를 적용했습니다. 단일 서버 기반으로 최소한의 자원을 사용하면서도 효율적인 시스템을 구축하고자 했습니다.

---



## **2. 전체 플로우**

1. **사용자 회원가입 및 로그인**
   - Spring Security와 JWT를 활용하여 인증 및 권한 관리.
2. **컨텐츠 리스트 페이지 접근**
   - Redis 캐싱으로 빠른 응답 제공.
3. **컨텐츠별 좌석 리스트 조회**
   - Redis Hash 자료구조를 활용하여 좌석 정보를  조회.
4. **예매 요청 처리**
   - Redis를 통해 Sorted Set 기반 대기열 관리 및 락을 통한 동시성 제어.
     - @Scheduled를 통해 대기큐의 우선순위를 기반으로 작업큐(예매 가능 상태)로 이동
5. **예약 성공 처리**
   - Spring Event 기반으로 비동기적으로 이메일 티켓 발송.

---



## 3. 주요 기능

**대기열 관리**

- Redis의 Sorted Set 자료구조를 활용하여 대기열 요청 순서를 관리.
- INCR와 System.nanoTime()을 조합해 요청 순서와 우선순위를 보장.
- 대기 큐와 작업 큐를 나눠서 구현

**동시성 제어**

- Redis의 setIfAbsent를 활용하여 좌석 단위로 락 구현.
- 락의 TTL(Time-To-Live)을 설정해 잠금 상태를 관리.

**Redis 캐싱**

- 자주 변경되지 않을 컨텐츠 데이터를 캐싱하여 조회

**비동기 처리**

- Spring Event를 활용해 이메일 발송과 같은 부가 작업을 비동기적으로 처리하여 빠른 응답 제공.

---



## 4. 기술 스택

> **Back-End**: Spring Boot, Spring Security, JWT, Spring Data JPA, Redis, MySQL, jmeter

---



## 5. 테스트 및 검증

**JMeter를 활용한 성능 테스트[(자세히 보기)](https://www.notion.so/159af9762bda80059819c7480a3a3345?pvs=21)** 

1. **테스트 시나리오**
   - **3000명의 사용자**가 총 **30만 건의 요청**을 Redis 기반 대기열로 전송하며 성능 및 동시성 제어를 테스트.
   - 예약 성공, 대기열 처리, 락 동작 여부 등을 검증.
2. **결과 분석**
   - **TPS(초당 처리량)**: 초당 약 3000개의 요청을 처리하며 안정적인 성능을 확인.
   - **대기열 순서 보장**: 대기열 요청 순서가 정확히 유지됨을 확인.
   - **좌석 동시성 제어**: 동일 좌석에 대해 중복 예약이 발생하지 않음을 확인.
3. **성능 최적화**
   - 테스트 결과에 따라 Redis 대기열 TTL(Time-To-Live) 및 작업 큐 크기를 조정하여 **처리량(TPS)을 약 80% 향상**.

---



## 6. 회고록

### **대량 트래픽에서의 동시성 문제 해결**

좌석 예약 시 동시에 여러 사용자가 동일 좌석을 선택하는 문제가 발생했습니다. 이를 해결하기 위해 **Redis 기반의 락**을 도입하여 동시성 문제를 효과적으로 해결했습니다. 락 점유 시간에 따른 부하를 분석하고, **최적의 TTL 값을 설정**하는 과정에서 시스템 성능 개선의 중요성을 체감했습니다.

또한, 락 경합 상황에서의 성능 저하를 최소화하기 위해 대기열을 활용한 요청 순서 제어를 적용하며, 안정성과 효율성 간의 균형을 고민하게 되었습니다.

### **작업 큐 크기 조절**

TPS(초당 처리량)를 높이기 위해 **대기열과 작업 큐의 크기**를 늘리는 실험을 진행했지만, 예상과 달리 처리량이 줄어드는 현상을 관찰했습니다.

큐에 포함된 작업이 많아질수록 **사용자 간 경쟁**이 증가하고, **락 경합**으로 인해 요청 처리 속도가 느려졌습니다. 이를 통해 단순히 큐 크기를 키우는 것이 아니라, 적절한 큐 크기와 효율적인 자원 분배가 중요하다는 것을 배웠습니다.

### **협업의 중요성 체감**

혼자 프로젝트를 진행하면서, 데이터베이스 설계, API 최적화, 비동기 작업 설계 등 **다양한 측면에서 시스템의 유기적인 상호작용**이 얼마나 중요한지 깨달았습니다.

각 파트가 독립적으로 동작할 뿐 아니라, 전체 시스템에서 조화롭게 작동해야 안정적이고 확장 가능한 서비스를 만들 수 있음을 실감했습니다.

이 경험을 통해, 실제 팀 프로젝트에서는 **파트 간 소통과 협력의 중요성**을 더욱 중시하게 될 것입니다.

### **캐싱 활용에 대한 고민**

좌석 예약 시스템에서는 **좌석 상태의 정확성과 일관성**이 중요했습니다. 처음에는 Redis에 좌석 상태를 캐싱하고, 일정 주기로 DB에 반영하는 방식을 고민했지만 이 과정에서 동기화에 대한 복잡성 (잦은 수정에 따른 비효율성)을 겪었습니다. 그래서, 좌석 상태는 DB에서 직접 조회하고 업데이트하는 방식으로 관리하여 데이터 일관성을 보장했습니다.
대신, 수정 사항이 적고 조회 빈도가 높은 **컨텐츠 리스트**와 같은 데이터에만 캐싱을 활용했습니다.
이 과정에서 "조회 데이터의 양이 적은 경우에도 캐싱을 사용해야 할까?"라는 고민을 했습니다.

결론적으로, **데이터 크기가 작더라도 반복적인 조회 요청이 많다면 캐싱은 충분히 성능 개선에 기여**할 수 있다는 것을 배웠습니다.


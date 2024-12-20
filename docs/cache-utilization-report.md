## 캐시를 활용한 리팩토링 보고서

### 활용 범위
* [x] 유저별 데이터가 상이하지 않은 데이터 반환
* [ ] 예약 시 결제 처리에 대한 유효기한 설정 (TTL)

### 각 로직을 캐시를 활용한 로직 이관으로 성능을 개선한다면

1. 유저별 데이터가 상이하지 않은 데이터 반환
   * 아래 API에 대해서 적용 가능
     * 콘서트 정보 목록 API (`/api/1.0/concerts`)
     * 콘서트 스케줄 목록 API (`/api/1.0/concerts/{concertId}/schedules`)
   * 기존 로직은 매번 특정 요청에 대한 같은 정보를 `N`명의 유저에게 동일하게 반환했음
     * 어차피 같은 정보에 대해 매번 DB를 조회해야 함
     * 따라서 `N`명의 유저가 데이터를 요청하면 `N`회의 데이터 조회가 일어남
     * 그러면 DB의 부하는 커지고 그에 따라 서비스 자체가 느려질 수 있음
   * 그럼 캐시(Redis)를 도입하면 어떻게 성능을 개선할 수 있을까
     * 특정 조건 A라는 요청이 발생했다고 가정하면 (`page=1&sort=id...`)
     * A의 최초 한 번의 요청에 대해 DB로부터 데이터를 조회함
     * A에 대해 조회된 데이터를 캐시에 저장하고 해당 데이터를 반환함
     * 다음 요청 부터는 A 조건에 부합한다면 동일한 데이터를 캐시로부터 읽어들여 반환할 수 있음
     * DB의 부하를 줄이고 서비스를 좀 더 널널하게 운영할 수 있음
   * 현재 적용 완료

2. 예약 시 결제 처리에 대한 유효기한 설정 (TTL)
   * 아래 API에 대해서 적용 가능
     * 콘서트 좌석 목록 API (`/api/1.0/concerts/{concertId}/schedules/{concertScheduleId}/seats`)
     * 콘서트 좌석 예약 API (`/api/1.0/concerts/{concertId}/schedules/{concertScheduleId}/seats/{concertSeatId}/reservations/pending`)
     * 콘서트 좌석 결제 API (`/api/1.0/concerts/{concertId}/schedules/{concertScheduleId}/seats/{concertSeatId}/reservations/{reservationId}/payment`)
   * 기존 로직은 스케줄러를 사용하거나 매 유저 요청으로 결제 만료 상태를 확인해야 했음
     * 스케줄러를 사용하는 경우
       * 스케줄러가 돌아가는 매 요청으로 일괄처리 해야함
       * 스케줄러 간격에 따라 정확한 만료 시간에 제대로 만료 시키기 어려움
       * 스케줄러 자체에 장애가 발생하는 것에 대해 대비하기가 어려움
     * 매 유저 요청으로 만료 여부를 체크하는 경우
       * 목록 조회, 좌석 예약, 좌석 결제 시 모든 케이스에서 만료 여부를 체크해야 함
       * 매 요청 마다 예약 및 결제 테이블의 레코드에 락을 걸어야 함
       * 확실하지만 부하가 심하고 시스템 성능 저하를 초래할 수 있음
   * 그럼 캐시(Redis)를 도입하면 어떻게 성능을 개선할 수 있을까
     * 점유 상태가 아닌 좌석에 대해 예약을 하는 경우의 시나리오
       * 예약 요청 시 실제 DB에 예약 데이터를 저장하되, 예약 정보를 캐시에 함께 저장함
       * 캐시에는 `...:{scheduleId}:{seatId}` 형태의 키와 `{userId}`의 값을 입력하면서 결제 유효기한을 설정함 (5분)
     * 목록을 조회하는 경우의 시나리오
       * 먼저 요청에 대한 좌석 정보를 DB로 부터 전부 조회함
       * `...:{scheduleId}:*` 조건으로 해당 스케줄에 대한 키를 전부 가져옴
       * DB로부터 조회된 좌석 ID와 캐시로부터 조회된 좌석 ID를 대조해서 일치하는 값에 대해 점유된 상태를 업데이트함 (`active=false`)
       * 업데이트된 데이터를 반환함
     * 이미 점유된 좌석에 예약을 시도하는 경우의 시나리오
       * 캐시에 데이터가 존재할 경우
         * 곧바로 "이미 예약된 좌석"이라는 안내와 함께 튕겨냄
       * 캐시에 데이터가 없을 경우
         * 캐시의 정보만 삭제됐을 뿐, DB에 저장된 예약 데이터는 남아있으므로 DB상의 결제 유효기한에 관한 조건을 체크해야 함
         * DB 데이터에 유효기한이 아직 유효한 상태로 남아있을 경우
           * 장애 등의 이유로 캐시가 삭제된 케이스로 판단
           * 유효기한 까지의 시간 만큼을 만료 시간(TTL)으로 설정하여 캐시 데이터 재등록
         * DB 데이터에 유효기한이 지난 상태로 남아있는 경우
           * DB 데이터를 결제 취소 및 예약 취소 상태로 변경
           * 이후 **점유 상태가 아닌 좌석에 대해 예약을 하는 경우의 시나리오** 대로 진행
     * 점유한 좌석에 결제를 요청하는 경우
       * `...:{scheduleId}:{seatId}` 형태의 키로 값을 조회
       * 현재 내 `{userId}`와, 조회된 값의 `{userId}`가 일치하는지 비교하는 방법으로 비정상 접근에 대한 DB 조회 횟수를 줄일 수 있음
   * 실제 적용은 완료하지 못했음

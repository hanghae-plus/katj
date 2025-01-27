package com.hh2.katj.trip.service

import com.hh2.katj.taxi.model.ChargeType
import com.hh2.katj.taxi.model.FuelType
import com.hh2.katj.taxi.model.Taxi
import com.hh2.katj.taxi.repository.TaxiRepository
import com.hh2.katj.taxidriver.model.entity.TaxiDriver
import com.hh2.katj.taxidriver.model.entity.TaxiDriverStatus
import com.hh2.katj.taxidriver.repository.TaxiDriverRepository
import com.hh2.katj.trip.model.DepartureRoadAddress
import com.hh2.katj.trip.model.DestinationRoadAddress
import com.hh2.katj.trip.model.Trip
import com.hh2.katj.trip.model.TripStatus
import com.hh2.katj.trip.model.request.RequestTrip
import com.hh2.katj.trip.model.response.ResponseTrip
import com.hh2.katj.trip.repository.TripRepository
import com.hh2.katj.user.model.entity.User
import com.hh2.katj.user.model.entity.UserStatus
import com.hh2.katj.user.repository.UserRepository
import com.hh2.katj.util.annotation.KATJTestContainerE2E
import com.hh2.katj.util.exception.ExceptionMessage
import com.hh2.katj.util.exception.ExceptionMessage.ID_DOES_NOT_EXIST
import com.hh2.katj.util.model.BaseTestEntity
import com.hh2.katj.util.model.Gender
import com.hh2.katj.util.model.RoadAddress
import org.assertj.core.api.Assertions
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime

@KATJTestContainerE2E
class CallingServiceTest(
    private val callingService: CallingService,
    private val userRepository: UserRepository,
    private val taxiRepository: TaxiRepository,
    private val taxiDriverRepository: TaxiDriverRepository,
    private val tripRepository: TripRepository,
): BaseTestEntity() {

    lateinit var user: User
    lateinit var taxiDriver: TaxiDriver
    lateinit var roadAddress: RoadAddress
    var departure = DepartureRoadAddress(
        departureAddressName = "address_name",
        departureRegion1depthName = "r_1",
        departureRegion2depthName = "r_2",
        departureRegion3depthName = "r_3",
        departureRoadName = "road_name",
        departureUndergroundYn = "Y",
        departureMainBuildingNo = "1",
        departureSubBuildingNo = "2",
        departureBuildingName = "bn",
        departureZoneNo = "11232",
        departureLongitude = "x.123",
        departureLatitude = "y.321",
    )
    var destination = DestinationRoadAddress(
        destinationAddressName = "address_name",
        destinationRegion1depthName = "r_1",
        destinationRegion2depthName = "r_2",
        destinationRegion3depthName = "r_3",
        destinationRoadName = "road_name",
        destinationUndergroundYn = "Y",
        destinationMainBuildingNo = "1",
        destinationSubBuildingNo = "2",
        destinationBuildingName = "bn",
        destinationZoneNo = "11232",
        destinationLongitude = "x.123",
        destinationLatitude = "y.321",
    )

    @BeforeEach
    fun setUp() {
        roadAddress = RoadAddress(
            addressName = "address_name",
            region1depthName = "r_1",
            region2depthName = "r_2",
            region3depthName = "r_3",
            roadName = "road_name",
            undergroundYn = "Y",
            mainBuildingNo = "1",
            subBuildingNo = "2",
            buildingName = "bn",
            zoneNo = "11232",
            longitude = "x.123",
            latitude = "y.321",
        )
        val saveUser = User(
            name = "newUser",
            phoneNumber = "123-456-789",
            email = "user@gmail.com",
            gender = Gender.MALE,
            status = UserStatus.ACTIVE,
            roadAddress = roadAddress
        )
        val taxi = Taxi(
            carNo = "taxi_num",
            kind = ChargeType.NORMAL,
            vin = "123A456BC",
            manufactureDate = LocalDate.now().minusYears(4),
            fuel = FuelType.GASOLINE,
            color = "RED",
            insureYN = true,
            accidentYN = false,
        )
        taxiDriver = TaxiDriver(
            taxi = taxi,
            driverLicenseId = "driver_license_id",
            issueDate = LocalDate.now().minusYears(5),
            securityId = "security_id",
            name = "Tom",
            status = TaxiDriverStatus.STARTDRIVE,
            gender = Gender.UNKNOWN,
            address = roadAddress,
            img = "123"
        )

        user = userRepository.save(saveUser)
        taxiRepository.save(taxi)
        taxiDriver = taxiDriverRepository.save(taxiDriver)
    }

    @AfterEach
    fun tearDown() {
        tripRepository.deleteAllInBatch()
        taxiRepository.deleteAllInBatch()
        taxiDriverRepository.deleteAllInBatch()
        userRepository.deleteAllInBatch()
    }

    //택시 호출 및 해당 택시 정보 수신
    @Test
    fun `사용자가 검색한 위치 정보를 가지고 택시를 호출 및 자동 배정`() {
        // given
        val trip = Trip(
            user = user,
            departure = departure,
            fare = 5000,
            destination = destination,
            driveStartDate = LocalDate.now(),
            driveStartAt = LocalDateTime.now(),
            spentTime = 12000000,
            tripStatus = TripStatus.CALL_TAXI,
            taxiDriver = null,
        )
        val taxi = Taxi(
            carNo = "taxi_num",
            kind = ChargeType.NORMAL,
            vin = "123A456BC",
            manufactureDate = LocalDate.now().minusYears(4),
            fuel = FuelType.GASOLINE,
            color = "RED",
            insureYN = true,
            accidentYN = false,
        )
        val waitingTaxiDriver = TaxiDriver(
            taxi = taxi,
            driverLicenseId = "driver_license_id",
            issueDate = LocalDate.now().minusYears(5),
            securityId = "security_id",
            name = "Tom",
            status = TaxiDriverStatus.WAITING,
            gender = Gender.UNKNOWN,
            address = roadAddress,
            img = "123"
        )
        taxiRepository.save(taxi)
        taxiDriverRepository.save(waitingTaxiDriver)

        // when
        val responseTrip: ResponseTrip = callingService.callTaxiByUser(trip)

        // then
        assertThat(responseTrip.tripStatus).isEqualTo(TripStatus.ASSIGN_TAXI)
        assertThat(responseTrip.taxiDriverId).isNotNull()
    }

    @Test
    fun `매칭 가능한 택시기사가 없는 경우 예외 반환`() {
        // given
        val trip = Trip(
            user = user,
            departure = departure,
            fare = 5000,
            destination = destination,
            driveStartDate = LocalDate.now(),
            driveStartAt = LocalDateTime.now(),
            spentTime = 12000000,
            tripStatus = TripStatus.CALL_TAXI,
            taxiDriver = null,
        )

        // when, then
        assertThrows<IllegalArgumentException> {
            callingService.assignTaxiDriver(trip)
        }.apply {
            assertThat(message).isEqualTo(ExceptionMessage.NO_SUCH_VALUE_EXISTS.name)
        }
    }

    @Test
    fun `사용자가 택시 이용 기록을 조회한다`() {
        // given
        val requestCreateTripByUser = RequestTrip(
            user = user,
            taxiDriver = taxiDriver,
            departure = departure,
            fare = 5000,
            destination = destination,
            driveStartDate = LocalDate.now(),
            driveStartAt = LocalDateTime.now(),
            spentTime = 12000000,
            tripStatus = TripStatus.END,
        )

        val requestTrip = requestCreateTripByUser.toEntity()
        val saveTrip = tripRepository.save(requestTrip)

        // when
        val responseTrip: ResponseTrip = callingService.findOneEndTripByUser(user.id, saveTrip.id)

        // then
        assertThat(responseTrip.tripStatus).isEqualTo(TripStatus.END)
    }

    @Test
    fun `사용자가 택시 이용 기록 조회에 실패한다`() {
        // given
        val tripId = 0L

        // when // then
        assertThrows<IllegalArgumentException>{
            callingService.findOneEndTripByUser(user.id, tripId)
        }.apply{
            assertThat(message).isEqualTo(ID_DOES_NOT_EXIST.name)
        }
    }

    @Test
    fun `사용자가 택시 이용 기록을 모두 조회한다`() {
        // given
        val firstTrip = RequestTrip(
            user = user,
            taxiDriver = taxiDriver,
            departure = departure,
            fare = 4000,
            destination = destination,
            driveStartDate = LocalDate.now(),
            driveStartAt = LocalDateTime.now(),
            spentTime = 11000000,
            tripStatus = TripStatus.END,
        )
        val secondTrip = RequestTrip(
            user = user,
            taxiDriver = taxiDriver,
            departure = departure,
            fare = 5000,
            destination = destination,
            driveStartDate = LocalDate.now(),
            driveStartAt = LocalDateTime.now(),
            spentTime = 12000000,
            tripStatus = TripStatus.END,
        )
        val thirdTrip = RequestTrip(
            user = user,
            taxiDriver = taxiDriver,
            departure = departure,
            fare = 6000,
            destination = destination,
            driveStartDate = LocalDate.now(),
            driveStartAt = LocalDateTime.now(),
            spentTime = 13000000,
            tripStatus = TripStatus.END,
        )
        val firstRequest = firstTrip.toEntity()
        val secondRequest = secondTrip.toEntity()
        val thirdRequest = thirdTrip.toEntity()
        tripRepository.save(firstRequest)
        tripRepository.save(secondRequest)
        tripRepository.save(thirdRequest)

        // when
        val trips = callingService.findAllEndTripByUser(user.id)

        // then
        Assertions.assertThat(trips.size).isEqualTo(3)

        assertThat(trips).extracting("fare")
            .containsExactlyInAnyOrder(firstTrip.fare, secondTrip.fare, thirdTrip.fare)
        assertThat(trips).extracting("spentTime")
            .containsExactlyInAnyOrder(firstTrip.spentTime, secondTrip.spentTime, thirdTrip.spentTime)
        assertThat(trips).extracting("tripStatus")
            .containsExactlyInAnyOrder(firstTrip.tripStatus, secondTrip.tripStatus, secondTrip.tripStatus)
        assertThat(trips).extracting("userId")
            .containsExactlyInAnyOrder(firstTrip.user.id, secondTrip.user.id, thirdTrip.user.id)
    }

}
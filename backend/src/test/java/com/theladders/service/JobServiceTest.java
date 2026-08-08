package com.theladders.service;

import com.theladders.model.CompanyType;
import com.theladders.model.EmploymentType;
import com.theladders.model.Job;
import com.theladders.model.Location;
import com.theladders.model.SalaryPeriod;
import com.theladders.storage.JobRepository;
import com.theladders.storage.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private LocationRepository locationRepository;

    private JobService jobService;

    @BeforeEach
    void setUp() {
        jobService = new JobService(jobRepository, locationRepository);
    }

    private Job job(BigDecimal salaryValue, String salaryCurrency, SalaryPeriod salaryPeriod,
            EmploymentType employmentType, CompanyType companyType, String language, Location location,
            boolean remote) {
        return new Job("Software Engineer", "A great job", "Acme", location, salaryValue, salaryCurrency,
                salaryPeriod, employmentType, LocalDate.now(), companyType, language, remote);
    }

    private Job eligibleJob() {
        return job(BigDecimal.valueOf(150000), "USD", SalaryPeriod.ANNUAL, EmploymentType.FULL_TIME,
                CompanyType.DIRECT_EMPLOYER, "English", new Location("New York", "NY", "USA"), false);
    }

    // --- ingest: approval logic ---

    @Test
    void ingestApprovesEligibleJob() {
        Job job = eligibleJob();

        jobService.ingest(List.of(job));

        assertThat(job.getRejectionReason()).isNull();
    }

    @Test
    void ingestRejectsJobNotRemoteOrInUsOrCanada() {
        Job job = job(BigDecimal.valueOf(150000), "USD", SalaryPeriod.ANNUAL, EmploymentType.FULL_TIME,
                CompanyType.DIRECT_EMPLOYER, "English", new Location("Berlin", null, "Germany"), false);

        jobService.ingest(List.of(job));

        assertThat(job.getRejectionReason()).isEqualTo("Job must be remote or located in the United States or Canada");
    }

    @Test
    void ingestApprovesRemoteJobRegardlessOfLocation() {
        Job job = job(BigDecimal.valueOf(150000), "USD", SalaryPeriod.ANNUAL, EmploymentType.FULL_TIME,
                CompanyType.DIRECT_EMPLOYER, "English", null, true);

        jobService.ingest(List.of(job));

        assertThat(job.getRejectionReason()).isNull();
    }

    @Test
    void ingestRejectsNonFullTimeEmployment() {
        Job job = job(BigDecimal.valueOf(150000), "USD", SalaryPeriod.ANNUAL, EmploymentType.PART_TIME,
                CompanyType.DIRECT_EMPLOYER, "English", new Location("New York", "NY", "USA"), false);

        jobService.ingest(List.of(job));

        assertThat(job.getRejectionReason()).isEqualTo("Job must be a full-time position");
    }

    @Test
    void ingestRejectsAnnualSalaryAtOrBelowMinimum() {
        Job job = job(BigDecimal.valueOf(100000), "USD", SalaryPeriod.ANNUAL, EmploymentType.FULL_TIME,
                CompanyType.DIRECT_EMPLOYER, "English", new Location("New York", "NY", "USA"), false);

        jobService.ingest(List.of(job));

        assertThat(job.getRejectionReason())
                .isEqualTo("Annual salary must be over $100,000 USD, or over $45/hour USD if paid hourly");
    }

    @Test
    void ingestRejectsHourlyRateAtOrBelowMinimum() {
        Job job = job(BigDecimal.valueOf(45), "USD", SalaryPeriod.HOURLY, EmploymentType.FULL_TIME,
                CompanyType.DIRECT_EMPLOYER, "English", new Location("New York", "NY", "USA"), false);

        jobService.ingest(List.of(job));

        assertThat(job.getRejectionReason())
                .isEqualTo("Annual salary must be over $100,000 USD, or over $45/hour USD if paid hourly");
    }

    @Test
    void ingestApprovesHourlyRateAboveMinimum() {
        Job job = job(BigDecimal.valueOf(45.01), "USD", SalaryPeriod.HOURLY, EmploymentType.FULL_TIME,
                CompanyType.DIRECT_EMPLOYER, "English", new Location("New York", "NY", "USA"), false);

        jobService.ingest(List.of(job));

        assertThat(job.getRejectionReason()).isNull();
    }

    @Test
    void ingestRejectsNonUsdSalaryCurrency() {
        Job job = job(BigDecimal.valueOf(150000), "EUR", SalaryPeriod.ANNUAL, EmploymentType.FULL_TIME,
                CompanyType.DIRECT_EMPLOYER, "English", new Location("New York", "NY", "USA"), false);

        jobService.ingest(List.of(job));

        assertThat(job.getRejectionReason())
                .isEqualTo("Annual salary must be over $100,000 USD, or over $45/hour USD if paid hourly");
    }

    @Test
    void ingestRejectsStaffingFirm() {
        Job job = job(BigDecimal.valueOf(150000), "USD", SalaryPeriod.ANNUAL, EmploymentType.FULL_TIME,
                CompanyType.STAFFING_FIRM, "English", new Location("New York", "NY", "USA"), false);

        jobService.ingest(List.of(job));

        assertThat(job.getRejectionReason()).isEqualTo("Job must not be from a staffing firm");
    }

    @Test
    void ingestRejectsNonEnglishLanguageOutsideCanada() {
        Job job = job(BigDecimal.valueOf(150000), "USD", SalaryPeriod.ANNUAL, EmploymentType.FULL_TIME,
                CompanyType.DIRECT_EMPLOYER, "Spanish", new Location("New York", "NY", "USA"), false);

        jobService.ingest(List.of(job));

        assertThat(job.getRejectionReason())
                .isEqualTo("Job description must be in English, or French if the job is in Canada");
    }

    @Test
    void ingestApprovesFrenchLanguageInCanada() {
        Job job = job(BigDecimal.valueOf(150000), "USD", SalaryPeriod.ANNUAL, EmploymentType.FULL_TIME,
                CompanyType.DIRECT_EMPLOYER, "French", new Location("Montreal", "QC", "Canada"), false);

        jobService.ingest(List.of(job));

        assertThat(job.getRejectionReason()).isNull();
    }

    @Test
    void ingestRejectsFrenchLanguageOutsideCanada() {
        Job job = job(BigDecimal.valueOf(150000), "USD", SalaryPeriod.ANNUAL, EmploymentType.FULL_TIME,
                CompanyType.DIRECT_EMPLOYER, "French", new Location("New York", "NY", "USA"), false);

        jobService.ingest(List.of(job));

        assertThat(job.getRejectionReason())
                .isEqualTo("Job description must be in English, or French if the job is in Canada");
    }

    @Test
    void ingestReturnsWhatRepositorySaveAllReturns() {
        Job job = eligibleJob();
        List<Job> saved = List.of(job);
        when(jobRepository.saveAll(List.of(job))).thenReturn(saved);

        List<Job> result = jobService.ingest(List.of(job));

        assertThat(result).isSameAs(saved);
    }

    // --- ingest: location resolution ---

    @Test
    void ingestSkipsLocationLookupWhenJobHasNoLocation() {
        Job job = job(BigDecimal.valueOf(150000), "USD", SalaryPeriod.ANNUAL, EmploymentType.FULL_TIME,
                CompanyType.DIRECT_EMPLOYER, "English", null, true);

        jobService.ingest(List.of(job));

        verifyNoInteractions(locationRepository);
    }

    @Test
    void ingestReusesExistingMatchingLocation() {
        Location existing = new Location("New York", "NY", "USA");
        when(locationRepository.findByCityAndStateAndCountry("New York", "NY", "USA")).thenReturn(List.of(existing));
        Job job = eligibleJob();

        jobService.ingest(List.of(job));

        assertThat(job.getLocation()).isSameAs(existing);
    }

    @Test
    void ingestKeepsNewLocationWhenNoExistingMatch() {
        when(locationRepository.findByCityAndStateAndCountry(any(), any(), any())).thenReturn(List.of());
        Job job = eligibleJob();
        Location original = job.getLocation();

        jobService.ingest(List.of(job));

        assertThat(job.getLocation()).isSameAs(original);
    }

    @Test
    void ingestOnlyLooksUpEachDistinctLocationOnceWithinABatch() {
        when(locationRepository.findByCityAndStateAndCountry(any(), any(), any())).thenReturn(List.of());
        Job job1 = eligibleJob();
        Job job2 = eligibleJob();

        jobService.ingest(List.of(job1, job2));

        verify(locationRepository, times(1)).findByCityAndStateAndCountry(any(), any(), any());
    }

    @Test
    void ingestMarksJobRemoteWhenResolvedLocationCountryIsRemote() {
        when(locationRepository.findByCityAndStateAndCountry(null, null, "Remote")).thenReturn(List.of());
        Job job = job(BigDecimal.valueOf(150000), "USD", SalaryPeriod.ANNUAL, EmploymentType.FULL_TIME,
                CompanyType.DIRECT_EMPLOYER, "English", new Location(null, null, "Remote"), false);

        jobService.ingest(List.of(job));

        assertThat(job.getRemote()).isTrue();
        assertThat(job.getLocation()).isNull();
    }

    // --- search ---

    @Test
    void searchThrowsForUnsupportedSortField() {
        assertThatThrownBy(() -> jobService.search(null, null, "unknown", "asc", 0, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported sort field: unknown");

        verifyNoInteractions(jobRepository);
    }

    @Test
    void searchIsUnsortedWhenSortByIsBlank() {
        when(jobRepository.findAll(any(), any(Pageable.class))).thenReturn(Page.empty());

        jobService.search(null, null, null, null, 0, 20);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobRepository).findAll(any(), captor.capture());
        assertThat(captor.getValue().getSort().isUnsorted()).isTrue();
    }

    @Test
    void searchSortsBySalaryDescendingAndPassesThroughPageAndSize() {
        when(jobRepository.findAll(any(), any(Pageable.class))).thenReturn(Page.empty());

        jobService.search(null, null, "salary", "desc", 2, 10);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobRepository).findAll(any(), captor.capture());
        Pageable pageable = captor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(10);
        Sort.Order order = pageable.getSort().getOrderFor("salaryValue");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void searchDefaultsToAscendingWhenDirectionIsNotDesc() {
        when(jobRepository.findAll(any(), any(Pageable.class))).thenReturn(Page.empty());

        jobService.search(null, null, "postingDate", "sideways", 0, 20);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobRepository).findAll(any(), captor.capture());
        Sort.Order order = captor.getValue().getSort().getOrderFor("postingDate");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void searchReturnsPageFromRepository() {
        Page<Job> page = new PageImpl<>(List.of(eligibleJob()));
        when(jobRepository.findAll(any(), any(Pageable.class))).thenReturn(page);

        Page<Job> result = jobService.search(null, null, null, null, 0, 20);

        assertThat(result).isSameAs(page);
    }
}

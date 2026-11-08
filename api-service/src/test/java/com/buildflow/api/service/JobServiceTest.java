package com.buildflow.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.buildflow.api.dto.CreateJobRequest;
import com.buildflow.api.dto.JobResponse;
import com.buildflow.api.exception.JobNotFoundException;
import com.buildflow.api.model.Job;
import com.buildflow.api.model.JobStatus;
import com.buildflow.api.model.Role;
import com.buildflow.api.model.User;
import com.buildflow.api.repository.JobRepository;
import com.buildflow.api.repository.UserRepository;
import com.buildflow.api.service.JobService;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private JobService jobService;

    @Test
    void createJob_savesJobAsPendingAndOwnedByUser() {
        // Arrange
        User sid = new User("sid", "hash", Role.USER);
        when(userRepository.findByUsername("sid")).thenReturn(Optional.of(sid));
        when(jobRepository.save(any(Job.class))).thenAnswer(call -> call.getArgument(0));
        CreateJobRequest request = new CreateJobRequest("https://github.com/sid/app", "main", "mvn test");

        // Act
        JobResponse response = jobService.createJob(request, "sid");

        // Assert
        assertThat(response.status()).isEqualTo(JobStatus.PENDING);
        assertThat(response.owner()).isEqualTo("sid");

        ArgumentCaptor<Job> saved = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(saved.capture());
        assertThat(saved.getValue().getCommand()).isEqualTo("mvn test");
    }

    @Test
    void getJob_belongingToSomeoneElse_throwsNotFound() {
        // Arrange: the repository finds nothing for rahul + job 1
        when(jobRepository.findByIdAndOwnerUsername(1L, "rahul")).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> jobService.getJob(1L, "rahul"))
                .isInstanceOf(JobNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void createJob_unknownUser_throwsAndSavesNothing() {
        // Arrange
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        CreateJobRequest request = new CreateJobRequest("https://github.com/x/y", "main", "ls");

        // Act + Assert
        assertThatThrownBy(() -> jobService.createJob(request, "ghost"))
                .isInstanceOf(IllegalStateException.class);
        verify(jobRepository, never()).save(any());
    }
}
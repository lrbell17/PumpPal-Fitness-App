package com.lrbell.fitness.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lrbell.fitness.api.helpers.ExerciseDeserializer;
import com.lrbell.fitness.api.helpers.UserDeserializer;
import com.lrbell.fitness.api.responses.exceptions.ExerciseNotFoundException;
import com.lrbell.fitness.api.responses.exceptions.InvalidUserIdUpdateException;
import com.lrbell.fitness.api.responses.exceptions.InvalidWorkoutStateException;
import com.lrbell.fitness.api.responses.exceptions.UserNotFoundException;
import com.lrbell.fitness.model.enums.WorkoutState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The entity representing a workout.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "WORKOUT", indexes = @Index(columnList = "userId"))
public class Workout implements ModelEntity {

    /**
     * The workout ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String workoutId;

    /**
     * The name of the workout.
     */
    @Column
    private String workoutName;

    /**
     * The user who the workout pertains to.
     */
    @ManyToOne
    @JoinColumn(name = "userId")
    @JsonDeserialize(using = UserDeserializer.class)
    @NotNull
    @JsonProperty("userId")
    private User user;

    /**
     * The {@link WorkoutState} representing the state of the workout.
     */
    @Enumerated(EnumType.STRING)
    private WorkoutState workoutState;

    /**
     * The epoch timestamp when the workout started.
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Long startTime;

    /**
     * The epoch timestamp when the workout ended.
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Long endTime;

    /**
     * The list of {@link Exercise}s performed during the workout.
     */
    @ManyToMany
    @JoinTable(
            name = "WORKOUT_EXERCISE",
            joinColumns = @JoinColumn(name = "workoutId"),
            inverseJoinColumns = @JoinColumn(name = "exerciseId")
    )
    @JsonDeserialize(using = ExerciseDeserializer.class)
    private List<Exercise> exercises;

    /**
     * Verifies that the workout is active.
     *
     * @throws InvalidWorkoutStateException
     */
    public void verifyWorkoutActive() throws InvalidWorkoutStateException {
        if (this.workoutState != WorkoutState.ACTIVE) {
            throw new InvalidWorkoutStateException();
        }
    }

    /**
     * Verifies that the user ID of the workout if not being updated.
     *
     * @param userId The new user ID.
     * @throws InvalidUserIdUpdateException
     */
    public void verifyUserIdNotUpdated(final String userId) throws InvalidUserIdUpdateException {
        if (!(Objects.isNull(userId) || userId.equals(this.getUser().getUserId()))) {
            throw new InvalidUserIdUpdateException();
        }
    }

    /**
     * Verifies that the user ID provided in the payload exists.
     *
     * @throws UserNotFoundException
     */
    public void verifyUserExists() throws UserNotFoundException {
        final String userId = this.user.getUserId();
        final String userName = this.user.getUserName();
        if (userName.equals(UserDeserializer.USER_NOT_FOUND_DUMMY_NAME)) {
            throw new UserNotFoundException(userId);
        }
    }

    /**
     * Verifies the list of exercises provided in the payload exist.
     *
     * @throws ExerciseNotFoundException
     */
    public void verifyExercisesExist() throws ExerciseNotFoundException {
        final List<Exercise> exerciseList = this.exercises;
        final List<String> errorMessage = new ArrayList<>();

        if (exerciseList.get(0).getExerciseName().equals(ExerciseDeserializer.EXERCISE_NOT_FOUND_DUMMY_NAME)) {
            for (Exercise exercise : exerciseList) {
                errorMessage.add(exercise.getExerciseId());
            }
            throw new ExerciseNotFoundException(errorMessage);
        }
    }
}

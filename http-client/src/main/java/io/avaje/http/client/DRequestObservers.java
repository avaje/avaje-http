package io.avaje.http.client;

import java.net.http.HttpResponse;
import java.util.List;

final class DRequestObservers implements RequestObserver {

  private final RequestObserver[] observers;

  DRequestObservers(List<RequestObserver> requestObservers) {
    this.observers = requestObservers.toArray(new RequestObserver[0]);
  }

  @Override
  public Observation start(HttpClientRequest request) {
    final Observation[] observations = new Observation[observers.length];
    for (int i = 0; i < observers.length; i++) {
      final Observation observation = observers[i].start(request);
      observations[i] = observation == null ? Observation.NOOP : observation;
    }
    return new DObservation(observations);
  }

  private static final class DObservation implements Observation {

    private final Observation[] observations;

    private DObservation(Observation[] observations) {
      this.observations = observations;
    }

    @Override
    public Attempt startAttempt(HttpClientRequest request, int resendCount) {
      final Attempt[] attempts = new Attempt[observations.length];
      for (int i = 0; i < observations.length; i++) {
        final Attempt attempt = observations[i].startAttempt(request, resendCount);
        attempts[i] = attempt == null ? Attempt.NOOP : attempt;
      }
      return new DAttempt(attempts);
    }
  }

  private static final class DAttempt implements Attempt {

    private final Attempt[] attempts;

    private DAttempt(Attempt[] attempts) {
      this.attempts = attempts;
    }

    @Override
    public void onResponse(HttpResponse<?> response) {
      for (final Attempt attempt : attempts) {
        attempt.onResponse(response);
      }
    }

    @Override
    public void onError(Throwable error) {
      for (final Attempt attempt : attempts) {
        attempt.onError(error);
      }
    }
  }
}

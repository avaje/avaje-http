package io.avaje.htmx.api;

import io.avaje.lang.Nullable;

final class DHxRequest implements HtmxRequest {

  private final boolean htmxRequest;

  private final boolean boosted;
  private final String currentUrl;
  private final boolean historyRestoreRequest;
  private final String promptResponse;
  private final String target;
  private final String triggerName;
  private final String triggerId;

  DHxRequest() {
    this.htmxRequest = false;
    this.boosted = false;
    this.currentUrl = null;
    this.historyRestoreRequest = false;
    this.promptResponse = null;
    this.target = null;
    this.triggerName = null;
    this.triggerId = null;
  }

  DHxRequest(boolean boosted, String currentUrl, boolean historyRestoreRequest, String promptResponse, String target, String triggerName, String triggerId) {
    this.htmxRequest = true;
    this.boosted = boosted;
    this.currentUrl = currentUrl;
    this.historyRestoreRequest = historyRestoreRequest;
    this.promptResponse = promptResponse;
    this.target = target;
    this.triggerName = triggerName;
    this.triggerId = triggerId;
  }

  @Override
  public boolean isHtmxRequest() {
    return htmxRequest;
  }

  @Override
  public boolean isBoosted() {
    return boosted;
  }

  @Nullable
  @Override
  public String currentUrl() {
    return currentUrl;
  }

  @Override
  public boolean isHistoryRestoreRequest() {
    return historyRestoreRequest;
  }

  @Nullable
  @Override
  public String promptResponse() {
    return promptResponse;
  }

  @Nullable
  @Override
  public String target() {
    return target;
  }

  @Nullable
  @Override
  public String triggerName() {
    return triggerName;
  }

  @Nullable
  public String triggerId() {
    return triggerId;
  }

  static final class DBuilder implements Builder {

    private boolean boosted;
    private String currentUrl;
    private boolean historyRestoreRequest;
    private String promptResponse;
    private String target;
    private String triggerName;
    private String triggerId;

    @Override
    public DBuilder boosted(boolean boosted) {
      this.boosted = boosted;
      return this;
    }

    @Override
    public DBuilder currentUrl(String currentUrl) {
      this.currentUrl = currentUrl;
      return this;
    }

    @Override
    public DBuilder historyRestoreRequest(boolean historyRestoreRequest) {
      this.historyRestoreRequest = historyRestoreRequest;
      return this;
    }

    @Override
    public DBuilder promptResponse(String promptResponse) {
      this.promptResponse = promptResponse;
      return this;
    }

    @Override
    public DBuilder target(String target) {
      this.target = target;
      return this;
    }

    @Override
    public DBuilder triggerName(String triggerName) {
      this.triggerName = triggerName;
      return this;
    }

    @Override
    public DBuilder triggerId(String triggerId) {
      this.triggerId = triggerId;
      return this;
    }

    @Override
    public HtmxRequest build() {
      return new DHxRequest(boosted, currentUrl, historyRestoreRequest, promptResponse, target, triggerName, triggerId);
    }
  }

}
